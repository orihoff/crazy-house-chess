package com.chessgame.Ai;

import com.chessgame.Board.Board;
import com.chessgame.Board.Move;
import com.chessgame.Pieces.Piece;

/**
 * Represents a regular chess move candidate.
 */
public class RegularMoveCandidate implements MoveCandidate {
    private Move move;
    private int fromX, fromY;

    // Constructor that takes a Move object
    public RegularMoveCandidate(Move move) {
        this.move = move;
        this.fromX = move.getFromX();
        this.fromY = move.getFromY();
    }

    @Override
    public void apply(Board board) {
        // Get the piece from the board at the starting coordinates and make the move
        Piece piece = board.getPiece(fromX, fromY);
        if (piece != null) {
            piece.makeMove(move.getToX(), move.getToY(), board);
        }
    }

    @Override
    public String getDescription() {
        return "Regular move from (" + fromX + "," + fromY + ") to (" 
                + move.getToX() + "," + move.getToY() + ")";
    }
    
    // Getter for the move field
    public Move getMove() {
        return move;
    }
}
