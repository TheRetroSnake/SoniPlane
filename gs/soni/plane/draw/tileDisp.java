package gs.soni.plane.draw;

import gs.app.lib.application.App;
import gs.app.lib.gfx.Graphics;
import gs.app.lib.gfx.Sprite;
import gs.app.lib.math.bounds;
import gs.app.lib.util.MouseUtil;
import gs.soni.plane.SP;
import gs.soni.plane.project.tileLoader;
import gs.soni.plane.util.Logicable;
import gs.soni.plane.util.Mouse;
import gs.soni.plane.v;

import java.awt.*;

public class tileDisp implements Drawable, Logicable {
    public static int drawn;
    private int last = 0;
    private boolean lastInTile;

    public tileDisp(){
        SP.addToRenderList(this);
    }

    @Override
    public void draw(Graphics g) {
        int w = tileLoader.GetWidth(), h = tileLoader.GetHeight();
        Sprite s = new Sprite();
        s.setColor(Color.RED);
        drawn = 0;

        int w_ = v.TileBounds.w / (w + 2);
        if (w_ > 0) {
            for (int o = 0; o < tileLoader.GetTextureAmount(v.PalLine); o++) {
                int x = o % w_, y = o / w_;
                bounds t = new bounds(v.TileBounds.x + (x * (w + 2)), v.TileBounds.y + (y * (h + 2)),
                        v.TileBounds.x + ((x + 1) * (w + 2)) - 2, v.TileBounds.y + ((y + 1) * (h + 2)) - 2);

                if (t.y < App.GetBounds().h) {
                    g.drawImage(tileLoader.GetTexture(v.PalLine, o), t.x, t.y, w, h);
                    drawn ++;

                    if (Mouse.IsInArea(t)) {
                        v.DrawBounds(g, s, new bounds(t.x, t.y, t.w - t.x, t.h - t.y), 2, 2);

                    } else if(o == v.TileSelected){
                        s.setColor(Color.MAGENTA);
                        v.DrawBounds(g, s, new bounds(t.x, t.y, t.w - t.x, t.h - t.y), 2, 2);
                        s.setColor(Color.RED);
                    }
                }
            }

            v.DrawBounds(g, s, v.TileBounds, 2, 2);
        }
    }

    @Override
    public void logic() {
        boolean inTile = false;

        int w = tileLoader.GetWidth(), h = tileLoader.GetHeight();
        int w_ = v.TileBounds.w / (w + 2);
        if (w_ > 0) {
            for (int o = 0; o < tileLoader.GetTextureAmount(v.PalLine); o++) {
                int x = o % w_, y = o / w_;
                bounds t = new bounds(v.TileBounds.x + (x * (w + 2)), v.TileBounds.y + (y * (h + 2)),
                        v.TileBounds.x + ((x + 1) * (w + 2)) - 2, v.TileBounds.y + ((y + 1) * (h + 2)) - 2);

                if (Mouse.IsInArea(t)) {
                    if (Mouse.IsClicked(MouseUtil.LEFT)) {
                        v.TileSelected = o;
                        SP.repaintLater();
                    }

                    if(last != o){
                        last = o;
                        SP.repaintLater();
                    }

                    inTile = true;
                    lastInTile = true;
                }
            }
        }

        if(lastInTile && !inTile){
            lastInTile = false;
            SP.repaintLater();
        }
    }

    @Override
    public int renderPriority() {
        return v.RENDERPR_MIN;
    }
}
