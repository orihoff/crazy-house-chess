
package com.chessgame.Pieces;

import com.chessgame.Board.Board;

public class PieceFactory {
    public static Piece createPiece(Class<? extends Piece> pieceClass, boolean isWhite) {
        if (pieceClass == Pawn.class) {
            return new Pawn(0, 0, isWhite, new Board(), isWhite ? 1 : -1);
        } else if (pieceClass == Rook.class) {
            return new Rook(0, 0, isWhite, new Board(), isWhite ? 5 : -5);
        } else if (pieceClass == Knight.class) {
            return new Knight(0, 0, isWhite, new Board(), isWhite ? 3 : -3);
        } else if (pieceClass == Bishop.class) {
            return new Bishop(0, 0, isWhite, new Board(), isWhite ? 3 : -3);
        } else if (pieceClass == Queen.class) {
            return new Queen(0, 0, isWhite, new Board(), isWhite ? 8 : -8);
        } else if (pieceClass == King.class) {
            return new King(0, 0, isWhite, new Board(), isWhite ? 10 : -10);
        }
        return null;
    }
}
