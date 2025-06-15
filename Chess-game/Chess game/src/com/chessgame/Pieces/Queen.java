package com.chessgame.Pieces;

import com.chessgame.Board.Board;

public class Queen extends Piece {

    private static final int[][] QUEEN_PST = {
        { -2, -1, -1,  0,  0, -1, -1, -2 },
        { -1,  0,  0,  0,  0,  0,  0, -1 },
        { -1,  0,  1,  1,  1,  1,  0, -1 },
        {  0,  0,  1,  2,  2,  1,  0,  0 },
        {  0,  0,  1,  2,  2,  1,  0,  0 },
        { -1,  0,  1,  1,  1,  1,  0, -1 },
        { -1,  0,  0,  0,  0,  0,  0, -1 },
        { -2, -1, -1,  0,  0, -1, -1, -2 }
    };

    public Queen(int x, int y, boolean iswhite, Board board, int value) {
        super(x, y, iswhite, board, value);
        this.pieceImage = PieceImages.QUEEN;
    }

    @Override
    public void intializeSide(int value) {
        super.intializeSide(value);
        if (isWhite()) {
            image = PieceImages.wq;
        } else {
            image = PieceImages.bq;
        }
    }

    @Override
    public int getPositionBonus(int row, int col) {
        int realRow = isWhite() ? row : (7 - row);
        return QUEEN_PST[realRow][col];
    }

    @Override
    public boolean canMove(int x, int y, Board board) {
        // cannot capture same color
        if (board.getPiece(x, y) != null && board.getPiece(x, y).isWhite() == isWhite()) {
            return false;
        }
        // Diagonal
        if (Math.abs(x - xCord) == Math.abs(y - yCord)) {
            return queenMovesDiagonal(x, y, board);
        }
        // Straight
        if (x == xCord || y == yCord) {
            return queenMovesStraight(x, y, board);
        }
        return false;
    }

    /**
     * נחזיר את המתודה הישנה במלואה:
     */
    public boolean queenMovesStraight(int x, int y, Board board) {
        if (x == xCord && (y < yCord)) {
            for (int i = yCord - 1; i > y; i--) {
                if (board.getPiece(x, i) != null) {
                    return false;
                }
            }
            return true;
        }
        if (x == xCord && (y > yCord)) {
            for (int i = yCord + 1; i < y; i++) {
                if (board.getPiece(x, i) != null) {
                    return false;
                }
            }
            return true;
        }
        if (y == yCord && (x > xCord)) {
            for (int i = xCord + 1; i < x; i++) {
                if (board.getPiece(i, y) != null) {
                    return false;
                }
            }
            return true;
        }
        if (y == yCord && (x < xCord)) {
            for (int i = xCord - 1; i > x; i--) {
                if (board.getPiece(i, y) != null) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * ומחזיר גם את queenMovesDiagonal המקורית:
     */
    public boolean queenMovesDiagonal(int x, int y, Board board) {
        if (x > xCord && y > yCord) {
            int j = yCord + 1;
            for (int i = xCord + 1; i < x; i++) {
                if (board.getPiece(i, j) != null) {
                    return false;
                }
                j++;
            }
        } else if (x < xCord && y < yCord) {
            int j = yCord - 1;
            for (int i = xCord - 1; i > x; i--) {
                if (board.getPiece(i, j) != null) {
                    return false;
                }
                j--;
            }
        } else if (x > xCord && y < yCord) {
            int j = yCord - 1;
            for (int i = xCord + 1; i < x; i++) {
                if (board.getPiece(i, j) != null) {
                    return false;
                }
                j--;
            }
        } else if (x < xCord && y > yCord) {
            int j = yCord + 1;
            for (int i = xCord - 1; i > x; i--) {
                if (board.getPiece(i, j) != null) {
                    return false;
                }
                j++;
            }
        }
        return true;
    }

    @Override
    public int getPieceTypeIndex() {
        return QUEEN_INDEX;
    }

    @Override
    public String toString() {
        return "Queen{" +
               "xCord=" + xCord +
               ", yCord=" + yCord +
               ", isWhite=" + isWhite +
               '}';
    }
}
