package gs.soni.plane.menu;

import gs.app.lib.application.App;
import gs.app.lib.gfx.Graphics;
import gs.app.lib.gfx.Sprite;
import gs.app.lib.math.bounds;
import gs.app.lib.util.MouseUtil;
import gs.soni.plane.SP;
import gs.soni.plane.util.*;
import gs.soni.plane.v;

public class MenuEntry {
    private EventHandler event;

    private String ID;
    private String text;
    public int X;
    public int Y;
    public int Width;
    public int Height;
    private StyleItem style;
    private float alpha;
    private Sprite spr;

    private bounds Off = new bounds(0, 0, 0, 0);
    private boolean ThisDisable = false;
    private boolean isHigh = false;

    public MenuEntry(String ID, String text, int x, int y, int width, int height, StyleItem style, float alpha,
                     EventHandler Event) {
        this.ID = ID;
        this.text = text;
        this.alpha = alpha;

        this.style = style;
        this.event = Event;
        Graphics.setFont(style.GetFont());

        X = ProsX(x, null);
        Y = ProsY(y, null);

        Width = ProsWidth(width, null);
        Height = ProsHeight(height, null);

        spr = new Sprite();
    }

    public MenuEntry() {
    }

    public void logic(menu m) {
        GetColorMode();
        Graphics.setFont(style.GetFont());

        X = ProsX(X, m);
        Y = ProsY(Y, m);
        Width = ProsWidth(Width, m);
        Height = ProsHeight(Height, m);

        if(Mouse.IsInArea(new bounds((X & menu.NORMALBITS) + Off.x, (Y & menu.NORMALBITS) + Off.y, (X & menu.NORMALBITS) +
                (Width & menu.NORMALBITS) + Off.x, (Y & menu.NORMALBITS) + (Height & menu.NORMALBITS) + Off.y), ThisDisable)){

            ThisDisable = true;
            v.BlockControls = true;
        } else if(ThisDisable){
            ThisDisable = false;
            v.BlockControls = false;
        }
    }

    public void draw(Graphics g) {
        spr.setSize(Width & menu.NORMALBITS, Height & menu.NORMALBITS);
        spr.setPosition((X & menu.NORMALBITS) + Off.x, (Y & menu.NORMALBITS) + Off.y);
        g.fillRect(spr);

        Graphics.setFont(style.GetFont());
        g.setColor(style.GetColor());
        g.drawText(text, (X & menu.NORMALBITS) + Off.x, (int) ((Y & menu.NORMALBITS) + Off.y + (((Height & menu.NORMALBITS) / 2) -
                (Graphics.GetTextHeight(text) / 2))),  style.GetColor(), style.GetAlignment(), Width & menu.NORMALBITS);

    }

    public void dispose(){
    }

    private void GetColorMode() {
        if(Mouse.IsInArea(new bounds((X & menu.NORMALBITS) + Off.x, (Y & menu.NORMALBITS) + Off.y, (X & menu.NORMALBITS) +
                (Width & menu.NORMALBITS) + Off.x, (Y & menu.NORMALBITS) + (Height & menu.NORMALBITS) + Off.y), ThisDisable)){

            if(Mouse.IsClicked(MouseUtil.LEFT, ThisDisable) || Mouse.IsClicked(MouseUtil.RIGHT, ThisDisable)) {
                spr.setColor(Colors.GetColor("click", alpha));
                Event.SetEvent(event);

                v.BlockControls = true;
                v.UnlockEndFrame = true;
                isHigh = false;
                SP.repaint();

            } else {
                spr.setColor(Colors.GetColor("high", alpha));
                if(!isHigh){
                    isHigh = true;
                    SP.repaint();
                }
            }

        } else {
            spr.setColor(Colors.GetColor("normal", alpha));
            if(isHigh){
                isHigh = false;
                SP.repaint();
            }
        }
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
                return (int) (Graphics.GetTextWidth(text) + menu.GetStyleSize());

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

    public int GetWidth() {
        return Width;
    }

    public int GetHeight() {
        return Height;
    }

    public void SetText(String text) {
        this.text = text;
    }

    public void SetBase(String text) {
        this.text = text;
    }

    public String GetBase(){
        return text;
    }
}
