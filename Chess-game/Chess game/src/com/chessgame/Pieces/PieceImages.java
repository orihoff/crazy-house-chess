package com.chessgame.Pieces;

import java.awt.Color;
import javax.swing.ImageIcon;

public class PieceImages {
    public static Color WHITECOLOR = Color.WHITE; // Make colors public
    public static Color BLACKCOLOR = Color.BLACK;
    public static String PAWN = "♟";
    public static String ROOK = "♜";
    public static String KNIGHT = "♞";
    public static String BISHOP = "♝";
    public static String QUEEN = "♛";
    public static String KING = "♚";

    public static ImageIcon wk; // Make image icons public
    public static ImageIcon bk;
    public static ImageIcon wr;
    public static ImageIcon br;
    public static ImageIcon wq;
    public static ImageIcon bq;
    public static ImageIcon wb;
    public static ImageIcon bb;
    public static ImageIcon wn;
    public static ImageIcon bn;
    public static ImageIcon wp;
    public static ImageIcon bp;

    public PieceImages() {
        wk = new ImageIcon(getClass().getResource("../Resources/images/wk.png"));
        bk = new ImageIcon(getClass().getResource("../Resources/images/bk.png"));
        wr = new ImageIcon(getClass().getResource("../Resources/images/wr.png"));
        br = new ImageIcon(getClass().getResource("../Resources/images/br.png"));
        wq = new ImageIcon(getClass().getResource("../Resources/images/wq.png"));
        bq = new ImageIcon(getClass().getResource("../Resources/images/bq.png"));
        wb = new ImageIcon(getClass().getResource("../Resources/images/wb.png"));
        bb = new ImageIcon(getClass().getResource("../Resources/images/bb.png"));
        wn = new ImageIcon(getClass().getResource("../Resources/images/wn.png"));
        bn = new ImageIcon(getClass().getResource("../Resources/images/bn.png"));
        wp = new ImageIcon(getClass().getResource("../Resources/images/wp.png"));
        bp = new ImageIcon(getClass().getResource("../Resources/images/bp.png"));
    }
}
