package com.chessgame;

import java.lang.reflect.InvocationTargetException;
import javax.swing.SwingUtilities;

import com.chessgame.Setup.GameSetupFrame;

/**
 * Main entry point for the Chess application.
 * Now shows GameSetupFrame first so user can select color & mode.
 */
public class main {
    public static void main(String[] args) throws InvocationTargetException, InterruptedException {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // Show the setup window first
                GameSetupFrame setupFrame = new GameSetupFrame();
                setupFrame.setVisible(true);
            }
        });
    }
}
