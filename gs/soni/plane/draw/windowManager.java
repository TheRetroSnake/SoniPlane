package gs.soni.plane.draw;

import gs.app.lib.gfx.Graphics;
import gs.app.lib.gfx.Sprite;
import gs.app.lib.math.bounds;
import gs.soni.plane.SP;
import gs.soni.plane.util.Logicable;
import gs.soni.plane.util.Mouse;
import gs.soni.plane.v;

import java.awt.*;
import java.util.ArrayList;

public class windowManager implements Drawable, Logicable {
    ArrayList<PanelManager> win;

    public windowManager(){
        SP.addToRenderList(this);
        win = new ArrayList<PanelManager>();
    }

    @Override
    public void draw(Graphics g) {
        Sprite s = new Sprite();
        for(PanelManager p : win.toArray(new PanelManager[win.size()])) {
            if (p.getWindow().drawBound()) {
                if (p.isFocus()) {
                    s.setColor(Color.GREEN);

                } else {
                    s.setColor(Color.RED);
                }

                v.DrawBounds(g, s, p.getWindow().getBounds(), 2, 2);
            }
        }
    }

    private static bounds getAreaBounds(bounds b) {
        return new bounds(b.x, b.y, b.x + b.w, b.y + b.h);
    }

    @Override
    public int renderPriority() {
        return v.RENDERPR_MIN;
    }

    @Override
    public void logic() {
        boolean repaint = false, isUnFocusable = getFocusable();
        for(PanelManager p : win.toArray(new PanelManager[win.size()])) {
            if (p.getWindow().drawBound()) {
                boolean isInBound = Mouse.IsInArea(getAreaBounds(p.getWindow().getBounds()));

                if (p.wasBound() != isInBound) {
                    repaint = true;
                }

                p.setWasBound(isInBound);
                if (isInBound && !isUnFocusable) {
                    if(!p.isFocus()){
                        repaint = true;
                    }
                    p.setFocus(true);
                    movePanel(p.getWindow(), 0);

                } else if (p.getWindow().canUnFocus()) {
                    if(p.isFocus()){
                        repaint = true;
                    }

                    p.setFocus(false);
                }
            }

            p.logic();

        }

        if(repaint){
            SP.repaint();
        }
    }

    public boolean getFocusable() {
        for(PanelManager p : win.toArray(new PanelManager[win.size()])){
            if(!p.getWindow().canUnFocus() && p.getWindow().drawBound()){
                return true;
            }
        }

        return false;
    }

    public void addWindow(Window w){
        win.add(0, new PanelManager(w));
        win.get(0).getWindow().create();
    }

    public void rmvWindow(Window w){
        PanelManager p = getPanelManager(w);

        win.remove(p);
        p.kill();
    }

    public PanelManager getPanelManager(Window w) {
        for(PanelManager p : win.toArray(new PanelManager[win.size()])){
            if(p.getWindow() == w){
                return p;
            }
        }

        return null;
    }

    public void repaintAll() {
        for(PanelManager p : win.toArray(new PanelManager[win.size()])){
            p.repaint();
        }
    }

    public void destroy() {
        for(PanelManager p : win.toArray(new PanelManager[win.size()])){
            rmvWindow(p.getWindow());
        }
    }

    public void movePanel(Window w, int index){
        if(win.contains(getPanelManager(w))){
            PanelManager m = win.get(win.indexOf(getPanelManager(w)));
            win.remove(win.indexOf(m));
            win.add(index, m);
        }
    }
}