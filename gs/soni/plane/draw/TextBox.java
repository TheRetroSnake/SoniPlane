package gs.soni.plane.draw;

import gs.app.lib.application.App;
import gs.app.lib.gfx.Graphics;
import gs.app.lib.math.bounds;
import gs.app.lib.util.KeyUtil;
import gs.soni.plane.util.Colors;
import gs.soni.plane.util.Logicable;
import gs.soni.plane.util.StyleItem;
import gs.soni.plane.v;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class TextBox implements Drawable, Logicable {

    private String regex;
    private JTextField tx;
    private boolean isEditing;
    private boolean minus;
    private boolean neg;

    public TextBox(String text, StyleItem style, String regex, boolean allowMinus) {
        tx = new JTextField(text);
        tx.setBackground(Colors.GetColor("normal", 1f));
        tx.setForeground(style.GetColor());
        tx.setCaretColor(style.GetColor());
        tx.setFont(style.GetFont());
        tx.setBorder(null);
        tx.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                super.keyTyped(e);
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyUtil.ENTER) {
                    isEditing = false;
                    App.getJPanel().remove(tx);

                } else if(e.getKeyCode() == KeyUtil.MINUS && minus){
                    super.keyPressed(e);
                    neg ^= true;

                } else {
                    super.keyPressed(e);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
            }
        });

        this.regex = regex;
        minus = allowMinus;
    }

    @Override
    public void draw(Graphics g) {

    }

    @Override
    public int renderPriority() {
        return v.RENDERPR_MAX - 1;
    }

    @Override
    public void logic() {
        tx.setEnabled(isEditing);

        if(minus && tx.getText().contains("-")){
            String t = tx.getText().replace("-", "");

            if(neg){
                t = "-"+ t;
            }

            tx.setText(t);
            tx.setCaretPosition(t.length());
        }
    }

    public void draw(Graphics g, bounds b) {
        tx.setBounds(b.x, b.y, b.w - b.x, b.h - b.y);
        tx.repaint();
    }

    public void SetText(String text) {
        if(!regex.equals("")) {
            text = text.replaceAll(regex, "");
        }
        tx.setText(text);
    }

    public String GetText() {
        if(!regex.equals("")) {
            tx.setText(tx.getText().replaceAll(regex, ""));
        }
        return tx.getText();
    }

    public boolean isEditing() {
        return isEditing;
    }

    public void Edit() {
        isEditing = true;
        App.getJPanel().add(tx);
        tx.requestFocus();

        int sel = tx.getText().length();
        tx.setSelectionStart(sel);
        tx.setSelectionEnd(sel);
    }
}
