package com.chessgame.Pieces;

import com.chessgame.Board.Board;

public class Bishop extends Piece {

    private static final int[][] BISHOP_PST = {
        { -2, -1, -1, -1, -1, -1, -1, -2 },
        { -1,  0,  0,  0,  0,  0,  0, -1 },
        { -1,  0,  1,  1,  1,  1,  0, -1 },
        { -1,  0,  1,  2,  2,  1,  0, -1 },
        { -1,  0,  1,  2,  2,  1,  0, -1 },
        { -1,  0,  1,  1,  1,  1,  0, -1 },
        { -1,  0,  0,  0,  0,  0,  0, -1 },
        { -2, -1, -1, -1, -1, -1, -1, -2 }
    };

    public Bishop(int x, int y, boolean iswhite, Board board, int value) {
        super(x, y, iswhite, board, value);
        this.pieceImage = PieceImages.BISHOP;
    }

    @Override
    public void intializeSide(int value) {
        super.intializeSide(value);
        if (isWhite()) {
            image = PieceImages.wb;
        } else {
            image = PieceImages.bb;
        }
    }

    @Override
    public int getPositionBonus(int row, int col) {
        int realRow = isWhite() ? row : (7 - row);
        return BISHOP_PST[realRow][col];
    }

    @Override
    public boolean canMove(int x, int y, Board board) {
        if (board.getPiece(x, y) != null && board.getPiece(x, y).isWhite() == isWhite()) {
            return false;
        }
        if (Math.abs(x - xCord) == Math.abs(y - yCord)) {
            return bishopMoves(x, y, board);
        }
        return false;
    }

    public boolean bishopMoves(int x, int y, Board board) {
        if (x > xCord && y > yCord) {
            int j = yCord + 1;
            for (int i = xCord + 1; i < x; i++, j++) {
                if (board.getPiece(i, j) != null) return false;
            }
        } else if (x < xCord && y < yCord) {
            int j = yCord - 1;
            for (int i = xCord - 1; i > x; i--, j--) {
                if (board.getPiece(i, j) != null) return false;
            }
        } else if (x > xCord && y < yCord) {
            int j = yCord - 1;
            for (int i = xCord + 1; i < x; i++, j--) {
                if (board.getPiece(i, j) != null) return false;
            }
        } else if (x < xCord && y > yCord) {
            int j = yCord + 1;
            for (int i = xCord - 1; i > x; i--, j++) {
                if (board.getPiece(i, j) != null) return false;
            }
        }
        return true;
    }

    /**
     * Polymorphic piece-type index for array-based counters.
     */
    @Override
    public int getPieceTypeIndex() {
        return BISHOP_INDEX;
    }

    @Override
    public String toString() {
        return "Bishop{" +
               "xCord=" + xCord +
               ", yCord=" + yCord +
               ", isWhite=" + isWhite +
               ", valueInTheBoard=" + valueInTheBoard +
               '}';
    }
}
