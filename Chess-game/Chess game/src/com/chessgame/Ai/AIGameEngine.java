package com.chessgame.Ai;

import com.chessgame.Board.Board;
import com.chessgame.Board.Move;
import com.chessgame.Game.Game;
import com.chessgame.Game.ToolShed;
import com.chessgame.Game.Game.Mode;
import com.chessgame.Pieces.Piece;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.Timer;

public class AIGameEngine {
	
	private static final int BOARD_SIZE = Board.getSize();
    // Bonus factor to reward transplant moves
    private static final int BONUS_FACTOR = 100;

    // Class to hold a candidate move with its evaluation score.
    private static class CandidateScorePair implements Comparable<CandidateScorePair> {
        public MoveCandidate candidate;
        public int score;

        public CandidateScorePair(MoveCandidate candidate, int score) {
            this.candidate = candidate;
            this.score = score;
        }

        @Override
        public int compareTo(CandidateScorePair other) {
            // Sorting in descending order (best score first)
            return Integer.compare(other.score, this.score);
        }
    }

    /**
     * AI move selection based on a 1-Ply evaluation that considers both regular chess moves and transplant moves.
     */
    public static void performHeuristicMove(Game game) {
        // Basic checks
        if (game.isGameOver()) {
            System.out.println("DEBUG: AI move aborted: Game is over.");
            return;
        }
        if (game.getGameMode() != Mode.PLAYER_VS_COMPUTER) {
            System.out.println("DEBUG: AI move aborted: Not in PLAYER_VS_COMPUTER mode.");
            return;
        }
        if (Game.isAiIsWhite() != Game.player) {
            System.out.println("DEBUG: AI move aborted: It's not AI's turn.");
            return;
        }

        System.out.println("DEBUG: AI is playing as: " + (Game.isAiIsWhite() ? "White" : "Black"));

        // Set a delay of one second before executing the move.
        Timer timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ((Timer) e.getSource()).stop();
                Board board = game.getBoard();

                // Collect all candidate moves (both regular moves and transplant moves)
                List<MoveCandidate> candidates = getAllMoveCandidates(game);
                if (candidates.isEmpty()) {
                    System.out.println("DEBUG: AI has no candidate moves. Checking for mate/stalemate.");
                    Game.checkMate();
                    return;
                }

                List<CandidateScorePair> candidateScores = new ArrayList<>();
                System.out.println("DEBUG: AI is analyzing " + candidates.size() + " candidate moves...");

                // For each candidate, clone the board, apply the move, and evaluate the position.
                for (MoveCandidate candidate : candidates) {
                    Board clonedBoard = board.clone(); // deep cloning
                    candidate.apply(clonedBoard);
                    int score = EvaluationUtils.evaluateBoard(clonedBoard, Game.isAiIsWhite());

                    // If candidate is a transplant move, add bonus based on piece value,
                    // then check if the transplanted piece is immediately vulnerable to capture.
                    if (candidate instanceof TransplantMoveCandidate) {
                        TransplantMoveCandidate trans = (TransplantMoveCandidate) candidate;
                        int pieceValue = getPieceValue(trans.getPieceName());
                        int bonus = pieceValue * BONUS_FACTOR;

                        // Check vulnerability: if the transplanted piece can be captured immediately,
                        // subtract an extra penalty equal to the bonus.
                        Piece transplanted = clonedBoard.getPiece(trans.getX(), trans.getY());
                        if (transplanted != null) {
                            boolean vulnerable = false;
                            // Only iterate enemy pieces instead of all board squares
                            for (Piece enemy : clonedBoard.getAllPieces()) { 
                                if (enemy.isWhite() != transplanted.isWhite()) {
                                    enemy.fillAllPseudoLegalMoves(clonedBoard);
                                    for (Move m : enemy.getMoves()) {
                                        if (m.getToX() == trans.getX() && m.getToY() == trans.getY()) {
                                            vulnerable = true;
                                            break;
                                        }
                                    }
                                }
                                if (vulnerable) break;
                            }
                            if (vulnerable) {
                                bonus -= pieceValue * BONUS_FACTOR;
                                System.out.println("DEBUG: Transplanted " + trans.getPieceName() + " is immediately vulnerable. Penalty applied.");
                            }
                        }
                        score += bonus;
                        System.out.println("DEBUG: Adding transplant bonus " + bonus + " for " + trans.getPieceName());
                    }

                    System.out.println("DEBUG: " + candidate.getDescription() + " => score=" + score);
                    candidateScores.add(new CandidateScorePair(candidate, score));
                }

                // Sort candidate moves in descending order (best score first)
                Collections.sort(candidateScores);

                // Print summary of top 3 candidate moves
                System.out.println("DEBUG: Top 3 candidate moves:");
                for (int i = 0; i < Math.min(3, candidateScores.size()); i++) {
                    CandidateScorePair pair = candidateScores.get(i);
                    System.out.println("DEBUG: " + pair.candidate.getDescription() + " => score=" + pair.score);
                }

                // Select the best candidate – the first in the sorted list
                CandidateScorePair bestPair = candidateScores.get(0);
                System.out.println("DEBUG: AI selected candidate: " + bestPair.candidate.getDescription() + " with score " + bestPair.score);

                // Execute the selected move based on its type
                if (bestPair.candidate instanceof RegularMoveCandidate) {
                    RegularMoveCandidate reg = (RegularMoveCandidate) bestPair.candidate;
                    Move bestMove = reg.getMove();
                    Piece aiPiece = board.getPiece(bestMove.getFromX(), bestMove.getFromY());
                    if (aiPiece != null) {
                        System.out.println("DEBUG: AI is moving piece: " + aiPiece);
                        game.setActivePiece(aiPiece);
                        Piece capturedPiece = game.move(bestMove.getToX(), bestMove.getToY());
                        if (capturedPiece == null) {
                            System.out.println("DEBUG: AI move executed successfully (no capture).");
                        } else {
                            System.out.println("DEBUG: AI move executed successfully, capturing piece: " + capturedPiece);
                        }
                    } else {
                        System.out.println("DEBUG: Error: Starting piece is null.");
                    }
                } else if (bestPair.candidate instanceof TransplantMoveCandidate) {
                    TransplantMoveCandidate trans = (TransplantMoveCandidate) bestPair.candidate;
                    System.out.println("DEBUG: AI is transplanting: " + trans.getPieceName() +
                            " at (" + trans.getX() + "," + trans.getY() + ")");
                    game.transplantPiece(trans.getPieceName(), trans.getX(), trans.getY());
                }
            }
        });
        timer.setRepeats(false);
        timer.start();
    }

    private static int getPieceValue(String pieceName) {
        switch (pieceName) {
            case "Pawn":
                return 1;
            case "Knight":
                return 3;
            case "Bishop":
                return 3;
            case "Rook":
                return 5;
            case "Queen":
                return 9;
            case "King":
                return 100;
            default:
                return 1;
        }
    }

    private static List<MoveCandidate> getAllMoveCandidates(Game game) {
        Board board = game.getBoard();
        List<MoveCandidate> candidates = new ArrayList<>();

        // 1) Regular moves:
        for (Piece p : Game.AllPieces) {
            if (p.isWhite() == Game.isAiIsWhite()) {
                p.fillAllPseudoLegalMoves(board);
                for (Move move : p.getMoves()) {
                    if (EvaluationUtils.isMoveLegal(board, p, move)) {
                        candidates.add(new RegularMoveCandidate(move));
                    }
                }
            }
        }

        // 2) Transplant moves:
        ToolShed aiToolShed = Game.isAiIsWhite() ? game.getWhiteToolShed() : game.getBlackToolShed();
        for (String pieceName : aiToolShed.getAvailablePieceNames()) {
            int count = aiToolShed.getPieceCount(pieceName);
            if (count <= 0) continue;
            for (int row = 0; row < BOARD_SIZE ; row++) {
                for (int col = 0; col < BOARD_SIZE ; col++) {
                    if (pieceName.equals("Pawn") && (col == 0 || col == 7)) continue;
                    if (board.getPiece(row, col) == null) {
                        TransplantMoveCandidate candidate = new TransplantMoveCandidate(pieceName, row, col, Game.isAiIsWhite());
                        if (EvaluationUtils.isTransplantLegal(board, candidate)) {
                            candidates.add(candidate);
                        }
                    }
                }
            }
        }
        return candidates;
    }
}
