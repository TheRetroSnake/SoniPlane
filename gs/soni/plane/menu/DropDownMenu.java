package gs.soni.plane.menu;

import gs.app.lib.gfx.Graphics;
import gs.app.lib.gfx.Sprite;
import gs.app.lib.math.bounds;
import gs.app.lib.util.MouseUtil;
import gs.soni.plane.SP;
import gs.soni.plane.util.*;
import gs.soni.plane.util.Event;
import gs.soni.plane.v;

import java.awt.*;

public class DropDownMenu extends MenuEntry {

    private EventHandler event[];

    private String ID;
    private String text[];
    public int X;
    public int Y;
    public int Width;
    public int Height;

    private float MinY;
    private float MaxY;
    private float ExtSpeed;

    private StyleItem style;
    private float alpha;
    private Sprite spr;
    private bounds bounds;
    private boolean isExt = false;
    private Color BoxColor;
    private boolean ThisDisable = false;
    private boolean isHigh = false;
    private Color[] ClickColor;

    public DropDownMenu(String ID, int x, int y, StyleItem style, float alpha,
                        String[] elementTexts, EventHandler[] elementEvents) {
        this.ID = ID;
        this.text = elementTexts;
        this.alpha = alpha;

        this.style = style;
        this.event = elementEvents;
        Graphics.setFont(style.GetFont());

        X = ProsX(x);
        Y = ProsY(y);

        Width = GetMaxWidth(elementTexts) + 4;
        Height = GetMaxHeight(elementTexts) + 4;
        spr = new Sprite();

        bounds = new bounds(X, Y, X + Width, Y + Height);
        MinY = bounds.h;
        MaxY = Y + (Height * (elementTexts.length - 1));

        ExtSpeed = (MaxY - MinY) / 8;
        ClickColor = new Color[elementTexts.length];
    }

    public DropDownMenu(String ID, int x, int y, int width, int height, StyleItem style, float alpha,
                        String[] elementTexts, EventHandler[] elementEvents) {
        this.ID = ID;
        this.text = elementTexts;
        this.alpha = alpha;

        this.style = style;
        this.event = elementEvents;
        Graphics.setFont(style.GetFont());

        X = ProsX(x);
        Y = ProsY(y);

        Width = width;
        Height = height;
        spr = new Sprite();

        bounds = new bounds(X, Y, X + Width, Y + Height);
        MinY = bounds.h;
        MaxY = Y + (Height * (elementTexts.length - 1));

        ExtSpeed = (MaxY - MinY) / 8;
        ClickColor = new Color[elementTexts.length];
    }

    public void logic(menu menu) {
        GetColorMode();
        Extend();
    }

    private void Extend() {
        if(isExt){
            if(bounds.h < MaxY){
                bounds.h += ExtSpeed;
                SP.repaint();

                if(bounds.h >= MaxY) {
                    v.BlockControls = true;
                }
            }

        } else {
            if(bounds.h > MinY){
                bounds.h -= ExtSpeed;
                SP.repaint();

                if(bounds.h <= MinY){
                    v.BlockControls = false;
                }
            }
        }
    }

    public void draw(Graphics g) {
        if(MinY == MaxY ? isExt : bounds.h != MinY) {
            for(int i = 1;i < text.length;i ++) {
                if (bounds.y < bounds.h - (Height * (i - 1))) {

                    bounds n = new bounds(bounds.x, bounds.h - (Height * i), bounds.w, Height);

                    g.setColor(ClickColor[i]);
                    g.fillRect(n);

                    Graphics.setFont(style.GetFont());
                    g.setColor(style.GetColor());
                    g.drawText(text[i], bounds.x, bounds.h - (Height * i), style.GetAlignment(), bounds.w - bounds.x);
                }
            }

            if(Y - 1 > 0){
                spr.setColor(Color.BLACK);
                spr.setBounds(bounds.x, Y - Height, bounds.w - bounds.x, Height);
                g.fillRect(spr);
            }

        } else {
            spr.setColor(BoxColor);
            spr.setBounds(bounds.x, bounds.y, bounds.w - bounds.x, bounds.h - bounds.y);
            g.fillRect(spr);

            Graphics.setFont(style.GetFont());
            g.setColor(style.GetColor());
            g.drawText(text[0], bounds.x, bounds.y, style.GetAlignment(), bounds.w - bounds.x);
        }
    }

    public void dispose(){
    }

    private int GetMaxWidth(String[] texts) {
        int out = 0;

        for(String t : texts){
            int i = (int) Graphics.GetTextWidth(t);

            if(i >= out){
                out = i;
            }
        }

        return out;
    }

    private int GetMaxHeight(String[] texts) {
        int out = 0;

        for(String t : texts){
            int i = (int) Graphics.GetTextHeight(t);

            if(i >= out){
                out = i;
            }
        }

        return out;
    }

    private int ProsX(int x) {
        return x & menu.NORMALBITS;
    }

    private int ProsY(int y) {
        return y & menu.NORMALBITS;
    }

    public void SetY(int y) {
        Y = y;
    }

    public void SetX(int x) {
        X = x;
    }

    public int GetY() {
        return Y;
    }

    public int GetX() {
        return X;
    }

    private void GetColorMode() {
        if (Mouse.IsInArea(bounds, ThisDisable)) {
            if (MinY == MaxY ? isExt : bounds.h != MinY) {
                for (int i = 1; i < text.length; i++) {
                    if (bounds.y < bounds.h - (Height * (i - 1))) {
                        bounds n = new bounds(bounds.x, bounds.h - (Height * i), bounds.w,
                                bounds.h - (Height * (i - 1)));

                        if (Mouse.IsInArea(n, ThisDisable) && bounds.h == MaxY) {
                            if (Mouse.IsClicked(MouseUtil.LEFT, ThisDisable) || Mouse.IsClicked(MouseUtil.RIGHT, ThisDisable)) {
                                ClickColor[i] = Colors.GetColor("click", alpha);

                                if (event[i] != null) {
                                    event[i].setString(text[i]);
                                    Event.SetEvent(event[i]);
                                }

                                SP.repaint();
                                isHigh = false;

                            } else {
                                ClickColor[i] = Colors.GetColor("high", alpha);

                                if (!isHigh) {
                                    isHigh = true;
                                    SP.repaint();
                                }
                            }

                        } else {
                            ClickColor[i] = Colors.GetColor("normal", alpha);

                            if (isHigh) {
                                isHigh = false;
                                SP.repaint();
                            }
                        }
                    }
                }

            } else {

                if (!ThisDisable) {
                    SP.repaint();
                }

                v.BlockControls = true;
                ThisDisable = true;

                BoxColor = Colors.GetColor("high", alpha);
                if (Mouse.IsClicked(MouseUtil.LEFT, ThisDisable) || Mouse.IsClicked(MouseUtil.RIGHT, ThisDisable)) {
                    isExt = true;
                    SP.repaintLater();
                }

                if (!isHigh) {
                    isHigh = true;
                    SP.repaint();
                }

            }
        } else {
            BoxColor = Colors.GetColor("normal", alpha);
            isExt = false;
            if (ThisDisable) {
                v.BlockControls = false;
                ThisDisable = false;
                SP.repaint();
            }

            for (int i = 1;i < text.length;i ++) {
                ClickColor[i] = Colors.GetColor("normal", alpha);
            }
        }

    }

    public String GetID(){
        return ID;
    }

    public String GetText(){
        return ID;
    }

    public void SetText(String text) {
        this.text[0] = text;
    }
}
