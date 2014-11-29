package gs.soni.plane.draw;

import gs.app.lib.gfx.Graphics;
import gs.app.lib.gfx.Sprite;
import gs.app.lib.math.bounds;
import gs.app.lib.util.MouseUtil;
import gs.soni.plane.SP;
import gs.soni.plane.util.Logicable;
import gs.soni.plane.util.Mouse;
import gs.soni.plane.v;

import java.awt.*;

public class palette implements Drawable, Logicable {

    private boolean Area;
    private bounds last = new bounds();

    public palette(){
        SP.addToRenderList(this);
    }

    @Override
    public void draw(Graphics g) {
        if (v.PalBounds.y + v.PalBounds.h > 0) {
            Sprite s = new Sprite();
            int height = v.PalBounds.h / gs.soni.plane.project.palette.getPalette().length;

            for (int l = 0; l < gs.soni.plane.project.palette.getPalette().length; l++) {
                int len = gs.soni.plane.project.palette.getPalette()[l].length, width = v.PalBounds.w / len;

                for (int o = 0; o < len; o++) {
                    bounds t = new bounds(v.PalBounds.x + (o * width), v.PalBounds.y + (l * height), width, height);

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

                    if (Mouse.IsInArea(new bounds(t.x, t.y, t.x + t.w, t.y + t.h))) {
                        s.setColor(Color.RED);
                        v.DrawBounds(g, s, t, 0, 2);
                        Area = true;
                    }
                }
            }

            s.setColor(Color.RED);
            v.DrawBounds(g, s, v.PalBounds, 2, 2);
        }
    }

    @Override
    public void logic() {
        if (Mouse.IsInArea(new bounds(v.PalBounds.x, v.PalBounds.y, v.PalBounds.x + v.PalBounds.w, v.PalBounds.y + v.PalBounds.h))) {

            float height = v.PalBounds.h / gs.soni.plane.project.palette.getPalette().length;
            for (int l = 0; l < gs.soni.plane.project.palette.getPalette().length; l++) {
                float len = gs.soni.plane.project.palette.getPalette()[l].length, width = v.PalBounds.w / len;

                for (int o = 0; o < len; o++) {
                    bounds t = new bounds((int)(v.PalBounds.x + ((float)o * width)), (int)(v.PalBounds.y + ((float)l * height)),
                            (int)width, (int)height);

                    if (Mouse.IsInArea(new bounds(t.x, t.y, t.x + t.w, t.y + t.h))) {
                        if (Mouse.IsHeld(MouseUtil.LEFT) || Mouse.IsHeld(MouseUtil.RIGHT)) {
                            v.PalSelcted = o;
                            v.PalLine = l;
                            SP.repaintLater();
                        }

                        if(!new bounds(o, l, 0, 0).compare(last)){
                            last = new bounds(o, l, 0, 0);
                            SP.repaintLater();
                            Area = true;
                        }
                    }
                }
            }
        } else if(Area){
            SP.repaintLater();
            Area = false;
        }
    }

    @Override
    public int renderPriority() {
        return v.RENDERPR_MIN;
    }
}
