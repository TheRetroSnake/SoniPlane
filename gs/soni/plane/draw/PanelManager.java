package gs.soni.plane.draw;

import gs.app.lib.application.App;
import gs.app.lib.gfx.Graphics;
import gs.app.lib.gfx.Sprite;
import gs.app.lib.gfx.gfx;
import gs.app.lib.math.bounds;
import gs.app.lib.util.MouseUtil;
import gs.soni.plane.SP;
import gs.soni.plane.util.*;
import gs.soni.plane.v;

import javax.swing.*;
import java.awt.*;
import java.util.*;

public class PanelManager extends JPanel {
    /* list of listeners for this Window */
    ArrayList<PanelListener> panL;
    /* the Window itself */
    private Window win;
    /* whether this is focused or not */
    private boolean focus;
    /* display name */
    private String name;
    /* buttons to display and ID of button hovered over */
    private int btn;
    private int buttonHover = -1;
    /* if is minimized and the boundaries it had once was big */
    private boolean minimized;
    private bounds old;
    private bounds moving;
    private int resizing = 0;

    public PanelManager(Window w, String name, int buttons) {
        /* create new listener arrayList */
        panL = new ArrayList<PanelListener>();
        /* add this to the main JPanel of the app */
        App.getJPanel().add(this);

        /* set visible, focusable and remove border */
        setVisible(true);
        setBorder(null);
        setFocusable(true);
        /* set boundaries */
        setBounds(w.getBounds().x, w.getBounds().y, w.getBounds().w, w.getBounds().h);

        /* set up variables */
        win = w;
        this.name = name;
        btn = buttons;
    }

    /* set close signal to listeners and close */
    public void kill() {
        /* tell listeners window are closed */
        for(PanelListener p : panL){
            p.close();
        }

        /* remove from JPanel */
        App.getJPanel().remove(this);
    }

    /* get the Window object */
    public Window getWindow() {
        return win;
    }

    /* get boundaries of Window object */
    public bounds getBound(){
        return win.getBounds();
    }

    @Override
    public void paintComponent(java.awt.Graphics g) {
        /* get gs.app.lib.gfx.Graphics context from java.awt.Graphics */
        Graphics gr = new Graphics((Graphics2D) g);

        /* if window exists (safety reasons) */
        if(win != null) {
            /* set new default boundaries */
            defaultBounds();

            /* call draw method on listeners */
            for (PanelListener p : panL) {
                p.draw();
            }

            /* if the window draws boundaries */
            if(win.drawBound()){
                /* if is not minimized, draw black area to fill the bg, and the window itself */
                if(!minimized) {
                    gr.fillRect(BOUNDS_SIZE, UPPER_HEIGHT, win.getBounds().w, win.getBounds().h, Color.BLACK);
                    win.draw(gr, new bounds(BOUNDS_SIZE, UPPER_HEIGHT, BOUNDS_SIZE, BOUNDS_SIZE), 1f);
                }

                /* if is focused, draw blue-ish boundaries */
                if (isFocus()) {
                    DrawWindow(gr, Colors.GetColor("window-focus2"), Colors.GetColor("window-focus"), name, Style.GetStyle("window"), 1);

                /* if not focused, draw gray boundaries */
                } else {
                    DrawWindow(gr, Colors.GetColor("window-normal2"), Colors.GetColor("window-normal"), name, Style.GetStyle("window"), 0);
                }

            /* if doesn't draw boundaries, draw normally */
            } else {
                win.draw(gr, new bounds(0, 0, 0, 0), 1f);
            }
        }
    }

    /* set boundaries for the JPanel */
    public void defaultBounds() {
        /* if we should draw boundaries around it */
        if (win.drawBound()) {
            setBounds(win.getBounds().x - BOUNDS_SIZE, win.getBounds().y - UPPER_HEIGHT,
                    win.getBounds().w + (BOUNDS_SIZE * 2), win.getBounds().h + UPPER_HEIGHT + BOUNDS_SIZE);

        } else {
            /* if not, draw normally */
            setBounds(win.getBounds().x, win.getBounds().y, win.getBounds().w, win.getBounds().h);
        }
    }

    /* variables for window drawing */
    public static final int BOUNDS_SIZE =  windowManager.BOUNDS_SIZE;
    public static final int UPPER_HEIGHT = windowManager.UPPER_HEIGHT;
    public static final int BTN_SIZE =      9;

    /* draw the window boundaries */
    private void DrawWindow(Graphics g, Color trans, Color solid, String text, StyleItem style, int imgOff) {
        /* create new dummy sprite with color for boundary drawing */
        Sprite s = new Sprite();
        s.setColor(solid);
        /* draw left, right and bottom boundaries of the window */
        v.DrawBounds(g, s, new bounds(BOUNDS_SIZE, BOUNDS_SIZE + UPPER_HEIGHT, win.getBounds().w, win.getBounds().h - BOUNDS_SIZE),
                BOUNDS_SIZE, BOUNDS_SIZE, new bounds(1, 0, 1, 1));
        /* draw the darker background color for the window */
        g.fillRect(UPPER_HEIGHT, 0, win.getBounds().w - (UPPER_HEIGHT * 2) + (BOUNDS_SIZE * 2), UPPER_HEIGHT, trans);

        /* get darker corner piece */
        s = new Sprite(v.corner[imgOff + 2]);
        s.setFlip(true, false);
        s.setBounds(win.getBounds().w - UPPER_HEIGHT + (BOUNDS_SIZE * 2), 0, UPPER_HEIGHT, UPPER_HEIGHT);
        s.setAlpha(1f);
        /* and draw it */
        g.drawImage(s);

        /* get the normal corner piece */
        s = new Sprite(v.corner[imgOff]);
        s.setBounds(0, 0, UPPER_HEIGHT, UPPER_HEIGHT);
        s.setAlpha(1f);
        /* and draw it */
        g.drawImage(s);

        /* set font, and get its width, and if subtraction value if too big */
        Graphics.setFont(style.GetFont());
        int width = (int) Graphics.GetTextWidth(text), width2 = width - (UPPER_HEIGHT / 2),
                sub = getLastButtonPos() - width2 - (int) (UPPER_HEIGHT * 2.6f);

        /* if text is too wide for buttons, make it smaller here */
        if(sub < 0){
            width2 += sub;
        }

        /* fill the normal color in */
        g.fillRect(UPPER_HEIGHT, 0, width2, UPPER_HEIGHT, solid);

        /* get the ending slope */
        s = new Sprite(v.slope[imgOff]);
        s.setBounds(UPPER_HEIGHT + width2, 0, (UPPER_HEIGHT * 2), UPPER_HEIGHT);
        s.setAlpha(1f);
        /* draw it */
        g.drawImage(s);

        /* draw the text, and if needed, shorten it first */
        g.drawText(sub >= 0 ? text : GetShorterText(text, width2 +(BOUNDS_SIZE * 2)), BOUNDS_SIZE +(UPPER_HEIGHT / 2), 0, style.GetColor());
        if(focus) {
            /* if focused, also draw the buttons */
            DrawButtons(g);
        }
    }

    /* if text does not fit the provided area, shorten the text until it fits */
    private String GetShorterText(String text, int size) {
        /* loop while text size is greater than allowed space */
        while(Graphics.GetTextWidth(text) > size){
            /* sub 1 character from string */
            text = text.substring(0, text.length() - 1);

            /* if text length is less than 1 */
            if(text.length() < 1){
                return ".";
            }
        }

        /* return text */
        return text +".";
    }

    /* draw buttons to the window */
    private void DrawButtons(Graphics g) {
        /* list of button art files */
        String[] img = new String[]{ "/res/remove.png", "/res/minimize.png", "/res/defaultsize.png" };
        /* initial x position and buttons to draw */
        int x = win.getBounds().w, but = minimized ? btn & 0x3 : btn;
        Sprite b = new Sprite();

        /* check for all buttons */
        for(int i = -1;i < 8;i ++){
            /* get bitmask */
            int bit = (2 << i) == 0 ? 1 : (2 << i);

            /* check the bitmask */
            if((but & bit) != 0){
                /* increase the x position for button position */
                x -= UPPER_HEIGHT;
                /* create new bounds to draw at and check if is hovering over */
                bounds a = new bounds(x, ((UPPER_HEIGHT - BTN_SIZE) / 3) * 2, BTN_SIZE, BTN_SIZE);
                boolean isHover = Mouse.IsInArea(new bounds(win.getBounds().x - BOUNDS_SIZE + a.x,
                        win.getBounds().y - UPPER_HEIGHT + a.y, a.w, a.h).toCoordinates());

                /* get color to draw with and set it */
                float c = isHover ? 0f : 0.25f;
                b.setColor(new Color(c, c, c, 1f));
                /* draw boundaries around the button */
                v.DrawBounds(g, b, a, 2, isHover ? 2 : 1);

                /* create new sprite with correct image inserted */
                Sprite s = new Sprite(gfx.getImage(v.LaunchAdr + img[i + 1]));
                s.setAlpha(1f);
                s.setBounds(x, a.y, a.w, a.h);
                /* draw it */
                g.drawImage(s);
            }
        }
    }

    public void logic() {
        /* call logic methods for all listeners */
        for (PanelListener p : panL) {
            p.logic(isFocus());
        }

        /* if not moving or resizing, use normal logic */
        if(moving == null && resizing == 0) {
        /* if is focused, mouse is in the window, and is not minimized */
            if (isFocus() && Mouse.IsInArea(win.getBounds().toCoordinates()) && !minimized) {
                win.logic();

            }

        /* check button logic */
            if (!checkButtons()) {
            /* if not hovering over a button, check if we should attempt to resize */
                if (!checkResize()) {
                    if (!checkMove()) {
                        toCursor_(CursorList.get(Cursor.DEFAULT_CURSOR));
                    }
                }

            } else {
            /* if hovered over a button, set cursor type */
                toCursor_(CursorList.get(Cursor.HAND_CURSOR));
            }
        } else {
            /* if we are not resizing, move the window */
            if(resizing == 0) {
                moveWindow();

            /* if we are not moving, resize window */
            } else if(moving == null){
                resizeWindow();

            /* if doing both, cancel them */
            } else {
                moving = null;
                resizing = 0;
            }
        }
    }

    /* resize the window */
    private void resizeWindow() {
        /* variables used */
        boolean repaint = false;
        int w = win.getBounds().w, h = win.getBounds().h, ow = Integer.MAX_VALUE, oh = Integer.MAX_VALUE;

        /* if resizing from right */
        if ((resizing & 1) != 0) {
            repaint = true;
            w = MouseUtil.getX() - win.getBounds().x;

        /* if resizing from the left */
        } else if ((resizing & 2) != 0) {
            repaint = true;
            w -= MouseUtil.getX() - win.getBounds().x;
            ow = win.getBounds().w;
        }

        /* if resizing from top */
        if ((resizing & 4) != 0) {
            repaint = true;
            h -= (MouseUtil.getY() + UPPER_HEIGHT - BOUNDS_SIZE) - win.getBounds().y;
            oh = win.getBounds().h;

        /* if resizing from bottom */
        } else if ((resizing & 8) != 0) {
            repaint = true;
            h = MouseUtil.getY() - win.getBounds().y;
        }

        /* if we should repaint */
        if (repaint) {
            /* call resize on the panel and fix the size */
            win.resize(w, h);
            fixSize();

            /* if the x-pos should be changed */
            if(ow != Integer.MAX_VALUE){
                win.getBounds().x -= win.getBounds().w - ow;
            }

            /* if the y-pos should be changed */
            if(oh != Integer.MAX_VALUE){
                win.getBounds().y -= win.getBounds().h - oh;
            }

            /* force on screen and repaint */
            SP.getWM().forceOnScreen(win);
            SP.repaint();
            repaint();
        }

        /* if not pressing left mouse button */
        if (!Mouse.IsHeld(MouseUtil.LEFT)) {
            /* tell listeners window is resize */
            for(PanelListener p : panL){
                p.move(win.getBounds().w, win.getBounds().h);
            }

            /* stop resizing */
            resizing = 0;
            /* reset the display and repaint */
            resetDisplay();
            SP.repaint();
        }
    }

    /* make sure the panel is not too big */
    private void fixSize() {
        /* if height is too small, change it back */
        if(win.getBounds().h < 1){
            win.getBounds().h = 1;

        /* if height is too high, change to max */
        } else if(win.getBounds().h > App.GetBounds().h - BOUNDS_SIZE - UPPER_HEIGHT){
            win.getBounds().h = App.GetBounds().h - BOUNDS_SIZE - UPPER_HEIGHT;
        }

        /* if is not width enough, change to normal width */
        if(win.getBounds().w < win.getBounds().w - getLastButtonPos() + (UPPER_HEIGHT * 2) + (BOUNDS_SIZE * 2)){
            win.getBounds().w = win.getBounds().w - getLastButtonPos() + (UPPER_HEIGHT * 2) + (BOUNDS_SIZE * 2);

        /* if is too wide, change to max size */
        } else if(win.getBounds().w > App.GetBounds().w - (BOUNDS_SIZE * 2)){
            win.getBounds().w = App.GetBounds().w - (BOUNDS_SIZE * 2);
        }
    }

    /* move the window */
    private void moveWindow() {
        /* set new positions */
        win.getBounds().x = MouseUtil.getPoint().x - App.GetBounds().x - moving.x;
        win.getBounds().y = MouseUtil.getPoint().y - App.GetBounds().y - moving.y;

        /* tell the panel we moved */
        win.move(win.getBounds().x, win.getBounds().y);
        /* force on screen and repaint */
        SP.getWM().forceOnScreen(win);
        SP.repaintLater();
        repaintLater();

        /* if not pressing left mouse button */
        if (!Mouse.IsHeld(MouseUtil.LEFT)) {
            moving = null;
            /* tell listeners window is moved */
            for(PanelListener p : panL){
                p.move(win.getBounds().x, win.getBounds().y);
            }

            /* reset display and repaint */
            resetDisplay();
            SP.repaint();
        }
    }

    /* check if we need to move the window */
    private boolean checkMove() {
        /* if is focused and mouse is in the header */
        if(focus && Mouse.IsInArea(new bounds(getX() + BOUNDS_SIZE, getY() + BOUNDS_SIZE,
                getLastButtonPos() - BOUNDS_SIZE, UPPER_HEIGHT - BOUNDS_SIZE).toCoordinates())){

            /* if is pressing left mouse button */
            if(Mouse.IsClicked(MouseUtil.LEFT)){
                /* create the grab2 cursor */
                toCursor_(CursorList.get(CursorList.GRAB2_CURSOR));
                /* make new bounds with relative cursor position */
                moving = new bounds(MouseUtil.getX() - getBound().x, MouseUtil.getY() - getBound().y, 0, 0);

            } else {
                /* create grab cursor */
                toCursor_(CursorList.get(CursorList.GRAB_CURSOR));
            }
            return true;
        }
        return false;
    }

    /* check if we should resize */
    private boolean checkResize() {
        /* get outer and inner boundaries */
        bounds outer = getWindowBounds().toCoordinates(),
                inner = new bounds(outer.x + BOUNDS_SIZE, outer.y + BOUNDS_SIZE, outer.w - BOUNDS_SIZE, outer.h - BOUNDS_SIZE);

        /* if is focused, is not minimized and is inside outer bounds but not inner */
        if(focus && !minimized && Mouse.IsInArea(outer) && !Mouse.IsInArea(inner)){
            /* get cursor type */
            int type = setResizeCursor(MouseUtil.getX() - outer.x, MouseUtil.getY() - outer.y);

            /* if left mouse button is clicked, resize */
            if(Mouse.IsClicked(MouseUtil.LEFT)){
                resizing = type;
            }
            return true;
        }
        return false;
    }

    /* set cursor based on position in resizing */
    private int setResizeCursor(int cursorX, int cursorY) {
        /* set offset to 0 and create table for types */
        int off = 0;
        int[] types = new int[]{
                Cursor.DEFAULT_CURSOR,  Cursor.E_RESIZE_CURSOR, Cursor.W_RESIZE_CURSOR, 0,
                Cursor.N_RESIZE_CURSOR, Cursor.NE_RESIZE_CURSOR, Cursor.NW_RESIZE_CURSOR, 0,
                Cursor.S_RESIZE_CURSOR, Cursor.SE_RESIZE_CURSOR, Cursor.SW_RESIZE_CURSOR, };

        /* if left side */
        if(cursorX <= BOUNDS_SIZE){
            off += 2;

        /* if right side */
        } else if(cursorX >= getWindowBounds().w - BOUNDS_SIZE){
            off ++;
        }

        /* if upper side */
        if(cursorY <= BOUNDS_SIZE){
            off += 4;

        /* if lower size */
        } else if(cursorY >= getWindowBounds().h - BOUNDS_SIZE){
            off += 8;
        }

        /* set cursor and return offset */
        toCursor_(CursorList.get(types[off]));
        return off;
    }

    /* execute logic to check if button is pressed */
    private boolean checkButtons() {
        /* variables to check if should repaint the window, x position and buttons to check */
        boolean repaint = false, hovered = false;
        int x = win.getBounds().w, but = minimized ? btn & 0x3 : btn;

        /* run check for all buttons */
        for(int i = -1;i < 8;i ++){
            /* get bitmask */
            int bit = (2 << i) == 0 ? 1 : (2 << i);

            /* check the bitmask */
            if((but & bit) != 0){
                /* increase x position */
                x -= UPPER_HEIGHT;
                /* get boundaries and check if hovering over */
                bounds a = new bounds(x, ((UPPER_HEIGHT - BTN_SIZE) / 3) * 2, BTN_SIZE, BTN_SIZE);
                boolean isHover = Mouse.IsInArea(new bounds(win.getBounds().x - BOUNDS_SIZE + a.x,
                        win.getBounds().y - UPPER_HEIGHT + a.y, a.w, a.h).toCoordinates());

                /* if we didn't hover over the button last frame and now we are */
                if(buttonHover != (i + 1) && isHover){
                    /* set last hover position and repaint */
                    buttonHover = (i + 1);
                    repaint = true;

                /* if we were hovering over this button last time and now we are not */
                } else if(buttonHover == (i + 1) && !isHover){
                    /* set the last button to none and repaint */
                    buttonHover = -1;
                    repaint = true;
                }

                /* if is hovering over and clicked left mouse button */
                if(isHover && Mouse.IsClicked(MouseUtil.LEFT)){
                    /* execute function for this button */
                    buttonFunction(i + 1);
                }

                /* if is hovering over a button, set return value to true */
                if(isHover){
                    hovered = true;
                }
            }
        }

        /* if we should repaint, do so */
        if(repaint){
            repaint();
        }

        /* return whether we hovered a button */
        return hovered;
    }

    /* execute functions of the button pressed */
    private void buttonFunction(int func) {
        switch (func){
            /* function 1: minimize */
            case 1:
                minimize();
                break;

            /* function 2: set default size */
            case 2:
                defaultSize();
                SP.getWM().forceOnScreen(win);
                repaint();
                SP.repaint();
                break;
        }
    }

    /* minimize the window */
    private void minimize() {
        /* change the mode */
        minimized ^= true;
        /* if window is now minimized, set correct size */
        if(minimized) {
            /* copy the boundaries */
            old = new bounds(win.getBounds());
            /* set minimized size */
            win.getBounds().h = 1;
            win.getBounds().w = (int) Graphics.GetTextWidth(getName()) - (UPPER_HEIGHT / 2) + (UPPER_HEIGHT * 3) +
                    (win.getBounds().w - getLastButtonPos());

        /* if window is maximized, set original size */
        } else {
            /* copy width and height from pre-minimized size */
            win.getBounds().w = old.w;
            win.getBounds().h = old.h;
        }

        /* make sure that the window is in screen */
        SP.getWM().forceOnScreen(win);

        /* repaint the window and clear background */
        repaint();
        SP.repaintLater();
    }

    /* get position for last button */
    private int getLastButtonPos(){
        /* get starting x position and bits to check */
        int x = win.getBounds().w, but = minimized ? btn & 0x3 : btn;

        /* check all bits */
        for(int i = -1;i < 8;i ++){
            /* get the bitmask to check (ex: bit 3 -> 0x4) */
            int bit = (2 << i) == 0 ? 1 : (2 << i);

            /* check the bitmask against buttons */
            if((but & bit) != 0){
                /* get next position */
                x -= UPPER_HEIGHT;
            }
        }

        /* return the position */
        return x;
    }

    /* is this window focused? */
    public boolean isFocus() {
        return focus;
    }

    /* set focus to this Window */
    public void setFocus(boolean focus) {
        this.focus = focus;
    }

    /* add Window listener */
    public void addListener(PanelListener p){
        panL.add(0, p);
    }

    /* get the name of the Window */
    public String getName(){
        return name;
    }

    /* set Window's name */
    public void setName(String name){
        this.name = name;
    }

    /* reset Window's display and repaint */
    public void resetDisplay() {
        /* set visible and enabled */
        setVisible(true);
        setEnabled(true);
        /* set boundaries */
        defaultBounds();
        /* repaint after 10 millisecond */
        repaintLater();
    }

    /* repaint after 10 milliseconds */
    public void repaintLater() {
        new java.util.Timer("RenderTimer (10ms) at "+ System.currentTimeMillis() / 1000 +" for "+ win).schedule(new TimerTask() {
            @Override
            public void run() {
                /* repaint */
                repaint();
            }
        }, 10);
    }

    /* get boundaries with Window included */
    public bounds getWindowBounds() {
        bounds b = getBound();
        return new bounds(b.x - BOUNDS_SIZE, b.y - UPPER_HEIGHT, b.w + (BOUNDS_SIZE * 2), b.h + UPPER_HEIGHT + BOUNDS_SIZE);
    }

    /* set default size for the window */
    public void defaultSize() {
        if(!minimized){
            /* if not minimized, call the Window's default size method */
            win.defaultSize();
        }
    }

    public void toCursor(Cursor c) {
        if(!getCursor().getName().equals(c.getName())) {
            /* if the cursor isn't the old cursor, set it */
            setCursor(c);
        }
    }

    /* set cursor and check if the panel overrides cursor type */
    private void toCursor_(Cursor c) {
        if(!win.cursorOverride()){
            /* if window doesn't override cursor, set cursor */
            toCursor(c);
        }
    }
}