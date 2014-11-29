package gs.soni.plane.menu;

import gs.app.lib.application.App;
import gs.app.lib.gfx.Graphics;
import gs.app.lib.gfx.Sprite;
import gs.app.lib.math.bounds;
import gs.app.lib.util.MouseUtil;
import gs.soni.plane.SP;
import gs.soni.plane.draw.TextBox;
import gs.soni.plane.util.*;
import gs.soni.plane.util.Event;
import gs.soni.plane.v;

import java.awt.*;

public class AdjustButton extends MenuEntry {

    private int t;
    private EventHandler event;

    private String ID;
    private String text;
    public int X;
    public int Y;
    public int Width;
    public int Height;
    private String base;
    private StyleItem style;
    private float alpha;
    private Sprite spr;

    private bounds Off = new bounds(0, 0, 0, 0);
    private boolean isActive = false;
    private bounds TextBox;
    private TextBox tx;
    private boolean ThisDisable = false;

    public AdjustButton(String ID, int x, int y, int width, int height, String basevalue,
                        StyleItem style, float alpha, EventHandler Event, int type) {

        t = type;
        this.ID = ID;
        this.text = ID;
        this.alpha = alpha;

        this.style = style;
        this.event = Event;
        base = basevalue;
        Graphics.setFont(style.GetFont());

        X = ProsX(x, null);
        Y = ProsY(y, null);

        Width = ProsWidth(width, null);
        Height = ProsHeight(height, null);

        spr = new Sprite();

        tx = new TextBox(basevalue, style, 2, "", (t & menu.AT_TMIN) != 0);
    }

    public AdjustButton(String ID, int x, int y, int width, int height, String basevalue,
                        StyleItem style, float alpha, EventHandler Event, int type, String regex) {

        t = type;
        this.ID = ID;
        this.text = ID;
        this.alpha = alpha;

        this.style = style;
        this.event = Event;
        base = basevalue;
        Graphics.setFont(style.GetFont());

        X = ProsX(x, null);
        Y = ProsY(y, null);

        Width = ProsWidth(width, null);
        Height = ProsHeight(height, null);

        spr = new Sprite();
        tx = new TextBox(basevalue, style, 2, regex, (t & menu.AT_TMIN) != 0);
    }

    public void logic(menu m) {
        Graphics.setFont(style.GetFont());

        X = ProsX(X, m);
        Y = ProsY(Y, m);
        Width = ProsWidth(Width, m);
        Height = ProsHeight(Height, m);

        if((t & menu.AT_TEXT) != 0 || (t & menu.AT_BTN) != 0) {
            if ((t & 0xFF) == menu.AT_NONE) {
                TextBox = new bounds((X & menu.NORMALBITS) + Off.x, (Y & menu.NORMALBITS) + Off.y, (X & menu.NORMALBITS) + Off.x +
                        (Width & menu.NORMALBITS), (Y & menu.NORMALBITS) + Off.y + (Height & menu.NORMALBITS));

            }

            if ((t & menu.AT_TEXT) != 0) {
                if (Mouse.IsInArea(TextBox, ThisDisable) &&
                        (Mouse.IsClicked(MouseUtil.LEFT,ThisDisable) || Mouse.IsClicked(MouseUtil.RIGHT, ThisDisable))) {
                    isActive = true;
                    v.BlockControls = true;
                    tx.Edit();
                    SP.repaint();

                } else if (isActive) {
                    tx.logic();

                    if (!tx.isEditing()) {
                        base = tx.GetText();

                        isActive = false;
                        v.BlockControls = false;
                        SP.repaint();

                        if(event != null) {
                            event.setString(String.valueOf(base));
                            Event.SetEvent(event);
                        }
                    }
                }
            }
        }

        if (Mouse.IsInArea(new bounds((X & menu.NORMALBITS) + Off.x, (Y & menu.NORMALBITS) + Off.y, (X & menu.NORMALBITS) + Off.x +
                (Width & menu.NORMALBITS), (Y & menu.NORMALBITS) + Off.y + (Height & menu.NORMALBITS)), ThisDisable)) {

            ThisDisable = true;
            v.BlockControls = true;
            SP.repaintLater();

        } else if (ThisDisable && !tx.isEditing()) {
            ThisDisable = false;
            v.BlockControls = false;
            SP.repaintLater();
        }
    }

    public void draw(Graphics g) {
        spr.setColor(Colors.GetColor("normal", alpha));
        spr.setSize(Width & menu.NORMALBITS, Height & menu.NORMALBITS);
        spr.setPosition((X & menu.NORMALBITS) + Off.x, (Y & menu.NORMALBITS) + Off.y);
        g.fillRect(spr);

        if((t & menu.AT_BTN) != 0 || (t & menu.AT_TEXT) != 0) {
            spr.setColor(ProcessButton(TextBox));
            spr.setSize(Width & menu.NORMALBITS, Height & menu.NORMALBITS);
            spr.setPosition((X & menu.NORMALBITS) + Off.x, (Y & menu.NORMALBITS) + Off.y);
            g.fillRect(spr);
        }

        if(!isActive) {
            if((t & 0xFF) == menu.AT_NONE){
                g.setColor(style.GetColor());
                Graphics.setFont(style.GetFont());
                g.drawText(text + ": " + base, (X & menu.NORMALBITS) + Off.x, (int) ((Y & menu.NORMALBITS) + Off.y +
                                ((Height & menu.NORMALBITS) / 2) - (Graphics.GetTextHeight(text + ": " + base) / 2)),
                        style.GetAlignment(), Width & menu.NORMALBITS);

            }

        } else {
            tx.draw(g, TextBox);
        }
    }

    private Color ProcessButton(bounds bounds) {
        if(bounds != null && Mouse.IsInArea(bounds, ThisDisable)){

            if(Mouse.IsClicked(MouseUtil.LEFT, ThisDisable) || Mouse.IsClicked(MouseUtil.RIGHT, ThisDisable)) {
                if(event != null) {
                    event.setString(String.valueOf(base));
                    Event.SetEvent(event);
                }

                v.BlockControls = true;
                v.UnlockEndFrame = true;
                return Colors.GetColor("click", alpha);

            } else {
                return Colors.GetColor("high", alpha);
            }

        }
            return Colors.GetColor("normal", alpha);
    }

    public void dispose(){
    }

    private int ProsX(int x, menu m) {
        int s = x & menu.COMMANDBITS, h = x & menu.NORMALBITS;      // get separate command and offset positions

        if(s != 0) {    // check if command bits are not set

            if ((s & menu.GetStyleSize()) != 0) {   // this is not available for this type, return 0
                return 0;

            } else if ((s & menu.GetScreenSize()) != 0) {       // gets screen width as xpos
                return (menu.GetScreenSize() + App.GetBounds().w);

            } else if (m != null && (s & menu.After()) != 0) {
                // gets position and width of object at offset h of the parent menu, and sets it as X
                return (m.GetMenu(h).X & menu.NORMALBITS) + (m.GetMenu(h).Width & menu.NORMALBITS);

            }
        }

        return x;
    }

    private int ProsY(int y, menu m) {
        int s = y & menu.COMMANDBITS, h = y & menu.NORMALBITS;
        if(s != 0){

            if((s & menu.GetStyleSize()) != 0){
                return 0;

            } else if((s & menu.GetScreenSize()) != 0){
                return (menu.GetScreenSize() + App.GetBounds().h);

            } else if (m != null && (s & menu.After()) != 0) {
                return (m.GetMenu(h).Y & menu.NORMALBITS) + (m.GetMenu(h).Height & menu.NORMALBITS);

            }
        }

        return y;
    }

    private int ProsHeight(int height, menu m) {
        int s = height & menu.COMMANDBITS, h = height & menu.NORMALBITS;
        if(s != 0){

            if((s & menu.GetStyleSize()) != 0){
                return (int) (Graphics.GetTextHeight(text) + 24 + menu.GetStyleSize());

            } else if((s & menu.GetScreenSize()) != 0){
                return (menu.GetScreenSize() + App.GetBounds().h);

            }
        }

        return height;
    }

    private int ProsWidth(int width, menu m) {
        int s = width & menu.COMMANDBITS, h = width & menu.NORMALBITS;
        if(s != 0){

            if((s & menu.GetStyleSize()) != 0){
                return ((int)Graphics.GetTextWidth(text) + menu.GetStyleSize());

            } else if((s & menu.GetScreenSize()) != 0){
                return (menu.GetScreenSize() + App.GetBounds().w);

            }
        }

        return width;
    }

    public String GetID(){
        return ID;
    }

    public String GetText(){
        return text;
    }

    public String GetBase(){
        return base;
    }

    public MenuEntry SetXYOff(int x, int y){
        Off.x = x;
        Off.y = y;
        return this;
    }

    public MenuEntry SetXYOff(bounds off) {
        Off = off;
        return this;
    }

    public bounds GetXYOff(){
        return Off;
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

    public void SetText(String text) {
        this.text = text;
    }

    public void SetBase(String text) {
        base = text;
        tx.SetText(text);
    }
}
