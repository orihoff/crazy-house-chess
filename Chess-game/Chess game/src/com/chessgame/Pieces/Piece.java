package com.chessgame.Pieces;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import com.chessgame.Board.Board;
import com.chessgame.Board.Move;
import com.chessgame.Game.Game;

public abstract class Piece implements Cloneable {
    //------------------------------------------------------------------------
    // Piece-type indices for array-based counters
	public static final int PAWN_INDEX   = 0;
    public static final int KNIGHT_INDEX = 1;
    public static final int BISHOP_INDEX = 2;
    public static final int ROOK_INDEX   = 3;
    public static final int QUEEN_INDEX  = 4;
    public static final int KING_INDEX   = 5;
    //------------------------------------------------------------------------

    /**
     * Each concrete subclass must override this to return its own piece-type index:
     *   Pawn   → PAWN_INDEX
     *   Knight → KNIGHT_INDEX
     *   Bishop → BISHOP_INDEX
     *   Rook   → ROOK_INDEX
     *   Queen  → QUEEN_INDEX
     */
    public abstract int getPieceTypeIndex();

    protected int xCord;
    protected int yCord;
    protected boolean isWhite;
    protected boolean isAlive;
    protected int valueInTheBoard;
    protected Board board;
    protected String pieceImage;
    protected Color pieceColor;
    public static int size = 80;
    protected List<Move> moves = new ArrayList<>();
    protected ImageIcon image;

    // Constructor
    public Piece(int x, int y, boolean isWhite, Board board, int value) {
        this.xCord = x;
        this.yCord = y;
        this.isWhite = isWhite;
        this.isAlive = true;
        this.board = board;
        intializeSide(value);
        board.setPieceIntoBoard(x, y, this);
    }
    
    /**
     * A method that each derived piece will override to initialize side, color, and image.
     */
    public void intializeSide(int value) {
        if (isWhite) {
            pieceColor = PieceImages.WHITECOLOR;
        } else {
            pieceColor = PieceImages.BLACKCOLOR;
        }
        valueInTheBoard = value;
    }

    /**
     * The default PST-based position bonus method.
     * By default returns 0 – each piece class will override if it has a PST.
     */
    public int getPositionBonus(int row, int col) {
        return 0; // By default, no PST bonus
    }

    // Abstract or default check if a piece can move to (x,y)
    public abstract boolean canMove(int x, int y, Board board);

    // Make a move if it is in the piece's move list
    public boolean makeMove(int toX, int toY, Board board) {
        Move move = new Move(xCord, yCord, toX, toY, this);
        if (!alive()) {
            return false;
        }
        for (Move m : moves) {
            if (m.compareTo(move) == 0) {
                board.updatePieces(xCord, yCord, toX, toY, this);
                this.xCord = toX;
                this.yCord = toY;
                return true;
            }
        }
        return false;
    }

    // Check if piece is blocked
    public boolean isBlocked(Board board) {
        for (Move move : moves) {
            int toX = move.getToX();
            int toY = move.getToY();
            Piece targetPiece = board.getPiece(toX, toY);
            if (targetPiece != null && targetPiece.isWhite() != this.isWhite()) {
                return false;  // has an enemy piece => not blocked
            }
        }
        return true;
    }

    // Check if the piece is alive
    @SuppressWarnings("unlikely-arg-type")
    public boolean alive() {
        Piece currentPiece = board.getPiece(xCord, yCord);
        if (currentPiece == null || currentPiece.getValueInTheboard() != valueInTheBoard) {
            isAlive = false;
            Game.AllPieces.remove(this);
        }
        return isAlive;
    }

    public void setAlive(boolean isAlive) {
        this.isAlive = isAlive;
    }

    // Toggle color (unused typically, but might be used in some logic)
    public void changeColor() {
        this.isWhite = !this.isWhite;
    }

    // Show moves graphically
    public void showMoves(Graphics2D g2, JPanel panel, boolean isUserWhite) {
        for (Move m : moves) {
            int fromX = m.getFromX();
            int fromY = m.getFromY();
            int toX   = m.getToX();
            int toY   = m.getToY();

            // Flip for display if user is black
            int drawXFrom = isUserWhite ? fromX : 7 - fromX;
            int drawYFrom = isUserWhite ? fromY : 7 - fromY;
            int drawXTo   = isUserWhite ? toX   : 7 - toX;
            int drawYTo   = isUserWhite ? toY   : 7 - toY;

            // Check if capturing
            Piece destinationPiece = board.getPiece(toX, toY);
            if (destinationPiece != null && destinationPiece.isWhite() != isWhite()) {
                g2.setColor(Color.RED);
                g2.drawRect(drawXTo * size, drawYTo * size, size, size);
            } else {
                g2.setColor(Color.DARK_GRAY);
                g2.fillOval(
                    (drawXTo * size) + size / 3,
                    (drawYTo * size) + size / 3,
                    size / 3,
                    size / 3
                );
            }

            // The piece's current square
            if (Game.drag) {
                g2.setColor(Color.DARK_GRAY);
                g2.fillRect(drawXFrom * size, drawYFrom * size, size, size);
            } else {
                g2.setColor(Color.DARK_GRAY);
                g2.drawRect(drawXFrom * size, drawYFrom * size, size, size);
            }
        }

        panel.revalidate();
        panel.repaint();
    }

    // Draw piece
    public void draw(Graphics g, boolean drag, JPanel panel) {
        g.drawImage(image.getImage(), xCord * size, yCord * size, size, size, panel);
        panel.revalidate();
        panel.repaint();
    }

    // Draw piece while dragging
    public void drawDrag(Graphics g, boolean player, int x, int y, JPanel panel) {
        g.drawImage(image.getImage(), x - size / 2, y - size / 2, size, size, panel);
        panel.revalidate();
        panel.repaint();
    }

    // Fill all pseudo-legal moves (no check for king safety, etc.)
    public void fillAllPseudoLegalMoves(Board b) {
        moves = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (canMove(i, j, b)) {
                    moves.add(new Move(xCord, yCord, i, j, this));
                }
            }
        }
    }

    // Getters & setters
    public int getXcord() { return xCord; }
    public void setXcord(int xcord) { this.xCord = xcord; }

    public int getYcord() { return yCord; }
    public void setYcord(int yCord) { this.yCord = yCord; }

    public boolean isWhite() { return isWhite; }
    public void setWhite(boolean isWhite) { this.isWhite = isWhite; }

    public Board getBoard() { return board; }
    public void setBoard(Board board) { this.board = board; }

    public void setValueInTheboard(int value) { this.valueInTheBoard = value; }
    public int getValueInTheboard() { return valueInTheBoard; }

    public List<Move> getMoves() { return moves; }
    public void setMoves(List<Move> moves) { this.moves = moves; }

    @Override
    public Piece clone() {
        try {
            Piece clonedPiece = (Piece) super.clone();
            clonedPiece.moves = new ArrayList<>(this.moves);
            return clonedPiece;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Clone not supported", e);
        }
    }
}
