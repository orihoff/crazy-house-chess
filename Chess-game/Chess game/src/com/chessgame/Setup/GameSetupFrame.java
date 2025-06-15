package com.chessgame.Setup;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.chessgame.Frame.Frame;
import com.chessgame.Game.Game;

/**
 * A startup screen where the user selects:
 *  - White or Black
 *  - Player vs Player or Player vs Computer
 * 
 * Once "Start" is pressed, this window disposes itself and launches the main Frame.
 */
public class GameSetupFrame extends JFrame {

    private JRadioButton whiteButton;
    private JRadioButton blackButton;
    private ButtonGroup colorGroup;

    private JRadioButton pvpButton;
    private JRadioButton pvcButton;
    private ButtonGroup modeGroup;

    private JButton startButton;

    public GameSetupFrame() {
        setTitle("Chess Game Setup");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(230, 230, 250));

        // Title label with custom font
        JLabel titleLabel = new JLabel("Crazyhouse Chess Game Setup", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 24));
        titleLabel.setForeground(new Color(50, 50, 150));
        add(titleLabel, BorderLayout.NORTH);

        // Center panel containing selection panels
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        
        // Color selection panel
        JPanel colorPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        colorPanel.setBorder(BorderFactory.createTitledBorder(
                new EtchedBorder(), "Select Color", TitledBorder.CENTER, TitledBorder.TOP));
        colorPanel.setOpaque(false);
        whiteButton = new JRadioButton("White", true);
        blackButton = new JRadioButton("Black");
        whiteButton.setOpaque(false);
        blackButton.setOpaque(false);
        colorGroup = new ButtonGroup();
        colorGroup.add(whiteButton);
        colorGroup.add(blackButton);
        colorPanel.add(whiteButton);
        colorPanel.add(blackButton);
        gbc.gridx = 0;
        gbc.gridy = 0;
        centerPanel.add(colorPanel, gbc);

        // Mode selection panel
        JPanel modePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        modePanel.setBorder(BorderFactory.createTitledBorder(
                new EtchedBorder(), "Select Game Mode", TitledBorder.CENTER, TitledBorder.TOP));
        modePanel.setOpaque(false);
        pvpButton = new JRadioButton("Player vs Player", true);
        pvcButton = new JRadioButton("Player vs Computer");
        pvpButton.setOpaque(false);
        pvcButton.setOpaque(false);
        modeGroup = new ButtonGroup();
        modeGroup.add(pvpButton);
        modeGroup.add(pvcButton);
        modePanel.add(pvpButton);
        modePanel.add(pvcButton);
        gbc.gridx = 0;
        gbc.gridy = 1;
        centerPanel.add(modePanel, gbc);

        add(centerPanel, BorderLayout.CENTER);

        // Start button panel with a custom styled button
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        startButton = new JButton("Start");
        startButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        startButton.setBackground(new Color(100, 149, 237));
        startButton.setForeground(Color.WHITE);
        startButton.setFocusPainted(false);
        startButton.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Color.DARK_GRAY, 2),
                new EmptyBorder(5, 15, 5, 15)));
        buttonPanel.add(startButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Start button action
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean userIsWhite = whiteButton.isSelected();
                Game.Mode selectedMode = pvpButton.isSelected() 
                        ? Game.Mode.PLAYER_VS_PLAYER 
                        : Game.Mode.PLAYER_VS_COMPUTER;

                // Create the main chess Frame with these settings
                Frame chessFrame = new Frame(userIsWhite, selectedMode);
                chessFrame.setVisible(true);

                // Close the setup window
                dispose();
            }
        });
    }
    
    public static void main(String[] args) {
        // For testing purposes
        SwingUtilities.invokeLater(() -> {
            GameSetupFrame setupFrame = new GameSetupFrame();
            setupFrame.setVisible(true);
        });
    }
}
