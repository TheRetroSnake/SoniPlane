package gs.soni.plane.draw;

import gs.app.lib.gfx.Graphics;
import gs.app.lib.gfx.Sprite;
import gs.app.lib.math.bounds;
import gs.app.lib.util.MouseUtil;
import gs.soni.plane.SP;
import gs.soni.plane.util.Mouse;
import gs.soni.plane.v;

import java.awt.*;

public class palette implements Window {
    private bounds last = new bounds();
    private bounds bound;

    public palette(){
        bound = v.PalBounds;
    }

    @Override
    public bounds getBounds() {
        return bound;
    }

    @Override
    public void draw(Graphics g) {
        bound = v.PalBounds;
        if (bound.y + bound.h > 0) {
            Sprite s = new Sprite();
            int height = bound.h / gs.soni.plane.project.palette.getPalette().length;

            for (int l = 0; l < gs.soni.plane.project.palette.getPalette().length; l++) {
                int len = gs.soni.plane.project.palette.getPalette()[l].length, width = bound.w / len;

                for (int o = 0; o < len; o++) {
                    bounds t = new bounds((o * width), (l * height), width, height);

                    s.setColor(gs.soni.plane.project.palette.getPalette(l, o));
                    s.setBounds(t);
                    g.fillRect(s);

                    if (v.PalLine == l) {
                        s.setColor(Color.GREEN);
                        v.DrawBounds(g, s, t, 0, 2, new bounds(0, 1, 0, 1));

                        if (v.PalSelcted == o) {
                            s.setColor(Color.MAGENTA);
                            v.DrawBounds(g, s, t, 0, 2);
                        }
                    }

                    if (Mouse.IsInArea(new bounds(bound.x + t.x, bound.y + t.y, bound.x + t.x + t.w, bound.y + t.y + t.h))) {
                        s.setColor(Color.RED);
                        v.DrawBounds(g, s, t, 0, 2);
                    }
                }
            }
        }
    }

    @Override
    public void logic() {
        float height = bound.h / gs.soni.plane.project.palette.getPalette().length;
        for (int l = 0; l < gs.soni.plane.project.palette.getPalette().length; l++) {
            float len = gs.soni.plane.project.palette.getPalette()[l].length, width = bound.w / len;

            for (int o = 0; o < len; o++) {
                bounds t = new bounds((int) (bound.x + ((float) o * width)), (int) (bound.y + ((float) l * height)),
                        (int) width, (int) height);

                if (Mouse.IsInArea(new bounds(t.x, t.y, t.x + t.w, t.y + t.h))) {
                    if (Mouse.IsHeld(MouseUtil.LEFT) || Mouse.IsHeld(MouseUtil.RIGHT)) {
                        v.PalSelcted = o;
                        v.PalLine = l;
                        repaintAll();
                    }

                    if (!new bounds(o, l, 0, 0).compare(last)) {
                        last = new bounds(o, l, 0, 0);
                        repaint();
                    }
                }
            }
        }
    }

    @Override
    public void create() {

    }

    @Override
    public boolean canUnFocus() {
        return !(Mouse.IsHeld(MouseUtil.RIGHT) || Mouse.IsHeld(MouseUtil.MIDDLE) || Mouse.IsHeld(MouseUtil.LEFT));
    }

    private void repaint() {
        SP.getWM().getPanelManager(this).repaint();
    }

    private void repaintAll() {
        SP.getWM().repaintAll();
    }

    @Override
    public boolean drawBound() {
        return true;
    }
}
