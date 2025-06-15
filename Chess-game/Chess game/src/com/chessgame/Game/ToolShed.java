package com.chessgame.Game;

import com.chessgame.Board.Board;
import com.chessgame.Pieces.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

/**
 * The ToolShed for a specific player (White/Black).
 * It allows storing the count of opponent's captured pieces and transplanting them onto the board.
 * Note: The King is intentionally omitted since it cannot be captured or transplanted.
 */
public class ToolShed extends JPanel {
    private String playerColor;                      // The player's color (White/Black)
    private HashMap<String, Integer> pieceCountMap;  // Map that counts pieces by their name (String)
    private HashMap<String, JLabel> pieceLabelMap;   // Map of labels by piece name (String)

    // GUI components
    private JComboBox<String> pieceSelector;
    private JButton transplantButton;
    private JButton cancelTransplantButton;

    // The piece currently selected for transplant (if any)
    private String selectedPieceForTransplant;
    private Game game;

    /**
     * Constructs a ToolShed for a player with the given color in a specified game.
     *
     * @param playerColor the player's color (e.g., "White" or "Black")
     * @param game        the current game instance
     */
    public ToolShed(String playerColor, Game game) {
        this.playerColor = playerColor;
        this.game = game;
        this.pieceCountMap = new HashMap<>();
        this.pieceLabelMap = new HashMap<>();

        // Set the preferred size and appearance of the tool shed
        setPreferredSize(new Dimension(150, 600));
        // Set border based on player's color: White -> white border, Black -> black border.
        // The border thickness is set to 5 pixels.
        if (playerColor.equals("White")) {
            setBorder(BorderFactory.createLineBorder(Color.WHITE, 5));
        } else {
            setBorder(BorderFactory.createLineBorder(Color.BLACK, 5));
        }
        setBackground(Color.LIGHT_GRAY);

        // Initialize the internal structure: piece counts and labels (excluding King)
        initializeToolShed();
        initializeLabels();
        initializeTransplantControls();
    }

    /**
     * Returns the player's color (White/Black).
     *
     * @return the player's color as a String
     */
    public String getPlayerColor() {
        return playerColor;
    }

    /**
     * Initializes the piece count to 0 for each piece type, excluding King.
     */
    private void initializeToolShed() {
        pieceCountMap.put("Pawn",   0);
        pieceCountMap.put("Rook",   0);
        pieceCountMap.put("Knight", 0);
        pieceCountMap.put("Bishop", 0);
        pieceCountMap.put("Queen",  0);
        // Do not include King as it cannot be captured or transplanted.
    }

    /**
     * Creates labels for each piece type (excluding King) and adds them to the panel.
     */
    private void initializeLabels() {
        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new GridLayout(5, 1));

        addLabelForPiece("Pawn",   labelPanel);
        addLabelForPiece("Rook",   labelPanel);
        addLabelForPiece("Knight", labelPanel);
        addLabelForPiece("Bishop", labelPanel);
        addLabelForPiece("Queen",  labelPanel);

        add(labelPanel, BorderLayout.NORTH);
    }

    /**
     * Creates transplant controls (buttons and combo box), excluding King from the selector.
     */
    private void initializeTransplantControls() {
        JPanel transplantPanel = new JPanel();
        transplantPanel.setLayout(new GridLayout(3, 1)); 
        // Three rows: piece selection, perform transplant, cancel transplant

        // ComboBox for selecting the type of piece to transplant (excluding King)
        pieceSelector = new JComboBox<>(new String[]{"Pawn", "Rook", "Knight", "Bishop", "Queen"});
        pieceSelector.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // When the user changes the piece selection,
                // cancel any ongoing transplant mode.
                game.setTransplantMode(false);
                game.setSelectedPieceForTransplant(null);
                System.out.println("Transplant mode canceled due to piece change.");
            }
        });
        transplantPanel.add(pieceSelector);

        // Transplant button
        transplantButton = new JButton("Transplant Piece");
        transplantButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Check if it's the correct player's turn
                if (!game.isPlayerTurn(playerColor)) {
                    JOptionPane.showMessageDialog(null, "It's not your turn to transplant!");
                    System.out.println("Transplant button disabled because it's not your turn.");
                    return;
                }

                selectedPieceForTransplant = (String) pieceSelector.getSelectedItem();
                if (selectedPieceForTransplant != null
                        && pieceCountMap.getOrDefault(selectedPieceForTransplant, 0) > 0) {
                    // At least one piece of this type is available in the tool shed
                    game.initiateTransplantMode(selectedPieceForTransplant);
                    System.out.println("Transplant mode initiated for: " + selectedPieceForTransplant);
                } else {
                    JOptionPane.showMessageDialog(null, "No pieces of this type are available to transplant.");
                }
            }
        });
        transplantPanel.add(transplantButton);

        // Cancel transplant button
        cancelTransplantButton = new JButton("Cancel Transplant");
        cancelTransplantButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Cancel the transplant mode
                game.setTransplantMode(false);
                game.setSelectedPieceForTransplant(null);
                JOptionPane.showMessageDialog(null, "Transplant canceled.");
                System.out.println("Transplant mode canceled.");
            }
        });
        transplantPanel.add(cancelTransplantButton);

        add(transplantPanel, BorderLayout.SOUTH);
    }

    /**
     * Adds a label for a specific piece type and initializes it to 0.
     *
     * @param pieceName  the name of the piece type
     * @param labelPanel the panel to which the label will be added
     */
    private void addLabelForPiece(String pieceName, JPanel labelPanel) {
        JLabel label = new JLabel(pieceName + ": " + pieceCountMap.get(pieceName));
        pieceLabelMap.put(pieceName, label);
        labelPanel.add(label);
    }

    /**
     * Decreases the count of a specific piece type in the tool shed by 1 (for example, when a piece is transplanted).
     *
     * @param pieceName the name of the piece type to remove
     */
    public void removePieceFromShed(String pieceName) {
        int currentCount = pieceCountMap.getOrDefault(pieceName, 0);
        if (currentCount > 0) {
            pieceCountMap.put(pieceName, currentCount - 1);
            System.out.println("Removed one " + pieceName + ". New count: " + (currentCount - 1));

            // Update the label
            updateLabel(pieceName);
        } else {
            System.out.println("No pieces of type " + pieceName + " left to remove.");
        }

        // Refresh the panel so that the display is updated
        SwingUtilities.invokeLater(() -> {
            revalidate();
            repaint();
        });
    }

    /**
     * Updates the label for a specific piece type in the tool shed.
     *
     * @param pieceName the name of the piece type to update
     */
    private void updateLabel(String pieceName) {
        System.out.println("Updating piece label for: " + pieceName);
        JLabel label = pieceLabelMap.get(pieceName);
        System.out.println("Label is: " + label);

        if (label != null) {
            label.setText(pieceName + ": " + pieceCountMap.get(pieceName));
            label.setForeground(Color.RED);

            SwingUtilities.invokeLater(() -> {
                label.revalidate();
                label.repaint();
            });
        }
    }

    /**
     * Returns the count of pieces of a specific type stored in the tool shed.
     *
     * @param pieceName the name of the piece type
     * @return the number of pieces available
     */
    public int getPieceCount(String pieceName) {
        return pieceCountMap.getOrDefault(pieceName, 0);
    }

    /**
     * Returns a list of available (non-zero count) piece types for transplantation.
     *
     * @return a list of piece type names that are available (excluding King)
     */
    public List<String> getAvailablePieceNames() {
        List<String> available = new ArrayList<>();
        for (String pieceName : pieceCountMap.keySet()) {
            if (getPieceCount(pieceName) > 0) {
                available.add(pieceName);
            }
        }
        return available;
    }

    /**
     * Creates an instance of a piece based on the received name (Pawn, Rook, etc.),
     * with the specified position (x,y), the color according to the tool shed (White/Black),
     * board, and value.
     * Note: The King cannot be transplanted, so this method will not create one.
     *
     * @param pieceName the name of the piece to create
     * @param x         the row coordinate
     * @param y         the column coordinate
     * @param board     the board on which the piece will be placed
     * @param value     the value of the piece (though not used directly in this function)
     * @return a new instance of the specified piece, or null if an invalid name is given or if it is King
     */
    public Piece createPieceInstance(String pieceName, int x, int y, Board board, int value) {
        // Do not allow creation of King via the tool shed.
        if ("King".equals(pieceName)) {
            System.out.println("Transplanting a King is not allowed.");
            return null;
        }
        boolean isWhite = playerColor.equals("White");
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
                return new Queen(x, y, isWhite, board, isWhite ? 8 : -8);
            default:
                System.out.println("Invalid piece name selected: " + pieceName);
                return null;
        }
    }

    /**
     * Adds a captured piece to the player's tool shed (only if it belongs to the opponent).
     * The King is not added because it cannot be captured.
     *
     * @param piece the captured piece
     */
    public void addCapturedPiece(Piece piece) {
        if (piece != null && !piece.alive()) {
            // Do not add the King to the tool shed
            if (piece.getClass().getSimpleName().equals("King")) {
                System.out.println("King cannot be captured or transplanted. Ignored.");
                return;
            }
            // Check if the captured piece belongs to the opponent based on its color
            boolean isOpponentColor = (playerColor.equals("White") && !piece.isWhite())
                                   || (playerColor.equals("Black") &&  piece.isWhite());

            if (isOpponentColor) {
                String pieceName = piece.getClass().getSimpleName();
                int currentCount = pieceCountMap.getOrDefault(pieceName, 0);

                // Increase the count in the tool shed
                pieceCountMap.put(pieceName, currentCount + 1);
                System.out.println("Captured " + pieceName + ". New count: " + (currentCount + 1));

                // Update the label
                updateLabel(pieceName);

                // Refresh the panel
                SwingUtilities.invokeLater(() -> {
                    revalidate();
                    repaint();
                });
            } else {
                System.out.println("Piece " + piece.getClass().getSimpleName() 
                                   + " does not belong to the opponent. Ignored.");
            }
        } else {
            System.out.println("Invalid piece or piece is still alive.");
        }
    }

    /**
     * Overridden paintComponent method to draw the tool shed.
     *
     * @param g the Graphics context used for drawing
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    /**
     * Helper function for drawing the components of the tool shed (text and icons).
     *
     * @param g the Graphics context used for drawing
     */
    private void draw(Graphics g) {
        int x = 20;
        int y = 200;

        // Title: [White/Black] Tool Shed
        Font boldFont = g.getFont().deriveFont(Font.BOLD, 16f);
        g.setFont(boldFont);
        g.setColor(Color.BLACK);
        g.drawString(playerColor + " Tool Shed", x, y - 30);

        // Draw the pieces along with their counts
        for (String pieceName : pieceCountMap.keySet()) {
            int count = pieceCountMap.get(pieceName);

            if (count > 0) {
                ImageIcon pieceImage = null;
                boolean isWhite = playerColor.equals("White");

                switch (pieceName) {
                    case "Pawn":
                        pieceImage = isWhite ? PieceImages.wp : PieceImages.bp;
                        break;
                    case "Rook":
                        pieceImage = isWhite ? PieceImages.wr : PieceImages.br;
                        break;
                    case "Knight":
                        pieceImage = isWhite ? PieceImages.wn : PieceImages.bn;
                        break;
                    case "Bishop":
                        pieceImage = isWhite ? PieceImages.wb : PieceImages.bb;
                        break;
                    case "Queen":
                        pieceImage = isWhite ? PieceImages.wq : PieceImages.bq;
                        break;
                }

                if (pieceImage != null) {
                    // Draw the piece icon
                    g.drawImage(pieceImage.getImage(), x, y, 40, 40, null);
                    // Write the count next to it
                    g.setColor(Color.BLACK);
                    g.drawString("x " + count, x + 50, y + 30);
                    // Move down to the next row
                    y += 50;
                }
            }
        }
    }
}
