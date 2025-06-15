package com.chessgame.Frame;

import javax.swing.*;
import java.awt.*;
import com.chessgame.Game.Game;
import com.chessgame.Game.ToolShed;

public class Frame extends JFrame {

    private static final long serialVersionUID = -4442947819954124379L;
    public static final int WIDTH = 640;
    public static final int HEIGHT = 640;
    
    private ToolShed whiteToolShed;
    private ToolShed blackToolShed;
    private Game game;

    /**
     * New constructor that accepts user color choice and game mode.
     * @param userIsWhite true if the user wants to play as White, false if as Black
     * @param userMode Player vs Player or Player vs Computer
     */
    public Frame(boolean userIsWhite, Game.Mode userMode) {
        // Create the Game object
        game = new Game();
        
        // Set the game mode
        game.setGameMode(userMode);
        
        // -- Added AI settings --
        if(userMode == Game.Mode.PLAYER_VS_COMPUTER) {
            if(!userIsWhite) {
                // User is Black => set AI to play as White
                Game.setAiIsWhite(true);
            } else {
                // User is White => set AI to play as Black
                Game.setAiIsWhite(false);
            }
        }
        
        game.checkAndPerformAIMove();
        
        // Get the tool sheds
        whiteToolShed = game.getWhiteToolShed();
        blackToolShed = game.getBlackToolShed();

        // Create the chess panel, passing game and user color
        JPanel chessPanel = new Panel(game, userIsWhite);

        // Window (frame) settings
        this.setTitle("Chess");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.setLayout(new BorderLayout());

        // Add the chess panel in the center
        chessPanel.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.add(chessPanel, BorderLayout.CENTER);

        // Add tool sheds on the sides
        whiteToolShed.setPreferredSize(new Dimension(180, HEIGHT));
        blackToolShed.setPreferredSize(new Dimension(180, HEIGHT));
        this.add(whiteToolShed, BorderLayout.EAST);
        this.add(blackToolShed, BorderLayout.WEST);

        // Finalize
        this.pack();
        this.setLocationRelativeTo(null);
    }

    public Frame() {
        this(true, Game.Mode.PLAYER_VS_PLAYER);
    }

    public ToolShed getWhiteToolShed() {
        return whiteToolShed;
    }

    public ToolShed getBlackToolShed() {
        return blackToolShed;
    }
}
