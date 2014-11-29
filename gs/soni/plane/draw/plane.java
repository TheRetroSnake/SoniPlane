package gs.soni.plane.draw;

import gs.app.lib.gfx.Graphics;
import gs.app.lib.gfx.Sprite;
import gs.app.lib.math.bounds;
import gs.app.lib.util.KeyUtil;
import gs.app.lib.util.MouseUtil;
import gs.soni.plane.SP;
import gs.soni.plane.project.map;
import gs.soni.plane.project.mappings;
import gs.soni.plane.project.tileLoader;
import gs.soni.plane.util.Event;
import gs.soni.plane.util.Keys;
import gs.soni.plane.util.Logicable;
import gs.soni.plane.util.Mouse;
import gs.soni.plane.v;

import java.awt.*;

public class plane implements Drawable, Logicable {

    private int last;

    public plane(){
        SP.addToRenderList(this);
    }

    @Override
    public void draw(Graphics g) {
        int w = tileLoader.GetWidth(), h = tileLoader.GetHeight(), mul = v.GetSizeMultiplier();
        Sprite s = new Sprite();
        s.setColor(Color.RED);

        for(int y = 0, o = 0;y < v.mapSize.y;y ++){
            for(int x = 0;x < v.mapSize.x;x ++, o ++) {
                if (o >= mappings.GetMapArray().length) {
                    break;
                }

                map n = mappings.GetMapArray()[o];
                Sprite spr;
                if (n.tileOff < tileLoader.GetTextureAmount((n.palLine - v.LineOff) % gs.soni.plane.project.palette.getPalette().length)
                        && n.tileOff >= 0 && ((v.DrawHighPlane && n.HighPlane) || (v.DrawLowPlane && !n.HighPlane))) {
                    spr = new Sprite(tileLoader.GetTexture((n.palLine - v.LineOff) %
                            gs.soni.plane.project.palette.getPalette().length, n.tileOff));

                } else {
                    spr = new Sprite(tileLoader.GetTrans());
                }

                bounds t = new bounds((v.PlaneBounds.x + (x * w * mul)), (v.PlaneBounds.y + (y * h * mul)),
                        v.PlaneBounds.x + ((x + 1) * w * mul), v.PlaneBounds.y + ((y + 1) * h * mul));

                spr.setBounds(t.x, t.y, w * mul, h * mul);
                spr.setFlip(n.XFlip, n.YFlip);
                spr.setAlpha(1f);
                g.drawImage(spr);

                if (Mouse.IsInArea(t)) {
                    v.DrawBounds(g, s, new bounds(t.x, t.y, t.w - t.x, t.h - t.y), 0, 2 * mul);

                } else if (n.tileOff == v.TileSelected) {
                    s.setColor(Color.GREEN);
                    v.DrawBounds(g, s, new bounds(t.x, t.y, t.w - t.x, t.h - t.y), 0, 2 * mul);
                    s.setColor(Color.RED);
                }
            }
        }

        v.DrawBounds(g, s, v.PlaneBounds, 2, 2);
        if(v.SelBounds != null) {
            v.DrawBounds(g, s, new bounds(v.PlaneBounds.x + (v.SelBounds.x * mul),v.PlaneBounds.y + (v.SelBounds.y * mul),
                    (v.SelBounds.w) * mul, (v.SelBounds.h * mul)), 0, 2 * mul);
        }
    }

    @Override
    public void logic() {
        int w = tileLoader.GetWidth(), h = tileLoader.GetHeight(), mul = v.GetSizeMultiplier();
        for(int y = 0, o = 0;y < v.mapSize.y;y ++) {
            for (int x = 0; x < v.mapSize.x; x++, o++) {
                if (o >= mappings.GetMapArray().length) {
                    break;
                }

                map n = mappings.GetMapArray()[o];
                bounds t = new bounds((v.PlaneBounds.x + (x * w * mul)), (v.PlaneBounds.y + (y * h * mul)),
                        v.PlaneBounds.x + ((x + 1) * w * mul), v.PlaneBounds.y + ((y + 1) * h * mul));
                if (Mouse.IsInArea(t)) {

                    if (Mouse.IsClicked(MouseUtil.MIDDLE) || Keys.isPressed(KeyUtil.P)) {
                        v.TileSelected = n.tileOff;
                        v.MapSelected = o;
                        v.SelBounds = new bounds((t.x - v.PlaneBounds.x) / mul, (t.y - v.PlaneBounds.y) / mul, t.w - t.x, t.h - t.y);
                        SP.repaintLater();

                    } else if (Mouse.IsHeld(MouseUtil.LEFT)) {
                        n.tileOff = v.TileSelected;
                        n.palLine = v.PalLine;
                        mappings.SetMap(n, o);

                        v.MapSelected = o;
                        v.SelBounds = new bounds((t.x - v.PlaneBounds.x) / mul, (t.y - v.PlaneBounds.y) / mul, t.w - t.x, t.h - t.y);
                        SP.repaintLater();

                    } else if (Mouse.IsHeld(MouseUtil.RIGHT)) {
                        if (v.SelStart == null) {
                            v.SelStart = new bounds(t.x, t.y, 0, 0);
                        }

                        v.SelEnd = new bounds(t.w, t.h, 0, 0);
                        v.SelBounds = new bounds((v.SelStart.x - v.PlaneBounds.x) / mul, (v.SelStart.y - v.PlaneBounds.y) / mul,
                                (v.SelEnd.x - v.SelStart.x) / mul, (v.SelEnd.y - v.SelStart.y) / mul);

                        if (-v.SelBounds.w > -1) {
                            v.SelBounds.x = (v.SelEnd.x - v.PlaneBounds.x) / mul - w;
                            v.SelBounds.w = ((v.SelStart.x - v.PlaneBounds.x) / mul + w) - v.SelBounds.x;
                        }

                        if (-v.SelBounds.h > -1) {
                            v.SelBounds.y = (v.SelEnd.y - v.PlaneBounds.y) / mul - h;
                            v.SelBounds.h = ((v.SelStart.y - v.PlaneBounds.y) / mul + h) - v.SelBounds.y;
                        }

                        if(!Event.SelMenu) {
                            Event.projectMenu();
                        }
                        SP.repaintLater();

                    } else {
                        v.SelStart = null;
                        v.SelEnd = null;
                    }

                    if(last != o) {
                        last = o;
                        SP.repaintLater();
                    }
                }
            }
        }
    }

    @Override
    public int renderPriority() {
        return v.RENDERPR_MIN;
    }
}
