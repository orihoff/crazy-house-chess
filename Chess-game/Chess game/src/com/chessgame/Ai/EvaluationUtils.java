package com.chessgame.Ai;

import com.chessgame.Board.Board;
import com.chessgame.Board.Move;
import com.chessgame.Pieces.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class containing static methods for one-ply evaluation.
 * The evaluation includes PSTs (Piece-Square Tables), pawn structure,
 * king safety, threats, and other factors.
 */
public class EvaluationUtils {

    private static final double MATERIAL_FACTOR = 4.0;
    private static final int BOARD_SIZE = Board.getSize();
    private static double GAME_STAGE_FACTOR = 0.0;

    /**
     * Searches the board for a King of the specified color.
     */
    public static King findKingOnBoard(Board board, boolean isWhite) {
        for (Piece p : board.getAllPieces()) {
            if (p instanceof King && p.isWhite() == isWhite) {
                return (King) p;
            }
        }
        return null;
    }

    /**
     * Determines if the specified king is in check by examining all enemy moves.
     */
    public static boolean isKingInCheck(Board board, King king) {
        if (king == null) return false;
        int kx = king.getXcord(), ky = king.getYcord();
        for (Piece e : board.getAllPieces()) {
            if (e.isWhite() != king.isWhite()) {
                e.fillAllPseudoLegalMoves(board);
                for (Move m : e.getMoves()) {
                    if (m.getToX() == kx && m.getToY() == ky) return true;
                }
            }
        }
        return false;
    }

    /**
     * Determines if a given move is legal by simulating it and checking if the king remains safe.
     */
    public static boolean isMoveLegal(Board board, Piece piece, Move move) {
        Board cb = board.clone();
        Piece cp = cb.getPiece(piece.getXcord(), piece.getYcord());
        if (cp == null) return false;
        cp.makeMove(move.getToX(), move.getToY(), cb);
        King k = findKingOnBoard(cb, piece.isWhite());
        return k != null && !isKingInCheck(cb, k);
    }

    /**
     * Determines if a Transplant move is legal by simulating it and checking if the king remains safe.
     */
    public static boolean isTransplantLegal(Board board, TransplantMoveCandidate candidate) {
        Board cb = board.clone();
        candidate.apply(cb);
        King k = findKingOnBoard(cb, candidate.isWhite());
        return k != null && !isKingInCheck(cb, k);
    }

    /**
     * Evaluates the current board position from the perspective of the AI.
     */
    public static int evaluateBoard(Board board, boolean aiIsWhite) {
        // 1) Game‐stage factor
        GAME_STAGE_FACTOR = computeGlobalGameStageFactor(board);

        // 2) Immediate mate
        if (isMate(board, true))  return getMateScore(aiIsWhite, false);
        if (isMate(board, false)) return getMateScore(aiIsWhite, true);

        // 3) Setup accumulators
        double whiteScore = 0.0, blackScore = 0.0;
        int[] whiteCounts = new int[Piece.KING_INDEX + 1];
        int[] blackCounts = new int[Piece.KING_INDEX + 1];
        List<int[]> whitePawns = new ArrayList<>();
        List<int[]> blackPawns = new ArrayList<>();

     // 4) For each piece: add its material + PST + center bonus, count types, collect pawns, and bonus bishops on long diagonals
        for (Piece p : board.getAllPieces()) {
            int r = p.getXcord(), c = p.getYcord();
            double mat = Math.abs(p.getValueInTheboard()) * MATERIAL_FACTOR;
            int pst = p.getPositionBonus(r, c);
            int centerBonus = ((r == 3 || r == 4) && (c == 3 || c == 4)) ? 1 : 0;
            double score = mat + pst + centerBonus;

            int type = p.getPieceTypeIndex();
            if (p.isWhite()) {
                whiteScore += score;
                whiteCounts[type]++;
                if (type == Piece.PAWN_INDEX) whitePawns.add(new int[]{r, c});
                if (type == Piece.BISHOP_INDEX && (r == c || r + c == BOARD_SIZE - 1)) whiteScore += 0.25;
            } else {
                blackScore += score;
                blackCounts[type]++;
                if (type == Piece.PAWN_INDEX) blackPawns.add(new int[]{r, c});
                if (type == Piece.BISHOP_INDEX && (r == c || r + c == BOARD_SIZE - 1)) blackScore += 0.25;
            }
        }


        // 5) Pair‐of‐bishops bonus
        if (whiteCounts[Piece.BISHOP_INDEX] >= 2) whiteScore += 1.0;
        if (blackCounts[Piece.BISHOP_INDEX] >= 2) blackScore += 1.0;

        // 6) Pawn structure
        whiteScore += evaluatePawnStructure(board, whitePawns, true);
        blackScore += evaluatePawnStructure(board, blackPawns, false);

        // 7) Rooks on open/semi-open files
        whiteScore += evaluateRooksOpenFiles(board, true,  whiteCounts[Piece.ROOK_INDEX]);
        blackScore += evaluateRooksOpenFiles(board, false, blackCounts[Piece.ROOK_INDEX]);

        // 8) King safety
        whiteScore += evaluateKingSafety(board, true);
        blackScore += evaluateKingSafety(board, false);

        // 9) Mobility
        whiteScore += countAllLegalMoves(board, true)  * 0.05;
        blackScore += countAllLegalMoves(board, false) * 0.05;

        // 10) Endgame king positioning (scaled by game‐stage)
        whiteScore += evaluateEndgameKingPosition(board, true)  * GAME_STAGE_FACTOR;
        blackScore += evaluateEndgameKingPosition(board, false) * GAME_STAGE_FACTOR;

        // 11) Center control + castling
        whiteScore += evaluateCenterControl(board, true)  + evaluateCastlingBonus(board, true);
        blackScore += evaluateCenterControl(board, false) + evaluateCastlingBonus(board, false);

        // 12) Slight bonus if the opponent is in check
        King wK = findKingOnBoard(board, true), bK = findKingOnBoard(board, false);
        if (wK != null && isKingInCheck(board, wK)) blackScore += 0.5;
        if (bK != null && isKingInCheck(board, bK)) whiteScore += 0.5;

        // 13) Threatened pieces
        double[] th = evaluateThreatenedPieces(board);
        whiteScore += th[0];
        blackScore += th[1];

        // 14) Final difference & invert if AI=black
        double diff = whiteScore - blackScore;
        int finalScore = (int)(diff * 100);
        if (!aiIsWhite) finalScore = -finalScore;

        // 15) Extra penalties/bonuses
        finalScore += applyImmediateCapturePenalty(board, aiIsWhite);
        finalScore += applyMateInOneThreatPenalty(board, aiIsWhite);
        finalScore += evaluateEarlyQueenPenalty(board, aiIsWhite);
        finalScore += evaluateMinorPieceMovementPenalty(board, aiIsWhite);

        return finalScore;
    }

    private static double computeGlobalGameStageFactor(Board board) {
        int totalMaterial = 0;
        for (Piece p : board.getAllPieces()) {
            if (!(p instanceof King)) {
                totalMaterial += Math.abs(p.getValueInTheboard());
            }
        }
        if (totalMaterial > 35) {
            return 0.0;
        } else if (totalMaterial > 20) {
            return (35 - totalMaterial) / 15.0;
        } else {
            return 1.0;
        }
    }

    private static int applyImmediateCapturePenalty(Board board, boolean aiIsWhite) {
        boolean[][] attacked = new boolean[BOARD_SIZE][BOARD_SIZE];
        fillAttackedSquares(board, !aiIsWhite, attacked);
        int penalty = 0;
        for (Piece p : board.getAllPieces()) {
            if (p.isWhite() == aiIsWhite) {
                int r = p.getXcord(), c = p.getYcord();
                if (attacked[r][c]) {
                    double loss = Math.abs(p.getValueInTheboard()) * MATERIAL_FACTOR;
                    penalty -= (int)(loss * 200);
                }
            }
        }
        return penalty;
    }

    private static int applyMateInOneThreatPenalty(Board board, boolean aiIsWhite) {
        boolean opp = !aiIsWhite;
        for (Piece p : board.getAllPieces()) {
            if (p.isWhite() == opp) {
                p.fillAllPseudoLegalMoves(board);
                for (Move m : p.getMoves()) {
                    if (isMoveLegal(board, p, m)) {
                        Board cb = board.clone();
                        Piece cp = cb.getPiece(p.getXcord(), p.getYcord());
                        if (cp != null) cp.makeMove(m.getToX(), m.getToY(), cb);
                        if (isMate(cb, aiIsWhite)) {
                            return -100000;
                        }
                    }
                }
            }
        }
        return 0;
    }

    /**
     * Evaluate penalties for pieces that are being attacked.
     * @param board    the current board state
     * @return         an array [whitePenalty, blackPenalty]
     */
    private static double[] evaluateThreatenedPieces(Board board) {
        // 1) Build attack maps for both sides
        boolean[][] wAtt = new boolean[BOARD_SIZE][BOARD_SIZE];
        boolean[][] bAtt = new boolean[BOARD_SIZE][BOARD_SIZE];
        fillAttackedSquares(board, true,  wAtt);   // squares White attacks
        fillAttackedSquares(board, false, bAtt);   // squares Black attacks

        double wPen = 0.0, bPen = 0.0;

        // 2) For each piece on the board...
        for (Piece p : board.getAllPieces()) {
            int r = p.getXcord(), c = p.getYcord();
            boolean isWhite = p.isWhite();
            double val = Math.abs(p.getValueInTheboard());

            // 3) Is this square attacked by the enemy?
            boolean threatened = isWhite ? bAtt[r][c] : wAtt[r][c];
            if (!threatened) continue;

            // 4) Is the piece defended by its own side?
            boolean defended = isWhite ? wAtt[r][c] : bAtt[r][c];

            // 5) Find the smallest‐valued attacker
            double minAttacker = findMinEnemyAttackerValue(board, r, c, isWhite);

            // 6) Compute penalty:
            //    – If undefended: value + 3
            //    – Else if lowest attacker’s value ≤ piece’s value: value + 2
            //    – Otherwise: half the piece’s value
            double pen = !defended
                       ? val + 3.0
                       : (minAttacker <= val ? val + 2.0 : val * 0.5);

            // 7) Subtract from that side’s total
            if (isWhite) wPen -= pen;
                     else bPen -= pen;
        }

        return new double[]{wPen, bPen};
    }

    /**
     * Find the minimum piece‐value of any enemy attacker that can move to (row,col).
     * @param board    current board
     * @param row,col  target square
     * @param isWhite  true if we’re finding attackers against White’s piece
     * @return         smallest attacker value, or 9999.0 if none found
     */
    private static double findMinEnemyAttackerValue(Board board, int row, int col, boolean isWhite) {
        double min = Double.POSITIVE_INFINITY;

        // For every opponent piece...
        for (Piece p : board.getAllPieces()) {
            if (p.isWhite() == isWhite) continue;   // skip own pieces

            // Generate its pseudo‐moves and check if any lands on (row,col)
            p.fillAllPseudoLegalMoves(board);
            for (Move m : p.getMoves()) {
                if (m.getToX() == row && m.getToY() == col) {
                    double v = Math.abs(p.getValueInTheboard());
                    if (v < min) min = v;
                }
            }
        }
        return (min == Double.POSITIVE_INFINITY) ? 9999.0 : min;
    }

    /**
     * Fill a boolean grid with every square attacked by the given side’s pseudo‐legal moves.
     * @param board    current board
     * @param isWhite  which side to map (true=White, false=Black)
     * @param att      output array: att[r][c]=true if attacked
     */
    private static void fillAttackedSquares(Board board, boolean isWhite, boolean[][] att) {
        // 1) Clear the grid
        for (int r = 0; r < BOARD_SIZE; r++)
            for (int c = 0; c < BOARD_SIZE; c++)
                att[r][c] = false;

        // 2) For each piece of that color, mark all destinations
        for (Piece p : board.getAllPieces()) {
            if (p.isWhite() != isWhite) continue;
            p.fillAllPseudoLegalMoves(board);
            for (Move m : p.getMoves()) {
                att[m.getToX()][m.getToY()] = true;
            }
        }
    }

    /**
     * Evaluates pawn structure factors:
     * - Doubled‐pawn penalty
     * - Isolated‐pawn penalty
     * - Passed‐pawn bonus (scaled by rank)
     */
    private static double evaluatePawnStructure(Board board, List<int[]> pawns, boolean isWhite) {
        double score = 0.0;

        // Count how many pawns occupy each file (column)
        int[] files = new int[BOARD_SIZE];
        for (int[] loc : pawns) {
            files[loc[1]]++;
        }

        // 1) Doubled pawn penalty: if more than one pawn on a file, subtract 0.25 per extra pawn
        for (int f = 0; f < BOARD_SIZE; f++) {
            if (files[f] > 1) {
                score -= (files[f] - 1) * 0.25;
            }
        }

        // 2) For each pawn, apply isolated pawn penalty and passed pawn bonus
        for (int[] loc : pawns) {
            int row = loc[0];
            int col = loc[1];

            // Check isolation: no friendly pawn on adjacent files
            boolean left  = (col == 0)                  || (files[col - 1] == 0);
            boolean right = (col == BOARD_SIZE - 1)     || (files[col + 1] == 0);
            // If both sides are empty, this pawn is isolated → penalty
            if (left && right) {
                score -= 0.3;
            }

            // Passed pawn bonus: add 0.5 plus a small bonus per rank advanced
            if (isPawnPassed(board, row, col, isWhite)) {
                // Normalize rank from pawn’s perspective (0 at its home rank)
                int rank = isWhite ? row : (BOARD_SIZE - 1 - row);
                score += 0.5 + rank * 0.05;
            }
        }

        return score;
    }

    /**
     * Checks whether a pawn is “passed” (no opposing pawns on its file or adjacent files
     * in front of it).
     *
     * @param board   current board state
     * @param row     pawn’s current rank
     * @param col     pawn’s current file
     * @param isWhite true for White’s pawn, false for Black’s
     * @return true if pawn is passed, false otherwise
     */
    private static boolean isPawnPassed(Board board, int row, int col, boolean isWhite) {
        // Determine forward direction and stopping condition
        int dir = isWhite ? +1 : -1;
        int start = row + dir;
        int end   = isWhite ? BOARD_SIZE : -1;

        // Walk each square ahead of the pawn until the last rank
        for (int r = start; isWhite ? (r < end) : (r > end); r += dir) {
            // Check the pawn’s file and both adjacent files
            for (int cc = col - 1; cc <= col + 1; cc++) { // Inner loop: 3 iterations each time
                // Skip off‐board files
                if (cc < 0 || cc >= BOARD_SIZE) continue;
                Piece p = board.getPiece(r, cc);
                // If we encounter an enemy pawn blocking the advance, it’s not passed
                if (p instanceof Pawn && p.isWhite() != isWhite) {
                    return false;
                }
            }
        }

        // No blocking enemy pawn found → it’s a passed pawn
        return true;
    }


    /**
     * 7) Rooks on open and semi open files
     *
     * +0.5 for each rook on an open file (no pawns of either color)
     * +0.25 for each rook on a semi open file (no own pawns)
     * +0.2 bonus for rooks on the 7th rank (White) or 2nd rank (Black)
     */
    private static double evaluateRooksOpenFiles(Board board, boolean isWhite, int rookCount) {
        if (rookCount == 0) return 0.0;
        double score = 0.0;

        // For each rook of the given color:
        for (Piece p : board.getAllPieces()) {
            if (p instanceof Rook && p.isWhite() == isWhite) {
                int row = p.getXcord(), col = p.getYcord();
                boolean ownPawn   = false;
                boolean enemyPawn = false;

                // Scan the entire file for pawns
                for (int r = 0; r < BOARD_SIZE; r++) {
                    Piece sq = board.getPiece(r, col);
                    if (sq instanceof Pawn) {
                        if (sq.isWhite() == isWhite) ownPawn   = true;
                        else                          enemyPawn = true;
                    }
                }

                // Open file if no pawns at all
                if (!ownPawn && !enemyPawn) {
                    score += 0.5;
                }
                // Semi‑open file if only enemy pawns present
                else if (!ownPawn) {
                    score += 0.25;
                }

                // Bonus for rooks on opponent’s back‑rank
                if (isWhite && row == BOARD_SIZE - 2) score += 0.2;  // White on rank 7
                if (!isWhite && row == 1)            score += 0.2;  // Black on rank 2
            }
        }

        return score;
    }

    /**
     * 8) King safety by pawn shield
     *
     * +0.2 for each friendly pawn on one of the 8 surrounding squares
     * –0.3 if the king is too far from the corner (i.e. stuck in center)
     */
    private static double evaluateKingSafety(Board board, boolean isWhite) {
        King k = findKingOnBoard(board, isWhite);
        if (k == null) return 0.0;

        int kr = k.getXcord(), kc = k.getYcord();
        int shieldPawns = 0;

        // Count friendly pawns in all adjacent directions
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                int rr = kr + dr, cc = kc + dc;
                if (rr >= 0 && rr < BOARD_SIZE && cc >= 0 && cc < BOARD_SIZE) {
                    Piece n = board.getPiece(rr, cc);
                    if (n instanceof Pawn && n.isWhite() == isWhite) {
                        shieldPawns++;
                    }
                }
            }
        }

        // Base safety = 0.2 per pawn shield
        double safety = shieldPawns * 0.2;

        // Penalty if king is too central (ranks/files 2–5)
        if (kr >= 2 && kr <= BOARD_SIZE - 3 && kc >= 2 && kc <= BOARD_SIZE - 3) {
            safety -= 0.3;
        }

        return safety;
    }

    /**
     * 9) Mobility
     *
     * Simply counts the number of legal moves available to the side.
     */
    private static int countAllLegalMoves(Board board, boolean isWhite) {
        int count = 0;
        for (Piece p : board.getAllPieces()) {
            if (p.isWhite() == isWhite) {
                p.fillAllPseudoLegalMoves(board);
                for (Move m : p.getMoves()) {
                    if (isMoveLegal(board, p, m)) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    /**
     * 10) Endgame king positioning
     *
     * Encourages the king to move toward the center in the endgame.
     * Score = (max distance to center – current distance) * 0.1
     */
    private static double evaluateEndgameKingPosition(Board board, boolean isWhite) {
        King k = findKingOnBoard(board, isWhite);
        if (k == null) return 0.0;

        int kr = k.getXcord(), kc = k.getYcord();
        int center = BOARD_SIZE / 2 - 1;
        int dist   = Math.abs(kr - center) + Math.abs(kc - center);

        // The closer the king is to center, the higher the bonus
        return (center - dist) * 0.1;
    }

    /**
     * 11) Center control
     *
     * Values occupancy of the four central squares:
     *  +2 for a pawn, +0.5 for any other friendly piece
     *  –2 or –0.5 for enemy pieces
     * If a square is empty, adds small influence based on pseudo legal moves targeting it:
     *  +0.3 for pawn, +0.2 for other friendly movers
     *  –0.3 / –0.2 for enemy
     */
    private static double evaluateCenterControl(Board board, boolean isWhite) {
        double bonus = 0.0;
        int mid = BOARD_SIZE / 2;
        int[][] centers = {{mid-1, mid-1}, {mid-1, mid}, {mid, mid-1}, {mid, mid}};

        for (int[] sq : centers) {
            int r = sq[0], c = sq[1];
            Piece occupant = board.getPiece(r, c);

            if (occupant != null) {
                // Direct occupancy bonus/penalty
                if (occupant.isWhite() == isWhite) {
                    bonus += (occupant instanceof Pawn) ? 2 : 0.5;
                } else {
                    bonus -= (occupant instanceof Pawn) ? 2 : 0.5;
                }
            } else {
                // Influence by pseudo‑legal attacks
                double friendlyInfluence = 0, enemyInfluence = 0;
                for (Piece p : board.getAllPieces()) {
                    p.fillAllPseudoLegalMoves(board);
                    for (Move m : p.getMoves()) {
                        if (m.getToX() == r && m.getToY() == c) {
                            if (p.isWhite() == isWhite) {
                                friendlyInfluence += (p instanceof Pawn) ? 0.3 : 0.2;
                            } else {
                                enemyInfluence   += (p instanceof Pawn) ? 0.3 : 0.2;
                            }
                        }
                    }
                }
                bonus += friendlyInfluence - enemyInfluence;
            }
        }

        return bonus;
    }


    /**
     * Checks whether a side is in mate (no legal move can remove the check).
     */
    public static boolean isMate(Board board, boolean isWhite) {
        King k = findKingOnBoard(board, isWhite);
        if (k == null) return false;
        if (!isKingInCheck(board, k)) return false;
        for (Piece p : board.getAllPieces()) {
            if (p.isWhite() == isWhite) {
                p.fillAllPseudoLegalMoves(board);
                for (Move m : p.getMoves()) {
                    if (isMoveLegal(board, p, m)) {
                        Board cb = board.clone();
                        Piece cp = cb.getPiece(p.getXcord(), p.getYcord());
                        if (cp != null) cp.makeMove(m.getToX(), m.getToY(), cb);
                        King ck = findKingOnBoard(cb, isWhite);
                        if (ck != null && !isKingInCheck(cb, ck)) return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Returns a large positive score if the AI side wins,
     * and a small negative score if the AI side loses.
     */
    private static int getMateScore(boolean aiIsWhite, boolean whiteWins) {
        return (aiIsWhite == whiteWins) ? 100000 : -100000;
    }

    /**
     * Evaluates a bonus for castling by checking the king's position.
     */
    private static double evaluateCastlingBonus(Board board, boolean isWhite) {
        King k = findKingOnBoard(board, isWhite);
        if (k == null) return 0.0;
        int x = k.getXcord(), y = k.getYcord();
        if (isWhite && x == 0 && (y == 2 || y == 6)) return 1.0;
        if (!isWhite && x == BOARD_SIZE-1 && (y == 2 || y == 6)) return 1.0;
        return 0.0;
    }

   
    /**
     * Applies a penalty for developing the queen off its original square in the opening.
     * The penalty scales down as the game transitions from opening to endgame.
     *
     * @param board      the current board state
     * @param aiIsWhite  true if evaluating White’s queen, false for Black’s
     * @return the penalty to subtract from the evaluation score
     */
    private static int evaluateEarlyQueenPenalty(Board board, boolean aiIsWhite) {
        // Determine the queen’s home rank: White on rank 7, Black on rank 0
        int startRank = aiIsWhite ? BOARD_SIZE - 1 : 0;
        // Queen always starts on file 'd' (index 3)
        int startFile = 3;

        // Invert the game‐stage factor so the penalty is largest in the opening
        double factor = 1.0 - GAME_STAGE_FACTOR;

        for (Piece p : board.getAllPieces()) {
            if (p instanceof Queen && p.isWhite() == aiIsWhite) {
                // If the queen has moved from its home square, apply the penalty
                if (p.getYcord() != startRank || p.getXcord() != startFile) {
                    return (int)(-500 * factor);
                }
            }
        }
        return 0;
    }




    /**
     * Applies a penalty for knights and bishops that remain on their home squares in the opening.
     * The penalty scales down as the game progresses toward the endgame.
     *
     * @param board   the current board state
     * @param isWhite true if evaluating White’s pieces, false for Black’s
     * @return the total penalty to subtract from the evaluation
     */
    private static int evaluateMinorPieceMovementPenalty(Board board, boolean isWhite) {
        // Invert game‐stage so penalty is highest in the opening and zero in the endgame
        double factor = 1.0 - GAME_STAGE_FACTOR;
        int penalty = 0;
        // White home rank = 7, Black home rank = 0
        int homeRank = isWhite ? BOARD_SIZE - 1 : 0;

        for (Piece p : board.getAllPieces()) {
            if (p.isWhite() != isWhite) continue;

            int file = p.getXcord();  // 0 = 'a', 1 = 'b', … 7 = 'h'
            int rank = p.getYcord();  // 0 = 1st rank, … 7 = 8th rank

            // Knight penalty: still on b1/g1 or b8/g8?
            if (p instanceof Knight) {
                if (rank == homeRank && (file == 1 || file == 6)) {
                    penalty -= (int)(50 * factor);
                }
            }
            // Bishop penalty: still on c1/f1 or c8/f8?
            if (p instanceof Bishop) {
                if (rank == homeRank && (file == 2 || file == 5)) {
                    penalty -= (int)(50 * factor);
                }
            }
        }
        return penalty;
    }


}
