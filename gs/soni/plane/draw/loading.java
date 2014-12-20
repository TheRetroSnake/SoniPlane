package gs.soni.plane.draw;

import gs.app.lib.application.App;
import gs.app.lib.gfx.Graphics;
import gs.app.lib.gfx.Sprite;
import gs.app.lib.gfx.gfx;
import gs.app.lib.math.bounds;
import gs.soni.plane.SP;
import gs.soni.plane.util.Logicable;
import gs.soni.plane.util.Style;
import gs.soni.plane.util.StyleItem;
import gs.soni.plane.v;

import java.awt.*;

public class loading implements Window, PanelListener {
    private int e;
    private Sprite s;
    private int counter;
    private StyleItem style;
    private float alpha;

    public loading(int event){
        e = event;
    }

    @Override
    public bounds getBounds() {
        return new bounds(0, 1, App.GetBounds().w, App.GetBounds().h);
    }

    @Override
    public void draw(Graphics g) {
        if (s != null) {
            g.drawImage(s);

            g.setAlpha(alpha);
            Graphics.setFont(style.GetFont());
            g.setColor(style.GetColor());
            g.drawText("Loading", (int) ((App.GetBounds().w / 2) - (Graphics.GetTextWidth("Loading") / 2)), (App.GetBounds().h / 2) + 32);
            g.setAlpha(1f);
        }
    }

    @Override
    public void logic() {
        if (s == null) {
            s = new Sprite(gfx.getImage(v.LaunchAdr +"/res/load.png"));
            s.setSize(60, 60);
            s.setAlpha(1f);
            counter = 0;
            style = Style.GetStyle("menu_center");
            alpha = 1f;
        }

        s.setPosition((App.GetBounds().w / 2) - (s.getBounds().w / 2), (App.GetBounds().h / 2) - (s.getBounds().h / 2));
        counter ++;
        if (counter > 1) {
            s.rotate(45f);
            counter = 0;
        }

        if (v.mode == e) {
            alpha -= 0.04f;
            s.setAlpha(alpha);

            if (alpha < 0f) {
                SP.getWM().rmvWindow(this);
                SP.SetNormalTitle();
                repaintAll();
                SP.repaint();
                return;
            }
        }

        repaint();
        SP.repaint();
    }

    @Override
    public void create() {
        SP.getWM().getPanelManager(this).addListener(this);
        SP.getWM().getPanelManager(this).setBackground(new Color(0, 0, 0, 0));
    }

    @Override
    public boolean canUnFocus() {
        return false;
    }

    @Override
    public boolean drawBound() {
        return false;
    }

    private void repaint() {
        SP.getWM().getPanelManager(this).repaint();
    }

    private void repaintAll() {
        SP.getWM().repaintAll();
    }

    @Override
    public void close() {

    }

    @Override
    public void draw() {

    }

    @Override
    public void logic(boolean focus) {
        logic();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void move(int x, int y) {

    }
}
