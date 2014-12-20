package gs.soni.plane.draw;

import gs.app.lib.application.App;
import gs.app.lib.gfx.Graphics;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class PanelManager extends JPanel {
    ArrayList<PanelListener> panL;
    private Window win;
    private boolean wasBound;
    private boolean focus;

    public PanelManager(Window w) {
        panL = new ArrayList<PanelListener>();
        App.getJPanel().add(this);

        setVisible(true);
        setBorder(null);
        setFocusable(true);
        setBounds(w.getBounds().x, w.getBounds().y, w.getBounds().w, w.getBounds().h);

        win = w;
    }

    public void kill() {
        for(PanelListener p : panL){
            p.close();
        }

        App.getJPanel().remove(this);
    }

    public Window getWindow() {
        return win;
    }

    public boolean wasBound(){
        return wasBound;
    }

    public void setWasBound(boolean b){
        wasBound = b;
    }

    @Override
    public void paintComponent(java.awt.Graphics g) {
        if(win != null) {
            super.paintComponent(g);
            setBounds(win.getBounds().x, win.getBounds().y, win.getBounds().w, win.getBounds().h);

            for (PanelListener p : panL) {
                p.draw();
            }

            win.draw(new Graphics((Graphics2D) g));
        }
    }

    public void logic() {
        for (PanelListener p : panL) {
            p.logic(isFocus());
        }

        if(isFocus()) {
            win.logic();
        }
    }

    public boolean isFocus() {
        return focus;
    }

    public void setFocus(boolean focus) {
        this.focus = focus;
    }

    public void addListener(PanelListener p){
        panL.add(0, p);
    }
}