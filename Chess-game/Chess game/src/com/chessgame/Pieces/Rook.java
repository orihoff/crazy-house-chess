package com.chessgame.Pieces;

import com.chessgame.Board.Board;

public class Rook extends Piece {

    private static final int[][] ROOK_PST = {
        {  0,  0,  0,  0,  0,  0,  0,  0 },
        {  0,  0,  0,  0,  0,  0,  0,  0 },
        {  0,  0,  0,  0,  0,  0,  0,  0 },
        {  0,  0,  0,  0,  0,  0,  0,  0 },
        {  0,  0,  0,  0,  0,  0,  0,  0 },
        {  0,  0,  0,  0,  0,  0,  0,  0 },
        {  5,  5,  5,  5,  5,  5,  5,  5 },
        {  0,  0,  0,  2,  2,  0,  0,  0 }
    };

    private boolean hasMoved;
    private boolean justMoved = false;

    public Rook(int x, int y, boolean iswhite, Board board, int value) {
        super(x, y, iswhite, board, value);
        hasMoved = false;
        this.pieceImage = PieceImages.ROOK;
    }

    @Override
    public void intializeSide(int value) {
        super.intializeSide(value);
        if (isWhite()) {
            image = PieceImages.wr;
        } else {
            image = PieceImages.br;
        }
    }

    @Override
    public int getPositionBonus(int row, int col) {
        int realRow = isWhite() ? row : (7 - row);
        return ROOK_PST[realRow][col];
    }

    @Override
    public boolean makeMove(int toX, int toY, Board board) {
        if (super.makeMove(toX, toY, board)) {
            if (!hasMoved) {
                justMoved = true;
            } else {
                justMoved = false;
            }
            hasMoved = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean canMove(int x, int y, Board board) {
        if (board.getPiece(x, y) != null && board.getPiece(x, y).isWhite() == isWhite()) {
            return false;
        }

        // Move vertically
        if (x == xCord) {
            int start = Math.min(yCord, y) + 1;
            int end   = Math.max(yCord, y);
            for (int i = start; i < end; i++) {
                if (board.getPiece(x, i) != null) return false;
            }
            return true;
        }
        // Move horizontally
        if (y == yCord) {
            int start = Math.min(xCord, x) + 1;
            int end   = Math.max(xCord, x);
            for (int i = start; i < end; i++) {
                if (board.getPiece(i, y) != null) return false;
            }
            return true;
        }

        return false;
    }

    public void castleDone(int x, Board board) {
        if (x == 6) {
            board.updatePieces(xCord, yCord, x - 1, yCord, this);
            xCord = x - 1;
        } else {
            board.updatePieces(xCord, yCord, x + 1, yCord, this);
            xCord = x + 1;
        }
        hasMoved = true;
    }

    public boolean HasMoved() {
        return hasMoved;
    }

    public void setHasMoved(boolean hasMoved) {
        this.hasMoved = hasMoved;
    }

    public boolean isJustMoved() {
        return justMoved;
    }

    public void setJustMoved(boolean justMoved) {
        this.justMoved = justMoved;
    }

    /**
     * Polymorphic piece-type index for array-based counters.
     */
    @Override
    public int getPieceTypeIndex() {
        return ROOK_INDEX;
    }

    @Override
    public String toString() {
        return "Rook{" +
                "xCord=" + xCord +
                ", yCord=" + yCord +
                ", isWhite=" + isWhite +
                ", hasMoved=" + hasMoved +
                '}';
    }
}
