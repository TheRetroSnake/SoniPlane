package gs.soni.plane.menu;

import gs.app.lib.application.App;
import gs.app.lib.gfx.Graphics;
import gs.app.lib.gfx.Sprite;
import gs.app.lib.gfx.gfx;
import gs.app.lib.math.bounds;
import gs.app.lib.util.MouseUtil;
import gs.soni.plane.SP;
import gs.soni.plane.draw.TextBox;
import gs.soni.plane.util.*;
import gs.soni.plane.v;

import java.awt.image.BufferedImage;

public class ProjEntry extends MenuEntry {
    private EventHandler[] event;

    private String ID;
    private String text;
    private int X;
    private int Y;
    private int Width;
    private int Height;
    private int btn;
    private int lastBtn;
    private StyleItem style;
    private float alpha;
    private Sprite spr;

    private bounds Off = new bounds(0, 0, 0, 0);
    private boolean ThisDisable = false;
    private boolean big;
    private TextBox tx;
    private boolean isActive;

    private final int maxBit = 28;

    public ProjEntry(String ID, String text, int x, int y, int width, int height, StyleItem style, float alpha,
                     EventHandler[] Event, int buttons) {

        this.ID = ID;
        this.text = text;
        this.alpha = alpha;
        isActive = false;

        this.style = style;
        this.event = Event;
        btn = buttons & 0x3FFFFFFF;
        lastBtn = -1;
        Graphics.setFont(style.GetFont());

        X = ProsX(x, null);
        Y = ProsY(y, null);

        Width = ProsWidth(width, null);
        Height = ProsHeight(height, null);
        spr = new Sprite();

        big = (buttons & 0x40000000) != 0;
        if((buttons & 0x80000000) != 0){
            tx = new TextBox(text, style, "", (buttons & 0x20000000) != 0);
        }

        if(Event == null){
            event = new EventHandler[31];
        }
    }

    public ProjEntry(String ID, String text, String regex, int x, int y, int width, int height, StyleItem style, float alpha,
                     EventHandler[] Event, int buttons) {

        this.ID = ID;
        this.text = text;
        this.alpha = alpha;
        isActive = false;

        this.style = style;
        this.event = Event;
        btn = buttons & 0x3FFFFFFF;
        lastBtn = -1;
        Graphics.setFont(style.GetFont());

        X = ProsX(x, null);
        Y = ProsY(y, null);

        Width = ProsWidth(width, null);
        Height = ProsHeight(height, null);
        spr = new Sprite();

        big = (buttons & 0x40000000) != 0;
        if((buttons & 0x80000000) != 0){
            tx = new TextBox(text, style, regex, (buttons & 0x20000000) != 0);
        }

        if(Event == null){
            event = new EventHandler[31];
        }
    }

    public void logic(menu m) {
        Graphics.setFont(style.GetFont());

        X = ProsX(X, m);
        Y = ProsY(Y, m);
        Width = ProsWidth(Width, m);
        Height = ProsHeight(Height, m);
        int last = CheckButtonBounds();

        if(tx != null && isActive) {
            ThisDisable = true;
            v.BlockControls = true;
            tx.logic();

            if (!tx.isEditing()) {
                text = tx.GetText();
                isActive = false;
                SP.repaint();
            }


        } else if(last > -2){
            ThisDisable = true;
            v.BlockControls = true;
            if(last != lastBtn) {
                lastBtn = last;
                SP.repaintLater();
            }

            if(Mouse.IsClicked(MouseUtil.LEFT, ThisDisable) || Mouse.IsClicked(MouseUtil.RIGHT, ThisDisable)) {
                Event.SetEvent(event[CheckButtonOffsetBounds()]);
            }

        } else if(Mouse.IsInArea(new bounds((X & menu.NORMALBITS) + Off.x, (Y & menu.NORMALBITS) + Off.y, (X & menu.NORMALBITS) +
               (Width & menu.NORMALBITS) + Off.x, (Y & menu.NORMALBITS) + (Height & menu.NORMALBITS) + Off.y), ThisDisable) && last == -2){

            ThisDisable = true;
            v.BlockControls = true;
            if(lastBtn != -2) {
                lastBtn = -2;
                SP.repaintLater();
            }

            if(Mouse.IsClicked(MouseUtil.LEFT, ThisDisable) || Mouse.IsClicked(MouseUtil.RIGHT, ThisDisable)) {
                Event.SetEvent(event[0]);
                if(tx != null){
                    tx.Edit();
                    isActive = true;
                }
                SP.repaint();
            }

        } else if(ThisDisable){
            ThisDisable = false;
            v.BlockControls = false;
            if(lastBtn != -3) {
                lastBtn = -3;
                SP.repaintLater();
            }
        }
    }

    private int CheckButtonBounds() {
        int x = 1, width = big ? Height & menu.NORMALBITS : (Height & menu.NORMALBITS) / 2;
        for(int i = -1;i < maxBit;i ++){
            if((btn & ((2 << i) == 0 ? 1 : (2 << i))) != 0){

                int xp = (X & menu.NORMALBITS) + Off.x + (Width & menu.NORMALBITS) - (x * width), yp = (Y & menu.NORMALBITS) + Off.y;
                if(Mouse.IsInArea(new bounds(xp, yp, xp + width, yp + width), ThisDisable)){
                    return i;
                }
                x ++;
            }
        }
        return -2;
    }

    private int CheckButtonOffsetBounds() {
        int x = 1, width = big ? Height & menu.NORMALBITS : (Height & menu.NORMALBITS) / 2;
        for(int i = -1;i < maxBit;i ++){
            if((btn & ((2 << i) == 0 ? 1 : (2 << i))) != 0){

                int xp = (X & menu.NORMALBITS) + Off.x + (Width & menu.NORMALBITS) - (x * width), yp = (Y & menu.NORMALBITS) + Off.y;
                if(Mouse.IsInArea(new bounds(xp, yp, xp + width, yp + width), ThisDisable)){
                    return x;
                }
                x ++;
            }
        }
        return -1;
    }

    public void draw(Graphics g) {
        spr.setBounds((X & menu.NORMALBITS) + Off.x, (Y & menu.NORMALBITS) + Off.y, Width & menu.NORMALBITS, Height & menu.NORMALBITS);
        GetMainColor();
        g.fillRect(spr);

        Graphics.setFont(style.GetFont());
        g.setColor(style.GetColor());
        if(tx == null){
            g.drawText(text, (X & menu.NORMALBITS) + Off.x, (int) ((Y & menu.NORMALBITS) + Off.y + (((Height & menu.NORMALBITS) / 2) -
                    (Graphics.GetTextHeight(text) / 2))),  style.GetColor(), style.GetAlignment(), Width & menu.NORMALBITS);

        } else {
            if (!tx.isEditing()) {
                g.drawText(ID +": "+ text, (X & menu.NORMALBITS) + Off.x, (int) ((Y & menu.NORMALBITS) + Off.y +
                        (((Height & menu.NORMALBITS) / 2) - (Graphics.GetTextHeight(text) / 2))),
                        style.GetColor(), style.GetAlignment(), Width & menu.NORMALBITS);

            } else {
                tx.draw(g, new bounds((X & menu.NORMALBITS) + Off.x, (Y & menu.NORMALBITS) + Off.y,
                        (X & menu.NORMALBITS) + Off.x + (Width & menu.NORMALBITS),
                        (Y & menu.NORMALBITS) + Off.y +(Height & menu.NORMALBITS)));
            }
        }

        int x = 1, width = big ? Height & menu.NORMALBITS : (Height & menu.NORMALBITS) / 2;
        for(int i = -1;i < maxBit;i ++){
            if((btn & ((2 << i) == 0 ? 1 : (2 << i))) != 0){

                spr = new Sprite(GetImage(i + 1));
                spr.setAlpha(1f);
                GetColorMode(x);
                spr.setBounds((X & menu.NORMALBITS) + Off.x + (Width & menu.NORMALBITS) - (x * width),
                        (Y & menu.NORMALBITS) + Off.y, width, width);

                g.fillRect(spr);
                g.drawImage(spr);
                x ++;
            }
        }
    }

    private BufferedImage GetImage(int id) {
        String[] it = new String[]{ "/res/remove.png", "/res/edit.png", "/res/dots.png", };
        return gfx.getImage(v.LaunchAdr + it[id]);
    }

    private void GetMainColor() {
        if(Mouse.IsInArea(new bounds((X & menu.NORMALBITS) + Off.x, (Y & menu.NORMALBITS) + Off.y, (X & menu.NORMALBITS) +
                (Width & menu.NORMALBITS) + Off.x, (Y & menu.NORMALBITS) + (Height & menu.NORMALBITS) + Off.y), ThisDisable) &&
                lastBtn == -2){

            if(Mouse.IsClicked(MouseUtil.LEFT, ThisDisable) || Mouse.IsClicked(MouseUtil.RIGHT, ThisDisable)) {
                spr.setColor(Colors.GetColor("click", alpha));
                v.BlockControls = true;
                v.UnlockEndFrame = true;

            } else {
                spr.setColor(Colors.GetColor("high", alpha));
            }

        } else {
            spr.setColor(Colors.GetColor("normal", alpha));
        }
    }

    public void dispose(){
    }

    private void GetColorMode(int indx) {
        int width = big ? Height & menu.NORMALBITS : (Height & menu.NORMALBITS) / 2,
                xp = (X & menu.NORMALBITS) + Off.x + (Width & menu.NORMALBITS) - (width * indx), yp = (Y & menu.NORMALBITS) + Off.y;

        if(Mouse.IsInArea(new bounds(xp, yp, xp + width, yp + width), ThisDisable)){
            if(Mouse.IsClicked(MouseUtil.LEFT, ThisDisable) || Mouse.IsClicked(MouseUtil.RIGHT, ThisDisable)) {
                spr.setColor(Colors.GetColor("click", alpha));
                v.BlockControls = true;
                v.UnlockEndFrame = true;

            } else {
                spr.setColor(Colors.GetColor("high", alpha));
            }

        } else {
            spr.setColor(Colors.GetColor("normal", alpha));
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
                return (m.GetMenu(h).GetY() & menu.NORMALBITS) + (m.GetMenu(h).GetHeight() & menu.NORMALBITS);
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
        if(tx != null) {
            tx.SetText(text);
        }
    }

    public String GetBase(){
        return text;
    }
}
