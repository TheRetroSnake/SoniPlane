package gs.soni.plane.draw;

import gs.app.lib.gfx.Graphics;
import gs.app.lib.math.bounds;
import gs.app.lib.util.KeyUtil;
import gs.soni.plane.SP;
import gs.soni.plane.util.Keys;
import gs.soni.plane.util.Logicable;
import gs.soni.plane.util.StyleItem;
import gs.soni.plane.v;

public class TextBox implements Drawable, Logicable {

    private boolean allowMinus;
    private String regex;
    private int align;
    private StyleItem style;
    private String text;
    private boolean isEditing;

    public TextBox(String text, StyleItem style, int align, String regex, boolean allowMinus) {
        this.text = text;
        this.style = style;
        this.align = align;
        this.regex = regex;
        this.allowMinus = allowMinus;
    }

    @Override
    public void draw(Graphics g) {
        draw(g, new bounds(0, 0, 0, 0));
    }

    @Override
    public int renderPriority() {
        return v.RENDERPR_MAX - 1;
    }

    @Override
    public void logic() {
        if(isEditing) {
            if (Keys.isPressed(KeyUtil.BACKSPACE, true)) {
                if(text.length() > 0) {
                    text = text.substring(0, text.length() - 1);
                    SP.repaint();
                }

            } else if (Keys.isPressed(KeyUtil.DELETE, true)) {
                text = "";
                SP.repaint();

            } else if (Keys.isPressed(KeyUtil.ENTER, true)) {
                isEditing = false;

            } else {
                String t = Keys.GetNextPress(true);
                if(t.equals("-") && regex.equals("\\D") && allowMinus){
                    if(text.startsWith("-")){
                        text = text.substring(1, text.length());
                        SP.repaint();

                    } else {
                        text = "-"+ text;
                        SP.repaint();
                    }
                } else {
                    text += t.replaceAll(regex, "");
                    SP.repaint();
                }
            }
        }
    }

    public void draw(Graphics g, bounds bounds) {
        g.setColor(style.GetColor());
        Graphics.setFont(style.GetFont());
        g.drawText(text, bounds.x + 2, bounds.y, align, bounds.w - 4 - bounds.x);
    }

    public void SetText(String text) {
        this.text = text;
    }

    public String GetText() {
        return text;
    }

    public boolean isEditing() {
        return isEditing;
    }

    public void Edit() {
        isEditing = true;
    }
}
