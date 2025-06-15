package com.chessgame.Pieces;

import com.chessgame.Board.Board;
import com.chessgame.Board.Move;
import com.chessgame.Game.Game;

public class Pawn extends Piece {
    private boolean firstMove;
    private boolean moved2Squares = false;

    // PST for Pawns
    private static final int[][] PAWN_PST = {
        {  0,   0,   0,   0,   0,   0,   0,   0 },
        {  5,   5,   5,   5,   5,   5,   5,   5 },
        {  1,   1,   2,   3,   3,   2,   1,   1 },
        {  0,   0,   0,   2,   2,   0,   0,   0 },
        {  0,   0,   0,  -2,  -2,   0,   0,   0 },
        { -1,  -1,  -2,   0,   0,  -2,  -1,  -1 },
        { -1,  -1,  -1,  -1,  -1,  -1,  -1,  -1 },
        {  0,   0,   0,   0,   0,   0,   0,   0 }
    };

    public Pawn(int x, int y, boolean iswhite, Board board, int value) {
        super(x, y, iswhite, board, value);
        firstMove = true;
        this.pieceImage = PieceImages.PAWN;
    }

    @Override
    public void intializeSide(int value) {
        super.intializeSide(value);
        if (isWhite()) {
            image = PieceImages.wp;
        } else {
            image = PieceImages.bp;
        }
    }

    @Override
    public int getPositionBonus(int row, int col) {
        int realRow = isWhite() ? row : (7 - row);
        return PAWN_PST[realRow][col];
    }

    @Override
    public boolean makeMove(int toX, int toY, Board board) {
        Move move = new Move(xCord, yCord, toX, toY, this);
        if (!alive()) {
            return false;
        }
        if (moves.contains(move)) {
            // En passant capture on the left
            if (toX == xCord + 1 && yCord - (isWhite ? 1 : -1) == toY && board.getPiece(toX, toY) == null) {
                Piece cap = board.getPiece(xCord + 1, yCord);
                Game.AllPieces.remove(cap);
                Game.fillPieces();
                board.setPieceIntoBoard(xCord + 1, yCord, null);
                Game.addCapturedPieceToToolShed(cap);
            }
            // En passant capture on the right
            if (toX == xCord - 1 && yCord - (isWhite ? 1 : -1) == toY && board.getPiece(toX, toY) == null) {
                Piece cap = board.getPiece(xCord - 1, yCord);
                Game.AllPieces.remove(cap);
                Game.fillPieces();
                board.setPieceIntoBoard(xCord - 1, yCord, null);
                Game.addCapturedPieceToToolShed(cap);
            }

            // Track double‐step for en passant
            if (firstMove && Math.abs(yCord - toY) == 2) {
                moved2Squares = true;
            }
            removeEnpassant();

            board.updatePieces(xCord, yCord, toX, toY, this);
            xCord = toX;
            yCord = toY;
            firstMove = false;
            return true;
        }
        return false;
    }

    private void removeEnpassant() {
        for (Piece p : Game.AllPieces) {
            if (p instanceof Pawn && p != this) {
                ((Pawn) p).setMoved2Squares(false);
            }
        }
    }

    public boolean madeToTheEnd() {
        return (isWhite && yCord == 0) || (!isWhite && yCord == 7);
    }

    @Override
    public boolean canMove(int x, int y, Board board) {
        int dir = isWhite ? -1 : 1;

        // same‐color block
        if (board.getPiece(x, y) != null && board.getPiece(x, y).isWhite() == isWhite()) {
            return false;
        }
        // forward move
        if (x == xCord && board.getPiece(x, y) == null) {
            if (firstMove && (y == yCord + 2*dir) && board.getPiece(x, yCord + dir) == null) {
                return true;
            }
            return y == yCord + dir;
        }
        // capture (including normal diagonal)
        return Math.abs(x - xCord) == 1 && y == yCord + dir &&
               board.getPiece(x, y) != null && board.getPiece(x, y).isWhite() != isWhite();
    }

    @Override
    public int getPieceTypeIndex() {
        return PAWN_INDEX;
    }

    // --- Accessors for firstMove and moved2Squares ---

    public boolean isFirstMove() {
        return firstMove;
    }

    public void setFirstMove(boolean firstMove) {
        this.firstMove = firstMove;
    }

    public boolean isMoved2Squares() {
        return moved2Squares;
    }

    public void setMoved2Squares(boolean moved2Squares) {
        this.moved2Squares = moved2Squares;
    }

    @Override
    public String toString() {
        return "Pawn{" +
               "xCord=" + xCord +
               ", yCord=" + yCord +
               ", isWhite=" + isWhite +
               ", firstMove=" + firstMove +
               ", moved2Squares=" + moved2Squares +
               '}';
    }
}
