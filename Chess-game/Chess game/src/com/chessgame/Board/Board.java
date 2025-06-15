package com.chessgame.Board;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.chessgame.Game.Game;
import com.chessgame.Pieces.Piece;

/**
 * Represents a chess board.
 */
public class Board implements Cloneable {
    public static final int ROWS = 8;
    public static final int COLUMNS = 8;

    // Stores pieces by key (x,y)
    private Map<String, Piece> boardMap;

    // Constructor
    public Board() {
        boardMap = new HashMap<>();
    }

    // Helper method to create a key for the HashMap
    private String getKey(int x, int y) {
        return x + "," + y;
    }

    // Sets a piece on the board at the given coordinates.
    public void setPieceIntoBoard(int x, int y, Piece piece) {
        String key = getKey(x, y);
        if (piece != null) {
            boardMap.put(key, piece);
            // Optionally, update the piece's coordinates
            piece.setXcord(x);
            piece.setYcord(y);
        } else {
            boardMap.remove(key);
        }
    }

    // Retrieves a piece from the board at the specified coordinates.
    public Piece getPiece(int x, int y) {
        return boardMap.get(getKey(x, y));
    }

    // Removes a piece from the board at the given coordinates.
    public void removePieceAt(int x, int y) {
        String key = getKey(x, y);
        boardMap.remove(key);
    }

    // Moves a piece from one position to another (without creating a Move object)
    public void movePiece(int fromX, int fromY, int toX, int toY) {
        Piece piece = getPiece(fromX, fromY);
        if (piece != null) {
            setPieceIntoBoard(toX, toY, piece);
            removePieceAt(fromX, fromY);
        }
    }

    /**
     * Updates the board state after an "official" move.
     * Creates a Move object, stores it in the move stack,
     * removes the captured piece (if any), and moves the piece.
     *
     * @param fromX starting row
     * @param fromY starting column
     * @param toX   destination row
     * @param toY   destination column
     * @param piece the piece being moved
     */
    public void updatePieces(int fromX, int fromY, int toX, int toY, Piece piece) {
        // 1. Create a new move (if needed in the Move stack)
        Move move = new Move(fromX, fromY, toX, toY, piece);

        // 2. Check if there is a piece on the destination square (captured piece)
        Piece captured = getPiece(toX, toY);
        move.setCapturedPiece(captured);

        // 3. Remove the captured piece from the board (if it exists)
        if (captured != null) {
            Game.AllPieces.remove(captured);
            removePieceAt(toX, toY);
        }

        // 4. Remove the original piece from its previous position and place it in the new square
        removePieceAt(fromX, fromY);
        setPieceIntoBoard(toX, toY, piece);
    }

    /**
     * Overrides the default clone method to perform a deep copy of the board.
     * Uses the getClone() method of each Piece so that every piece is cloned properly.
     *
     * @return a new cloned Board instance
     */
    @Override
    public Board clone() {
        try {
            Board clonedBoard = (Board) super.clone();
            // Create a new HashMap for the cloned board
            clonedBoard.boardMap = new HashMap<>();
            // Deep clone each piece
            for (Map.Entry<String, Piece> entry : boardMap.entrySet()) {
                Piece originalPiece = entry.getValue();
                Piece clonedPiece = originalPiece.clone();
                // Use setPieceIntoBoard to ensure coordinates are updated correctly
                clonedBoard.setPieceIntoBoard(clonedPiece.getXcord(), clonedPiece.getYcord(), clonedPiece);
            }
            return clonedBoard;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Clone not supported", e);
        }
    }
    
    public static int getSize() {
        return ROWS;
    }

    
    /**
     * Prints the board (for debugging purposes).
     */
    public void printBoard() {
        for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < COLUMNS; x++) {
                Piece piece = getPiece(x, y);
                System.out.print((piece != null ? piece.getValueInTheboard() : ".") + " ");
            }
            System.out.println();
        }
    }

    /**
     * Returns a collection of all pieces currently on the board.
     */
    public Collection<Piece> getAllPieces() {
        return boardMap.values();
    }
    
    
}
