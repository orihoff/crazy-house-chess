package com.chessgame.Frame;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import com.chessgame.Game.*;
import com.chessgame.Pieces.Piece;

public class Panel extends JPanel {

    private static final long serialVersionUID = 1L;
    private Game game;
    private int ti, tj;
    public static int xx, yy;
    
    // Flag indicating if the user plays as White (true) or Black (false)
    private boolean isUserWhite;

    public Panel(Game game, boolean isUserWhite) {
        this.game = game;
        this.isUserWhite = isUserWhite;
        this.setFocusable(true);
        this.requestFocusInWindow();
        this.addMouseListener(new Listener());
        this.addMouseMotionListener(new Listener());
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Call the draw method in Game with the isUserWhite flag
        game.draw(g, xx, yy, this, isUserWhite);
    }

    class Listener extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e)) {
                int x = e.getX() / Piece.size;
                int y = e.getY() / Piece.size;
                // Flip coordinates if user is Black
                if (!isUserWhite) {
                    x = 7 - x;
                    y = 7 - y;
                }
                if (game.isTransplantMode()) {
                    game.transplantPiece(game.getSelectedPieceForTransplant(), x, y);
                    game.setTransplantMode(false);
                } else {
                    Game.drag = false;
                    game.active = null;
                    game.selectPiece(x, y);
                }
                revalidate();
                repaint();
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            ti = e.getX() / Piece.size;
            tj = e.getY() / Piece.size;
            if (!isUserWhite) {
                ti = 7 - ti;
                tj = 7 - tj;
            }
            if (Game.board.getPiece(ti, tj) != null) {
                setCursor(new Cursor(Cursor.HAND_CURSOR));
            } else {
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
            revalidate();
            repaint();
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e)) {
                int x = e.getX() / Piece.size;
                int y = e.getY() / Piece.size;
                if (!isUserWhite) {
                    x = 7 - x;
                    y = 7 - y;
                }
                if (!Game.drag && game.active != null) {
                    game.active = null;
                }
                game.selectPiece(x, y);
                Game.drag = true;
                // Save drag offset (no coordinate flip needed here)
                xx = e.getX();
                yy = e.getY();
            }
            revalidate();
            repaint();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            int x = e.getX() / Piece.size;
            int y = e.getY() / Piece.size;
            if (!isUserWhite) {
                x = 7 - x;
                y = 7 - y;
            }
            game.move(x, y);
            revalidate();
            repaint();
        }
    }
}
