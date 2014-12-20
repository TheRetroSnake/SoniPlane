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

    public void set(boolean delete) {
        int w = tileLoader.GetWidth(), h = tileLoader.GetHeight();
        mappings.ShiftMap((end.x / w) - (origin.x / w), (end.y / h) - (origin.y / h), origin);
        if(delete){
            mappings.Delete(origin, end);
        }

        v.SelBounds = new bounds(end);
    }

    public void logic(bounds off, boolean mouse) {
        if(mouse && Mouse.IsInArea(new bounds(off.x, off.y, off.x + off.w, off.y + off.h))){
            int x = ((Mouse.GetPos().x - mx) / tileLoader.GetWidth()) * tileLoader.GetWidth(),
                    y = ((Mouse.GetPos().y - my) / tileLoader.GetHeight()) * tileLoader.GetHeight();

            end = new bounds(origin.x + x, origin.y + y, origin.w, origin.h);
            v.BlockControls = true;
            v.UnlockEndFrame = true;
        }
    }

    public void draw(Graphics g, bounds off, int mul) {
        Sprite s = new Sprite();
        s.setColor(Color.RED);
        v.DrawBounds(g, s, new bounds(off.x + (origin.x * mul), off.y + (origin.y * mul), origin.w * mul, origin.h * mul), 0, 2 * mul);
        s.setColor(Color.MAGENTA);
        v.DrawBounds(g, s, new bounds(off.x + (end.x * mul), off.y + (end.y * mul), end.w * mul, end.h * mul), 0, 2 * mul);
    }
}
