package com.chessgame.Ai;

import com.chessgame.Board.Board;
import com.chessgame.Pieces.*;

/**
 * Implementation of a transplant move candidate – i.e., placing a piece from the ToolShed
 * into an empty square.
 */
public class TransplantMoveCandidate implements MoveCandidate {
    private String pieceName;  // For example: "Pawn", "Rook", "Knight", "Bishop", "Queen", "King"
    private int x, y;          // The board coordinates for the transplant
    private boolean isWhite;   // The color of the piece to be transplanted

    public TransplantMoveCandidate(String pieceName, int x, int y, boolean isWhite) {
        this.pieceName = pieceName;
        this.x = x;
        this.y = y;
        this.isWhite = isWhite;
    }

    @Override
    public void apply(Board board) {
        // "Transplant" the piece onto the board – similar to game.transplantPiece,
        // but here the move is applied to the cloned board for evaluation purposes only.
        Piece newPiece = createPieceInstance(pieceName, x, y, board, isWhite);
        board.setPieceIntoBoard(x, y, newPiece);
    }

    @Override
    public String getDescription() {
        return "Transplant " + pieceName + " at (" + x + "," + y + ")";
    }
    
    /**
     * Helper function to create an instance of a piece.
     *
     * @param pieceName the name of the piece to create
     * @param x         the row coordinate
     * @param y         the column coordinate
     * @param board     the board on which the piece will be placed
     * @param isWhite   the color of the piece
     * @return a new instance of the specified piece
     */
    private Piece createPieceInstance(String pieceName, int x, int y, Board board, boolean isWhite) {
        switch (pieceName) {
            case "Pawn": 
                return new Pawn(x, y, isWhite, board, isWhite ? 1 : -1);
            case "Rook":
                return new Rook(x, y, isWhite, board, isWhite ? 5 : -5);
            case "Knight":
                return new Knight(x, y, isWhite, board, isWhite ? 3 : -3);
            case "Bishop":
                return new Bishop(x, y, isWhite, board, isWhite ? 3 : -3);
            case "Queen":
                return new Queen(x, y, isWhite, board, isWhite ? 9 : -9);
            case "King":
                return new King(x, y, isWhite, board, isWhite ? 10 : -10);
            default:
                return new Pawn(x, y, isWhite, board, isWhite ? 1 : -1);
        }
    }

    public String getPieceName() {
        return pieceName;
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }

    // Added getter method for isWhite field
    public boolean isWhite() {
        return isWhite;
    }
}
