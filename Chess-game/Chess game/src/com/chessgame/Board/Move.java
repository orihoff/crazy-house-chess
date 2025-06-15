package com.chessgame.Board;

import com.chessgame.Pieces.Piece;
import com.chessgame.Game.ToolShed;

/**
 * Represents a single move in the game, including information about:
 *  - From and to coordinates (fromX, fromY, toX, toY)
 *  - Which piece moved (piece)
 *  - Which piece was captured (capturedPiece), if any
 *  - To which ToolShed the captured piece is added (toolShed), if applicable
 *  - Optionally: whether the move is a transplant move (transplantMove)
 *    and which piece was transplanted (transplantedPieceName)
 */
public class Move implements Comparable<Move> {
    
    private int fromX, fromY;
    private int toX, toY;
    private Piece piece;           // The piece that moved
    private Piece capturedPiece;   // The captured piece (if any)
    private ToolShed toolShed;     // The tool shed of the capturing player (if applicable)

    // --- Optional fields in case of a "transplant" move ---
    private boolean transplantMove;
    private String transplantedPieceName;

    /**
     * Full constructor - includes reference to the ToolShed.
     *
     * @param fromX    starting row coordinate
     * @param fromY    starting column coordinate
     * @param toX      destination row coordinate
     * @param toY      destination column coordinate
     * @param piece    the piece that is moving
     * @param toolShed the tool shed where a captured piece might be stored
     */
    public Move(int fromX, int fromY, int toX, int toY, Piece piece, ToolShed toolShed) {
        this.fromX = fromX;
        this.fromY = fromY;
        this.toX = toX;
        this.toY = toY;
        this.piece = piece;
        this.toolShed = toolShed;
    }

    /**
     * Constructor without ToolShed (if not needed).
     *
     * @param fromX starting row coordinate
     * @param fromY starting column coordinate
     * @param toX   destination row coordinate
     * @param toY   destination column coordinate
     * @param piece the piece that is moving
     */
    public Move(int fromX, int fromY, int toX, int toY, Piece piece) {
        this(fromX, fromY, toX, toY, piece, null);
    }

    //---------------------------------------------------------------------------------
    // Getters & Setters
    //---------------------------------------------------------------------------------

    public int getFromX() {
        return fromX;
    }

    public void setFromX(int fromX) {
        this.fromX = fromX;
    }

    public int getFromY() {
        return fromY;
    }

    public void setFromY(int fromY) {
        this.fromY = fromY;
    }

    public int getToX() {
        return toX;
    }

    public void setToX(int toX) {
        this.toX = toX;
    }

    public int getToY() {
        return toY;
    }

    public void setToY(int toY) {
        this.toY = toY;
    }

    public Piece getPiece() {
        return piece;
    }

    public void setPiece(Piece piece) {
        this.piece = piece;
    }

    public Piece getCapturedPiece() {
        return capturedPiece;
    }

    public void setCapturedPiece(Piece capturedPiece) {
        this.capturedPiece = capturedPiece;
    }

    public ToolShed getToolShed() {
        return toolShed;
    }

    public void setToolShed(ToolShed toolShed) {
        this.toolShed = toolShed;
    }

    public boolean isTransplantMove() {
        return transplantMove;
    }

    public void setTransplantMove(boolean transplantMove) {
        this.transplantMove = transplantMove;
    }

    public String getTransplantedPieceName() {
        return transplantedPieceName;
    }

    public void setTransplantedPieceName(String transplantedPieceName) {
        this.transplantedPieceName = transplantedPieceName;
    }

    //---------------------------------------------------------------------------------
    // Helper Methods
    //---------------------------------------------------------------------------------

    /**
     * Helper method that applies the move on the board,
     * and adds the captured piece (if any) to the tool shed (if a toolShed exists).
     *
     * @param board the chess board on which to perform the move
     */
    public void makeMove(Board board) {
        // Check for a captured piece
        capturedPiece = board.getPiece(toX, toY);
        if (capturedPiece != null) {
            board.removePieceAt(toX, toY); // Remove the captured piece from the board
            if (toolShed != null) {
                toolShed.addCapturedPiece(capturedPiece); // Add captured piece to the tool shed
            }
        }
        // Move the piece
        board.movePiece(fromX, fromY, toX, toY);
    }

    @Override
    public int compareTo(Move other) {
        // Basic comparison based on the destination position (or another criterion)
        if (this.toX == other.getToX() && this.toY == other.getToY()) {
            return 0;
        }
        return -1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Move otherM = (Move) o;
        return (this.toX == otherM.getToX()
                && this.toY == otherM.getToY()
                && this.fromX == otherM.getFromX()
                && this.fromY == otherM.getFromY());
    }

    /**
     * Overrides toString() to display move information.
     *
     * @return a string representation of the move, including capture information if applicable
     */
    @Override
    public String toString() {
        // If piece exists, use its class name; otherwise "UnknownPiece"
        String pieceName = (piece != null) ? piece.getClass().getSimpleName() : "UnknownPiece";

        // Optionally add information about the captured piece, if any
        String captureInfo = "";
        if (capturedPiece != null) {
            String capturedName = capturedPiece.getClass().getSimpleName();
            captureInfo = " [captured " + capturedName + "]";
        }

        return pieceName 
                + " from (" + fromX + "," + fromY + ") "
                + "to (" + toX + "," + toY + ")"
                + captureInfo;
    }
}
