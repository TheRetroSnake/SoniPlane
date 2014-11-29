package gs.soni.plane.util;

import java.awt.*;

public class StyleItem {

    private Color color;
    private Font font;
    private String ID;
    private int align;

    public StyleItem(String ID, Font font, Color color, int align) {
        this.ID = ID;
        this.font = font;
        this.color = color;
        this.align = align;
    }

    public String GetID() {
        return ID;
    }

    public Color GetColor(){
        return color;
    }

    public Font GetFont(){
        return font;
    }

    public int GetAlignment(){
        return align;
    }
}
