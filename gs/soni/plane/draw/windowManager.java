package gs.soni.plane.draw;

import gs.app.lib.application.App;
import gs.app.lib.gfx.Graphics;
import gs.app.lib.math.bounds;
import gs.app.lib.util.KeyUtil;
import gs.soni.plane.SP;
import gs.soni.plane.util.*;
import gs.soni.plane.v;

import java.awt.*;
import java.util.ArrayList;

public class windowManager implements Drawable, Logicable {
    /* list of panelManagers */
    private ArrayList<PanelManager> win;
    /* default sizes for parts of the window */
    public static final int BOUNDS_SIZE =   4;
    public static final int UPPER_HEIGHT = 16;

    public windowManager(){
    //    SP.addToRenderList(this);
        win = new ArrayList<PanelManager>();
    }

    @Override
    public void draw(Graphics g) {
    }

    @Override
    public int renderPriority() {
        return v.RENDERPR_MIN;
    }

    @Override
    public void logic() {
        /* initialize variables */
        boolean isUnFocusable = getFocusable(), newFocus = true;
        /* loop through all panelManagers */
        for(PanelManager p : getPanels()) {
            /* if the panel draws boundaries, check focus */
            if (p.getWindow().drawBound()) {
                /* get if we are hovering over the panel */
                bounds old = p.getBound();
                boolean isInBound = Mouse.IsInArea(new bounds(old.x - BOUNDS_SIZE, old.y - UPPER_HEIGHT,
                                old.w + (BOUNDS_SIZE * 2), old.h + BOUNDS_SIZE + UPPER_HEIGHT).toCoordinates());
                /* check if we are able to focus onto this panel */
                if (isInBound && !isUnFocusable && newFocus) {
                    /* set unable to focus any other windows */
                    newFocus = false;
                    /* if it was not focused yet, repaint */
                    if(!p.isFocus()){
                        /* set focus and put this to highest priority */
                        p.setFocus(true);
                        movePanel(p.getWindow(), 0);
                        /* repaint to fix the blackness bug */
                        resetDisplay(p.getWindow());
                    }

                /* check if we can unfocus this panel */
                } else if (p.getWindow().canUnFocus()) {
                    /* if was focused, repaint */
                    if(p.isFocus()){
                        /* removed focus */
                        p.setFocus(false);
                        resetDisplay(p.getWindow());
                    }
                }
            }

            /* run logic */
            p.logic();
        }

        /* if F1 is pressed, reset panel positions */
        if(Keys.isPressed(KeyUtil.F1)){
            /* reset positions */
            for(PanelManager p : getPanels()){
                p.getBound().x = Integer.MAX_VALUE;
                p.getBound().y = Integer.MAX_VALUE;
            }

            /* fetch optimal positions */
            for(PanelManager p : getPanels()){
                setStartPos(p.getWindow());
            }

            /*  finally, update the screen to remove any garbled graphics */
            SP.repaintLater();
        }
    }

    /* check if can be unfocused */
    public boolean getFocusable() {
        for(PanelManager p : getPanels()){
            /* check if can be unfocused and if does draw boundaries */
            if(!p.getWindow().canUnFocus() && p.getWindow().drawBound()){
                return true;
            }
        }

        return false;
    }

    /* add a new window */
    public void addWindow(Window w, String name, int buttons){
        /* add new panelManager */
        win.add(0, new PanelManager(w, name, buttons));
        /* call create routine */
        win.get(0).getWindow().create();
        /* find new position for the window */
        setStartPos(w);
    }

    /* remove the window */
    public void rmvWindow(Window w){
        /* get the panelManager containing window */
        PanelManager p = getPanelManager(w);

        /* remove the window from the list and call remove function in the panelManager */
        win.remove(p);
        p.kill();
    }

    /* get panelManager containing with the Window */
    public PanelManager getPanelManager(Window w) {
        for(PanelManager p : getPanels()){
            /* if the paneManager's window is same as window we are trying to find */
            if(p.getWindow() == w){
                /* return the windowManager */
                return p;
            }
        }

        return null;
    }

    /* repaint every window */
    public void repaintAll() {
        for(PanelManager p : getPanels()){
            /* repaint this window */
            if(p != null) {
                p.repaint();
            }
        }
    }

    /* reset all displays */
    private void resetDisplayAll() {
        for(PanelManager p : getPanels()){
            p.resetDisplay();
        }
    }

    /* reset display on window */
    private void resetDisplay(Window w) {
        getPanelManager(w).resetDisplay();
    }

    /* remove all the windows */
    public void destroy() {
        for(PanelManager p : getPanels()){
            /* remove window */
            rmvWindow(p.getWindow());
        }
    }

    /* shift panel to position */
    public void movePanel(Window w, int index){
        /* if this panel exists in the list */
        if(win.contains(getPanelManager(w))){
            /* get the panelManager */
            PanelManager m = win.get(win.indexOf(getPanelManager(w)));
            /* remove it and add to right index */
            win.remove(m);
            win.add(index, m);

            /* set to index on main jPanel too */
            App.getJPanel().remove(m);
            App.getJPanel().add(m, index);
        }
    }

    /* get all panelManagers */
    public PanelManager[] getPanels(){
        return win.toArray(new PanelManager[win.size()]);
    }

    /* returns default boundaries to set the windows at end of the screen */
    public static bounds defaultBounds() {
        return new bounds(Integer.MAX_VALUE, Integer.MAX_VALUE, 0, 0);
    }

    /* script to place objects to optimal position in the left lower corner of the screen */
    public void setStartPos(Window w) {
        /* if this window does not draw boundaries, ignore */
        if(w.drawBound()) {
            /* set to default size */
            getPanelManager(w).defaultSize();
            /* set the window to left lower corner of the screen */
            w.getBounds().x = App.GetBounds().w - w.getBounds().w;
            w.getBounds().y = App.GetBounds().h - w.getBounds().h + UPPER_HEIGHT - BOUNDS_SIZE;

            /* calculate the best position for the window */
            bounds b = getCloseProximityBounds(getPanelManager(w), App.GetBounds().w + BOUNDS_SIZE, App.GetBounds().h + UPPER_HEIGHT);

            /* apply the new position to the window */
            w.getBounds().x = b.x;
            w.getBounds().y = b.y;

            /* reset the display of this window */
            resetDisplay(w);
        }
    }

    /* calculates the most optimal position for window */
    private bounds getCloseProximityBounds(PanelManager pan, int targetX, int targetY) {
        /* create new boundaries to take account the window position */
        bounds bound = pan.getWindowBounds();

        /* if we have not yet attempted to calculate 3 times and the boundaries still clip, loop */
        if(doesClip(bound, pan)){
            /* calculate right position */
            getNextProximity(bound, targetX, targetY, new bounds(0, 0, targetX, targetY), pan);
        }

        /* return the new boundaries */
        return bound;
    }

    /* function to calculate good position for the window */
    private int getNextProximity(bounds b, int targetX, int targetY, bounds area, PanelManager pan) {
        /* setup master X and Y positions, last proximity counter and max value for checking positions */
        int masterX = b.x, masterY = b.y, lastProximity = Integer.MAX_VALUE, maxInt = win.size();

        /* two for loops to check horizontal and vertical positions */
        for(int y_ = 0;y_ < maxInt;y_ ++) {
            for (int x_ = 0; x_ < maxInt; x_++) {
                /* set positions to calculate with, and times counter to make sure we don't get stuck */
                int x = b.x, y = b.y, times = 0;

                /* if we have attempted less than 10 times, and new proximity is less than old one, then loop */
                while (times < 10 && calculateProximity(x + (b.w / 2), y + (b.h / 2), targetX, targetY) <= lastProximity) {
                    /* if this is a valid panelManager at index _x, and draws its boundaries */
                    if (x_ < win.size() && win.get(x_).getWindow() != pan.getWindow() && win.get(x_).getWindow().drawBound()) {
                        /* place right from the panelManager */
                        x = win.get(x_).getBound().x - b.w;

                    } else {
                        /* place next to left well */
                        x = targetX - b.w;
                    }

                    /* if this is a valid panelManager at index _y, and draws its boundaries */
                    if (y_ < win.size() && win.get(y_).getWindow() != pan.getWindow() && win.get(y_).getWindow().drawBound()) {
                        /* place above the panelManager */
                        y = win.get(y_).getBound().y - b.h;

                    } else {
                        /* place above the bottom well */
                        y = targetY - b.h;
                    }

                    /* increase attempt counter */
                    times ++;
                }

                /* calculate new proximity */
                int prox = calculateProximity(x + (b.w / 2), y + (b.h / 2), targetX, targetY);
                /* if the proximity we had calculated earlier was greater than this one, and the object does not clip any objects
                 * and also is in the screen, accept this position */
                if (lastProximity > prox && !doesClip(new bounds(x, y, b.w, b.h), pan) &&
                        area.toRectangle().contains(new bounds(x, y, b.w, b.h).toRectangle())) {
                    /* save position and proximity */
                    lastProximity = prox;
                    masterX = x;
                    masterY = y;
                }
            }
        }

        /* save the master position */
        b.x = masterX;
        b.y = masterY;
        /* return new proximity */
        return calculateProximity(masterX + (b.w / 2), masterY + (b.h / 2), targetX, targetY);
    }

    /* this function is used to calculate the proximity for tests later on */
    private int calculateProximity(int x, int y, int targetX, int targetY) {
        return ((targetX - x) * 10) + ((targetY - y) * 10);
    }

    /* check through all bounds objects to check if they clip */
    private boolean doesClip(bounds b, PanelManager p) {
        for(PanelManager pa : getPanels()){
            /* check if this window is the same we are checking clipping from and is drawing boundaries */
            if(pa != p && pa.getWindow().drawBound()){
                /* check if it actually does clip and if so, return true */
                if(clips(b, pa.getBound())){
                    return true;
                }
            }
        }

        /* if no clipping objects found, return false */
        return false;
    }

    /* check if two bounds objects clips in any location */
    private boolean clips(bounds b1, bounds b2) {
        return b1.toRectangle().intersects(b2.toRectangle()) || b2.toRectangle().intersects(b1.toRectangle()) ||
                b1.toRectangle().contains(b2.toRectangle()) || b2.toRectangle().contains(b1.toRectangle());
    }

    /* used to check if any window is outside the screen after resize */
    public void resize(int width, int height) {
        /* do for all panelManagers */
        for(PanelManager p : getPanels()){
            forceOnScreen(p.getWindow());
        }
    }

    /* method to force window inside the screen */
    public void forceOnScreen(Window w) {
        /* get window bounds */
        PanelManager p = getPanelManager(w);
        bounds b = p.getWindowBounds();
            /* calculate offsets from screen */
        int x = b.x + b.w - App.GetBounds().w, y = b.y + b.h - App.GetBounds().h;

            /* if is outside the screen (left), reset position */
        if(x > 0){
            p.getBound().x -= x;
        }

            /* if is outside the screen (below), reset position */
        if(y > 0){
            p.getBound().y -= y;
        }

            /* get the window bounds again */
        b = p.getWindowBounds();

            /* if is outside the screen (right), reset position */
        if(b.x < 0){
            p.getBound().x = BOUNDS_SIZE;
        }

            /* if is outside the screen (above), reset position */
        if(b.y < 0){
            p.getBound().y = UPPER_HEIGHT;
        }
    }
}