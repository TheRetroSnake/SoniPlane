package gs.soni.plane.draw;

import gs.app.lib.gfx.Graphics;
import gs.app.lib.gfx.Sprite;
import gs.app.lib.math.bounds;
import gs.soni.plane.project.mappings;
import gs.soni.plane.project.tileLoader;
import gs.soni.plane.util.Mouse;
import gs.soni.plane.v;

import java.awt.*;

public class PlaneDrag {
    private final bounds origin;
    private bounds end;
    private int mx;
    private int my;

    public PlaneDrag(bounds b) {
        origin = b;
        end = b;
        mx = Mouse.GetPos().x;
        my = Mouse.GetPos().y;
    }

    public void set(boolean delete, plane p) {
        int w = tileLoader.GetWidth(), h = tileLoader.GetHeight();
        mappings.ShiftMap((end.x / w) - (origin.x / w), (end.y / h) - (origin.y / h), origin);
        if(delete){
            mappings.Delete(origin, end);
        }

        p.selBounds = new bounds(end);
    }

    public void logic(bounds off, boolean mouse, float mul) {
        if(mouse && Mouse.IsInArea(new bounds(off.x, off.y, off.x + off.w, off.y + off.h))){

            int x = ((int)((Mouse.GetPos().x - mx) / mul) / tileLoader.GetWidth()) * tileLoader.GetWidth(),
                    y = ((int)((Mouse.GetPos().y - my) / mul) / tileLoader.GetHeight()) * tileLoader.GetHeight();

            end = new bounds(origin.x + x, origin.y + y, origin.w, origin.h);
            v.BlockControls = true;
            v.UnlockEndFrame = true;
        }
    }

    public void draw(Graphics g, bounds off, float mul) {
        Sprite s = new Sprite();
        s.setColor(Color.RED);
        v.DrawBounds(g, s, new bounds(off.x + (int)(mul * origin.x), off.y + (int)(mul * origin.y),
                (int)(mul * origin.w), (int)(mul * origin.h)), 0, 2);
        s.setColor(Color.MAGENTA);
        v.DrawBounds(g, s, new bounds(off.x + (int)(mul * end.x), off.y + (int)(mul * end.y),
                (int)(mul * end.w), (int)(mul * end.h)), 0, 2);
    }
}
