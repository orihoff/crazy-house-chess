package com.chessgame.Pieces;

import com.chessgame.Board.Board;
import com.chessgame.Board.Move;
import com.chessgame.Game.Game;

public class King extends Piece {
    private boolean hasMoved;
    private Rook rook = null;

    private static final int[][] KING_PST = {
        { -3, -4, -4, -5, -5, -4, -4, -3 },
        { -3, -4, -4, -5, -5, -4, -4, -3 },
        { -3, -4, -4, -5, -5, -4, -4, -3 },
        { -3, -4, -4, -5, -5, -4, -4, -3 },
        { -2, -3, -3, -4, -4, -3, -3, -2 },
        { -1, -2, -2, -2, -2, -2, -2, -1 },
        {  2,  2,  0,  0,  0,  0,  2,  2 },
        {  2,  3,  1,  0,  0,  1,  3,  2 }
    };

    public King(int x, int y, boolean iswhite, Board board, int value) {
        super(x, y, iswhite, board, value);
        hasMoved = false;
        this.pieceImage = PieceImages.KING;
    }

    @Override
    public void intializeSide(int value) {
        super.intializeSide(value);
        if (isWhite()) {
            image = PieceImages.wk;
        } else {
            image = PieceImages.bk;
        }
    }

    @Override
    public int getPositionBonus(int row, int col) {
        int realRow = isWhite() ? row : (7 - row);
        return KING_PST[realRow][col];
    }

    @Override
    public boolean makeMove(int x, int y, Board board) {
        Move move = new Move(xCord, yCord, x, y, this);
        if (!alive()) {
            return false;
        }
        for (Move m: moves) {
            if (m.compareTo(move) == 0) {
                getRook(x, board);
                board.updatePieces(xCord, yCord, x, y, this);
                xCord = x;
                yCord = y;
                if (rook != null && !this.hasMoved && !rook.HasMoved()) {
                    if (x == rook.getXcord() - 1 || x == rook.getXcord() + 2) {
                        rook.castleDone(xCord, board);
                    }
                }
                hasMoved = true;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canMove(int x, int y, Board board) {
        int i = Math.abs(xCord - x);
        int j = Math.abs(yCord - y);

        // king's usual move: 1 step in any direction
        if ((j == 1 && i == 1) || (i + j) == 1) {
            if (board.getPiece(x, y) == null) {
                return true;
            } else {
                return board.getPiece(x, y).isWhite() != isWhite();
            }
        }

        // castling logic
        getRook(x, board);
        if (rook != null && (rook.HasMoved() || this.hasMoved)) {
            return false;
        } else if (rook != null) {
            // check squares in between
            if (x > xCord) {
                // short castling
                for (int k = xCord + 1; k < rook.getXcord(); k++) {
                    if (board.getPiece(k, yCord) != null) return false;
                    // check if squares are attacked
                    for (Move m: Game.allEnemysMove) {
                        if ((m.getToX() == k || m.getToX() == xCord) && m.getToY() == yCord) {
                            return false;
                        }
                    }
                }
                if (x == rook.getXcord() - 1 && y == yCord) {
                    return true;
                }
            } else {
                // long castling
                for (int k = xCord - 1; k > rook.getXcord(); k--) {
                    if (board.getPiece(k, yCord) != null) return false;
                    for (Move m: Game.allEnemysMove) {
                        if ((m.getToX() == k || m.getToX() == xCord) && m.getToY() == yCord) {
                            return false;
                        }
                    }
                }
                if (x == rook.getXcord() + 2 && y == yCord) {
                    return true;
                }
            }
        }
        return false;
    }

    private void getRook(int x, Board board) {
        rook = null;
        if (isWhite()) {
            if (x >= xCord) {
                if (board.getPiece(7, 7) instanceof Rook) {
                    rook = (Rook) board.getPiece(7, 7);
                }
            } else {
                if (board.getPiece(0, 7) instanceof Rook) {
                    rook = (Rook) board.getPiece(0, 7);
                }
            }
        } else {
            if (x >= xCord) {
                if (board.getPiece(7, 0) instanceof Rook) {
                    rook = (Rook) board.getPiece(7, 0);
                }
            } else {
                if (board.getPiece(0, 0) instanceof Rook) {
                    rook = (Rook) board.getPiece(0, 0);
                }
            }
        }
    }

    /**
     * Check if this king is in check by any of the opponent's pseudo-legal moves
     */
    public boolean isInCheck() {
        for (Move m: Game.allEnemysMove) {
            if (m.getToX() == xCord && m.getToY() == yCord) {
                return true;
            }
        }
        return false;
    }

    /**
     * Polymorphic piece-type index for array-based counters.
     */
    @Override
    public int getPieceTypeIndex() {
        return KING_INDEX;
    }

    @Override
    public String toString() {
        return "King{" +
                "xCord=" + xCord +
                ", yCord=" + yCord +
                ", isWhite=" + isWhite +
                ", hasMoved=" + hasMoved +
                '}';
    }
}
