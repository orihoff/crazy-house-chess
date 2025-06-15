package com.chessgame.Pieces;

import com.chessgame.Board.Board;

public class Knight extends Piece {

    private static final int[][] KNIGHT_PST = {
        { -5, -4, -2, -2, -2, -2, -4, -5 },
        { -4, -2,  0,  0,  0,  0, -2, -4 },
        { -2,  0,  1,  2,  2,  1,  0, -2 },
        { -2,  0,  2,  3,  3,  2,  0, -2 },
        { -2,  0,  2,  3,  3,  2,  0, -2 },
        { -2,  0,  1,  2,  2,  1,  0, -2 },
        { -4, -2,  0,  0,  0,  0, -2, -4 },
        { -5, -4, -3, -2, -2, -3, -4, -5 }
    };

    public Knight(int x, int y, boolean iswhite, Board board, int value) {
        super(x, y, iswhite, board, value);
        this.pieceImage = PieceImages.KNIGHT;
    }

    @Override
    public void intializeSide(int value) {
        super.intializeSide(value);
        if (isWhite()) {
            image = PieceImages.wn;
        } else {
            image = PieceImages.bn;
        }
    }

    @Override
    public int getPositionBonus(int row, int col) {
        int realRow = isWhite() ? row : (7 - row);
        return KNIGHT_PST[realRow][col];
    }

    @Override
    public boolean canMove(int x, int y, Board board) {
        // cannot capture same color
        if (board.getPiece(x, y) != null && board.getPiece(x, y).isWhite() == isWhite()) {
            return false;
        }
        // Knight moves
        if (x == xCord + 1 && y == yCord - 2) return true;
        if (x == xCord - 1 && y == yCord - 2) return true;
        if (x == xCord - 1 && y == yCord + 2) return true;
        if (x == xCord + 1 && y == yCord + 2) return true;
        if (x == xCord + 2 && y == yCord - 1) return true;
        if (x == xCord + 2 && y == yCord + 1) return true;
        if (x == xCord - 2 && y == yCord - 1) return true;
        if (x == xCord - 2 && y == yCord + 1) return true;
        return false;
    }

    @Override
    public int getPieceTypeIndex() {
        return KNIGHT_INDEX;
    }

    @Override
    public String toString() {
        return "Knight{" +
                "xCord=" + xCord +
                ", yCord=" + yCord +
                ", isWhite=" + isWhite +
                '}';
    }
}
