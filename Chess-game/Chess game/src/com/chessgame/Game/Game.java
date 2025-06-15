package com.chessgame.Game;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.*;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.JPanel;
import javax.swing.JFrame;

import com.chessgame.Ai.AIGameEngine;
import com.chessgame.Board.Board;
import com.chessgame.Board.Move;
import com.chessgame.Pieces.*;
import com.chessgame.Setup.GameSetupFrame;
import com.chessgame.Frame.Frame;

public class Game {
    public static Board board = new Board();

    static King wk;
    static King bk;
    static ArrayList<Piece> wPieces = new ArrayList<>();
    static ArrayList<Piece> bPieces = new ArrayList<>();

    public static boolean player = true; 
    public static Piece active = null;
    public static boolean drag = false;
    public static ArrayList<Piece> AllPieces = new ArrayList<>();

    static List<Move> allPlayersMove = new ArrayList<>();
    public static List<Move> allEnemysMove = new ArrayList<>();
    private static boolean gameOver = false;

    private static ToolShed whiteToolShed;
    private static ToolShed blackToolShed;
    private boolean transplantMode = false;
    private String pieceToTransplant = null;
    // Cache the allowed transplant cells for the current transplant mode
    private List<Point> allowedTransplantCells;

    // Flag: if user is White => true, else false (user color)
    public static boolean isUserWhiteGame = true; 

    // AI color flag (true = AI is white, false = AI is black)
    static boolean aiIsWhite = false;

    public enum Mode {
        PLAYER_VS_PLAYER,
        PLAYER_VS_COMPUTER
    }
    private Mode gameMode = Mode.PLAYER_VS_PLAYER;

    // Static variable to hold the current Game instance
    public static Game currentGame;

    public Game() {
        new PieceImages();
        whiteToolShed = new ToolShed("White", this);
        blackToolShed = new ToolShed("Black", this);

        currentGame = this;
        
        // Load starting position via FEN – default: white's turn ("w")
        loadFenPosition("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
        start();
    }

    /**
     * Checks and performs an AI move if needed.
     */
    public void checkAndPerformAIMove() {
        if (gameMode == Mode.PLAYER_VS_COMPUTER && player == aiIsWhite && !gameOver) {
            SwingUtilities.invokeLater(() -> {
                AIGameEngine.performHeuristicMove(Game.this);
            });
        }
    }

    // --------------------------
    // DRAW LOGIC
    // --------------------------
    public void draw(Graphics g, int x, int y, JPanel panel, boolean isUserWhite) {
        drawBoard(g, isUserWhite);
        drawPieces(g, panel, isUserWhite);
        drawPossibleMoves(g, panel, isUserWhite);  
        drag(active, x, y, g, panel);
        drawKingInCheck(isUserWhite, g, panel);
    }

    public void drawBoard(Graphics g, boolean isUserWhite) {
        for (int row = 0; row < Board.ROWS; row++) {
            for (int col = 0; col < Board.COLUMNS; col++) {
                int drawCol = flipCoord(col, isUserWhite);
                int drawRow = flipCoord(row, isUserWhite);
                g.setColor(((drawCol + drawRow) % 2 == 1) ? new Color(118, 150, 86) : new Color(238, 238, 210));
                g.fillRect(drawCol * Piece.size, drawRow * Piece.size, Piece.size, Piece.size);
            }
        }
    }

    /**
     * Draws all pieces using a temporary coordinate flip.
     */
    public void drawPieces(Graphics g, JPanel panel, boolean isUserWhite) {
        for (Piece p : AllPieces) {
            int origX = p.getXcord();
            int origY = p.getYcord();
            int drawX = flipCoord(origX, isUserWhite);
            int drawY = flipCoord(origY, isUserWhite);
            p.setXcord(drawX);
            p.setYcord(drawY);
            p.draw(g, false, panel);
            p.setXcord(origX);
            p.setYcord(origY);
        }
    }

    /**
     * Draws the possible moves or transplant positions (with coordinate flip).
     */
    public void drawPossibleMoves(Graphics g, JPanel panel, boolean isUserWhite) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(3));
        if (active != null) {
            active.showMoves(g2, panel, isUserWhite);
        } else if (transplantMode) {
            drawAvailableTransplantCells(g2, isUserWhite);
        }
    }

    /**
     * Draws green dots on empty cells for transplant positions, with coordinate flip.
     * For Pawn, only cells whose row is between 1 and 6 are drawn.
     */
    private void drawAvailableTransplantCells(Graphics2D g2, boolean isUserWhite) {
        g2.setColor(new Color(34, 139, 34));
        int dotSize = Piece.size / 3;
        // Use cached allowed cells if available; otherwise compute now
        List<Point> allowed = allowedTransplantCells;
        if (allowed == null) {
            allowed = computeAllowedTransplantCells();
        }
    
        for (Point cell : allowed) {
            int col = cell.x;
            int row = cell.y;
            int drawCol = flipCoord(col, isUserWhite);
            int drawRow = flipCoord(row, isUserWhite);
            int x = drawCol * Piece.size + (Piece.size - dotSize) / 2;
            int y = drawRow * Piece.size + (Piece.size - dotSize) / 2;
            g2.fillOval(x, y, dotSize, dotSize);
        }
    }

    /**
     * Draws a red rectangle around a king that is in check.
     */
    public void drawKingInCheck(boolean isUserWhite, Graphics g, JPanel panel) {
        g.setColor(Color.RED);
        drawSingleKingInCheck(wk, isUserWhite, g);
        drawSingleKingInCheck(bk, isUserWhite, g);
        panel.revalidate();
        panel.repaint();
    }

    private void drawSingleKingInCheck(King king, boolean isUserWhite, Graphics g) {
        if (king != null && king.isInCheck()) {
            int kingX = flipCoord(king.getXcord(), isUserWhite);
            int kingY = flipCoord(king.getYcord(), isUserWhite);
            g.drawRect(kingX * Piece.size, kingY * Piece.size, Piece.size, Piece.size);
        }
    }

    // --------------------------
    // selectPiece
    // --------------------------
    public void selectPiece(int x, int y) {
        int realX = isUserWhiteGame ? x : 7 - x;
        int realY = isUserWhiteGame ? y : 7 - y;
        if (transplantMode) {
            // Use the cached allowed transplant cells to validate the selection
            if (allowedTransplantCells == null) {
                allowedTransplantCells = computeAllowedTransplantCells();
            }
            boolean isAllowed = false;
            for (Point cell : allowedTransplantCells) {
                if (cell.x == realX && cell.y == realY) {
                    isAllowed = true;
                    break;
                }
            }
            if (!isAllowed) {
                JOptionPane.showMessageDialog(null, "You selected an unmarked cell. Please select a highlighted cell.");
                return;
            }
            if (board.getPiece(realX, realY) != null) {
                JOptionPane.showMessageDialog(null, "Selected cell is occupied.");
                return;
            }
            transplantPiece(pieceToTransplant, realX, realY);
            transplantMode = false;
            pieceToTransplant = null;
            allowedTransplantCells = null; // Clear the cache after transplant
        } else if (active == null && board.getPiece(realX, realY) != null 
                && board.getPiece(realX, realY).isWhite() == player) {
            active = board.getPiece(realX, realY);
        }
    }
    
    public Piece move(int x, int y) {
        int realX = isUserWhiteGame ? x : 7 - x;
        int realY = isUserWhiteGame ? y : 7 - y;
        if (active != null) {
            Piece capturedPiece = board.getPiece(realX, realY);
            boolean moveSuccessful = active.makeMove(realX, realY, board);
            if (moveSuccessful) {
                tryToPromote(active);
                if (capturedPiece != null) {
                    addCapturedPieceToToolShed(capturedPiece);
                }
                changeSide();
                if (gameMode == Mode.PLAYER_VS_COMPUTER && player == aiIsWhite && !gameOver) {
                    SwingUtilities.invokeLater(() -> {
                        AIGameEngine.performHeuristicMove(Game.this);
                    });
                }
                active = null;
                return capturedPiece;
            }
            drag = false;
        }
        return null;
    }

    // --------------------------
    // Pawn promotion
    // --------------------------
    public void tryToPromote(Piece p) {
        if (p instanceof Pawn) {
            Pawn pawn = (Pawn) p;
            if (pawn.madeToTheEnd()) {
                if (gameMode == Mode.PLAYER_VS_COMPUTER && p.isWhite() == Game.isAiIsWhite()) {
                    choosePiece(p, 0); // 0 => "Queen"
                } else {
                    int choice = showMessageForPromotion();
                    choosePiece(p, choice);
                }
            }
        }
    }

    public int showMessageForPromotion() {
        Object[] options = { "Queen", "Rook", "Knight", "Bishop" };
        drag = false;
        return JOptionPane.showOptionDialog(
            null, 
            "Choose piece to promote to", 
            null,
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE, 
            null, 
            options, 
            options[0]
        );
    }

    public static void choosePiece(Piece p, int choice) {
        AllPieces.remove(p);
        int x = p.getXcord();
        int y = p.getYcord();
        boolean isWhite = p.isWhite();
        Board brd = Game.board;

        switch (choice) {
            case 0: p = new Queen(x, y, isWhite, brd, isWhite ? 8 : -8); break;
            case 1: p = new Rook(x, y, isWhite, brd, isWhite ? 5 : -5); break;
            case 2: p = new Knight(x, y, isWhite, brd, isWhite ? 3 : -3); break;
            case 3: p = new Bishop(x, y, isWhite, brd, isWhite ? 3 : -3); break;
            default: p = new Queen(x, y, isWhite, brd, isWhite ? 8 : -8); break;
        }
        AllPieces.add(p);
        fillPieces();
    }

    // --------------------------
    // Turn checking
    // --------------------------
    /**
     * Checks if it's the specified player's turn.
     */
    public boolean isPlayerTurn(String playerColor) {
        return (playerColor.equals("White") && player) || (playerColor.equals("Black") && !player);
    }

    // --------------------------
    // Other game logic
    // --------------------------
    public static void generatePlayersTurnMoves(Board board) {
        allPlayersMove = new ArrayList<>();
        for (Piece p : AllPieces) {
            if (p.isWhite() == player) {
                p.fillAllPseudoLegalMoves(board);
                allPlayersMove.addAll(p.getMoves());
            }
        }
    }

    public static void generateEnemysMoves(Board board) {
        allEnemysMove = new ArrayList<>();
        for (Piece p : AllPieces) {
            if (p.isWhite() != player) {
                p.fillAllPseudoLegalMoves(board);
                allEnemysMove.addAll(p.getMoves());
            }
        }
    }

    public static void changeSide() {
        active = null;
        if (currentGame != null) {
            currentGame.setTransplantMode(false);
            currentGame.pieceToTransplant = null;
            currentGame.allowedTransplantCells = null;
        }
        player = !player;
        generateEnemysMoves(board);
        generatePlayersTurnMoves(board);
        checkPlayersLegalMoves();
        checkMate();
    }

    public static void checkPlayersLegalMoves() {
        List<Piece> pieces = player ? wPieces : bPieces;
        for (Piece p : pieces) {
            checkLegalMoves(p);
        }
    }

    public static void checkLegalMoves(Piece piece) {
        List<Move> movesToRemove = new ArrayList<>();
        for (Move move : piece.getMoves()) {
            Board clonedBoard = board.clone();
            Piece clonedActive = piece.clone();
            clonedActive.makeMove(move.getToX(), move.getToY(), clonedBoard);

            List<Piece> enemyPieces = piece.isWhite() ? bPieces : wPieces;
            Piece king = piece.isWhite() ? wk : bk;
            for (Piece enemyP : enemyPieces) {
                Piece clonedEnemyPiece = enemyP.clone();
                clonedEnemyPiece.fillAllPseudoLegalMoves(clonedBoard);
                for (Move bMove : clonedEnemyPiece.getMoves()) {
                    Piece occupant = clonedBoard.getPiece(enemyP.getXcord(), enemyP.getYcord());
                    if (!(clonedActive instanceof King)
                            && bMove.getToX() == king.getXcord()
                            && bMove.getToY() == king.getYcord()
                            && occupant != null 
                            && occupant.getValueInTheboard() == enemyP.getValueInTheboard()) {
                        movesToRemove.add(move);
                    } else if (clonedActive instanceof King
                            && bMove.getToX() == clonedActive.getXcord()
                            && bMove.getToY() == clonedActive.getYcord()
                            && occupant != null
                            && occupant.getValueInTheboard() == enemyP.getValueInTheboard()) {
                        movesToRemove.add(move);
                    }
                }
            }
        }
        for (Move rem : movesToRemove) {
            piece.getMoves().remove(rem);
        }
    }

    public static void checkMate() {
        boolean hasLegalMove = false;
        List<Piece> pieces = player ? wPieces : bPieces;
        for (Piece p : pieces) {
            if (!p.getMoves().isEmpty()) {
                hasLegalMove = true;
                break;
            }
        }
        if (hasLegalMove) return;

        String resultMessage;
        if (player) {
            resultMessage = (wk != null && wk.isInCheck()) 
                ? "Checkmate! Black wins." 
                : "Stalemate.";
        } else {
            resultMessage = (bk != null && bk.isInCheck()) 
                ? "Checkmate! White wins." 
                : "Stalemate.";
        }
        JOptionPane.showMessageDialog(null, resultMessage);
        gameOver = true;
        Object[] options = {"Play Again", "Return to Setup Menu", "Exit"};
        int choice = JOptionPane.showOptionDialog(null, 
            "Choose an option:", 
            "Game Over", 
            JOptionPane.DEFAULT_OPTION, 
            JOptionPane.QUESTION_MESSAGE, 
            null, 
            options, 
            options[0]);

        JFrame currentFrame = (JFrame) SwingUtilities.getWindowAncestor(currentGame.getWhiteToolShed());
        resetStaticData();
        if (choice == 0) {
            if (currentFrame != null) {
                currentFrame.dispose();
            }
            new Frame(isUserWhiteGame, currentGame.getGameMode()).setVisible(true);
        } else if (choice == 1) {
            if (currentFrame != null) {
                currentFrame.dispose();
            }
            new GameSetupFrame().setVisible(true);
        } else {
            System.exit(0);
        }
    }
    
    /**
     * Resets all static game data for a full reinitialization.
     */
    public static void resetStaticData() {
        board = new Board();
        AllPieces = new ArrayList<>();
        wPieces = new ArrayList<>();
        bPieces = new ArrayList<>();
        gameOver = false;
        player = true;
    }

    public void drag(Piece piece, int x, int y, Graphics g, JPanel panel) {
        if (piece != null && drag) {
            piece.drawDrag(g, player, x, y, panel);
        }
    }

    public static void addCapturedPieceToToolShed(Piece capturedPiece) {
        ToolShed currentToolShed = player ? whiteToolShed : blackToolShed;
        currentToolShed.addCapturedPiece(capturedPiece);
    }

    /**
     * Transplant method – updates the board, adds the piece, removes it from the Tool Shed, and changes turn.
     */
    public void transplantPiece(String pieceName, int x, int y) {
        ToolShed currentToolShed = player ? whiteToolShed : blackToolShed;
        int value = getValueForPiece(pieceName);

        if (currentToolShed.getPieceCount(pieceName) <= 0) {
            JOptionPane.showMessageDialog(null, 
                "No pieces of type " + pieceName + " left in the Tool Shed.");
            return;
        }
        Piece piece = currentToolShed.createPieceInstance(pieceName, x, y, board, value);
        if (piece == null) {
            JOptionPane.showMessageDialog(null, 
                "Piece creation failed for piece: " + pieceName);
            return;
        }
        board.setPieceIntoBoard(x, y, piece);
        piece.setXcord(x);
        piece.setYcord(y);
        AllPieces.add(piece);
        if (piece.isWhite()) {
            wPieces.add(piece);
        } else {
            bPieces.add(piece);
        }
        currentToolShed.removePieceFromShed(pieceName);
        changeSide();
        if (gameMode == Mode.PLAYER_VS_COMPUTER && player == aiIsWhite && !gameOver) {
            SwingUtilities.invokeLater(() -> {
                AIGameEngine.performHeuristicMove(Game.this);
            });
        }
    }

    public void setSelectedPieceForTransplant(String pieceName) {
        this.pieceToTransplant = pieceName;
    }

    public boolean isTransplantMode() {
        return transplantMode;
    }

    public String getSelectedPieceForTransplant() {
        return pieceToTransplant;
    }

    public void setTransplantMode(boolean mode) {
        transplantMode = mode;
    }

    /**
     * Initiates transplant mode and caches the allowed transplant cells for consistency.
     */
    public void initiateTransplantMode(String pieceName) {
        transplantMode = true;
        pieceToTransplant = pieceName;
        allowedTransplantCells = computeAllowedTransplantCells();
        System.out.println("Transplant mode activated for piece: " + pieceName);
    }
    
    /**
     * Computes and returns the list of allowed transplant cells based on the current game state.
     */
    private List<Point> computeAllowedTransplantCells() {
        List<Point> allowed = new ArrayList<>();
        King currentKing = player ? wk : bk;
    
        // If the king is not in check, any empty cell is allowed
        if (!currentKing.isInCheck()) {
            for (int col = 0; col < board.COLUMNS; col++) {
                for (int row = 0; row < board.ROWS; row++) {
                    if (board.getPiece(col, row) == null) {
                        allowed.add(new Point(col, row));
                    }
                }
            }
        } else {
            // When the king is in check, compute the cells that block the check
            List<Piece> checkingPieces = new ArrayList<>();
            List<Piece> enemyPieces = player ? bPieces : wPieces;
            for (Piece enemy : enemyPieces) {
                enemy.fillAllPseudoLegalMoves(board);
                for (Move m : enemy.getMoves()) {
                    if (m.getToX() == currentKing.getXcord() && m.getToY() == currentKing.getYcord()) {
                        checkingPieces.add(enemy);
                        break;
                    }
                }
            }
    
            // If there is a double check, blocking via transplant is not possible
            if (checkingPieces.size() == 1) {
                Piece checker = checkingPieces.get(0);
                // If the checking piece is a Knight or Pawn, blocking is not possible
                if (!(checker instanceof Knight || checker instanceof Pawn)) {
                    int dx = Integer.signum(currentKing.getXcord() - checker.getXcord());
                    int dy = Integer.signum(currentKing.getYcord() - checker.getYcord());
                    int x = checker.getXcord() + dx;
                    int y = checker.getYcord() + dy;
                    while (x != currentKing.getXcord() || y != currentKing.getYcord()) {
                        allowed.add(new Point(x, y));
                        x += dx;
                        y += dy;
                    }
                }
            }
        }
    
        // For Pawn transplant, restrict allowed cells to rows 1-6
        if (pieceToTransplant != null && pieceToTransplant.equals("Pawn")) {
            allowed.removeIf(point -> point.y < 1 || point.y > 6);
        }
        return allowed;
    }

    public void loadFenPosition(String fenString) {
        String[] parts = fenString.split(" ");
        String position = parts[0];
        int row = 0, col = 0;
        for (char c : position.toCharArray()) {
            if (c == '/') {
                row++;
                col = 0;
            } else if (Character.isLetter(c)) {
                addToBoard(col, row, c, Character.isUpperCase(c));
                col++;
            } else if (Character.isDigit(c)) {
                col += Character.getNumericValue(c);
            }
        }
        if (parts.length > 1) {
            player = parts[1].equals("w");
        }
    }

    public static void fillPieces() {
        wPieces = new ArrayList<>();
        bPieces = new ArrayList<>();
        for (Piece p : AllPieces) {
            if (p.isWhite()) {
                wPieces.add(p);
            } else {
                bPieces.add(p);
            }
        }
    }

    public void addToBoard(int x, int y, char c, boolean isWhite) {
        switch (Character.toUpperCase(c)) {
            case 'R':
                AllPieces.add(new Rook(x, y, isWhite, board, isWhite ? 5 : -5));
                break;
            case 'N':
                AllPieces.add(new Knight(x, y, isWhite, board, isWhite ? 3 : -3));
                break;
            case 'B':
                AllPieces.add(new Bishop(x, y, isWhite, board, isWhite ? 3 : -3));
                break;
            case 'Q':
                AllPieces.add(new Queen(x, y, isWhite, board, isWhite ? 8 : -8));
                break;
            case 'K':
                King king = new King(x, y, isWhite, board, isWhite ? 10 : -10);
                AllPieces.add(king);
                if (isWhite) {
                    wk = king;
                } else {
                    bk = king;
                }
                break;
            case 'P':
                AllPieces.add(new Pawn(x, y, isWhite, board, isWhite ? 1 : -1));
                break;
        }
    }

    public static void highlightAvailableCellsForTransplant() {
        for (int i = 0; i < board.COLUMNS; i++) {
            for (int j = 0; j < board.ROWS; j++) {
                if (board.getPiece(i, j) == null) {
                    System.out.println("Available cell for transplant at: (" + i + ", " + j + ")");
                }
            }
        }
    }

    private int getValueForPiece(String pieceName) {
        switch(pieceName) {
            case "Pawn":   return 1;
            case "Rook":   return 5;
            case "Knight": return 3;
            case "Bishop": return 3;
            case "Queen":  return 9;
            case "King":   return 100;
            default:       return 1;
        }
    }

    // --------------------------
    // Getters/Setters
    // --------------------------
    public Mode getGameMode() {
        return gameMode;
    }

    public void setGameMode(Mode mode) {
        this.gameMode = mode;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public Board getBoard() {
        return board;
    }

    public void setActivePiece(Piece p) {
        active = p;
    }

    public static boolean isAiIsWhite() {
        return aiIsWhite;
    }

    public static void setAiIsWhite(boolean aiIsWhiteParam) {
        aiIsWhite = aiIsWhiteParam;
    }

    public void resetGame() {
        AllPieces.clear();
        wPieces.clear();
        bPieces.clear();
        board = new Board();
        loadFenPosition("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
        gameOver = false;
    }

    public ToolShed getWhiteToolShed() {
        return whiteToolShed;
    }

    public ToolShed getBlackToolShed() {
        return blackToolShed;
    }

    public void start() {
        fillPieces();
        generatePlayersTurnMoves(board);
        generateEnemysMoves(board);
    }

    /**
     * Helper method to flip coordinates when needed.
     */
    private int flipCoord(int coord, boolean isUserWhite) {
        return isUserWhite ? coord : 7 - coord;
    }
}