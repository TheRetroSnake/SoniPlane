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
import gs.soni.plane.util.Mouse;
import gs.soni.plane.v;

import java.awt.*;

public class plane implements Window {
    private bounds bound;
    private int last;
    private PlaneDrag dr;
    private boolean lastHeld = false;

    public plane(){
        bound = v.PlaneBounds;
    }

    @Override
    public bounds getBounds() {
        return bound;
    }

    @Override
    public void draw(Graphics g) {
        bound = v.PlaneBounds;
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

                bounds t = new bounds(x * w * mul, y * h * mul, (x + 1) * w * mul, (y + 1) * h * mul);

                spr.setBounds(t.x, t.y, w * mul, h * mul);
                spr.setFlip(n.XFlip, n.YFlip);
                spr.setAlpha(1f);
                g.drawImage(spr);

                if (Mouse.IsInArea(new bounds(t.x + bound.x, t.y + bound.y, t.w + bound.x, t.h + bound.y))) {
                    v.DrawBounds(g, s, new bounds(t.x, t.y, t.w - t.x, t.h - t.y), 0, 2 * mul);

                } else if (isSelected(n.tileOff)) {
                    s.setColor(Color.GREEN);
                    v.DrawBounds(g, s, new bounds(t.x, t.y, t.w - t.x, t.h - t.y), 0, 2 * mul);
                    s.setColor(Color.RED);
                }
            }
        }

        if(dr != null) {
            dr.draw(g, new bounds(0, 0, bound.w, bound.h), mul);

        } else if(v.SelBounds != null) {
            v.DrawBounds(g, s, new bounds((v.SelBounds.x * mul), (v.SelBounds.y * mul),
                    (v.SelBounds.w) * mul, (v.SelBounds.h * mul)), 0, 2 * mul);
        }
    }

    private boolean isSelected(int off) {
        if(v.TileSelected - v.TileSelectedEnd >= 0){
            return off <= v.TileSelected && off >= v.TileSelectedEnd;

        } else {
            return off >= v.TileSelected && off <= v.TileSelectedEnd;
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
                bounds t = new bounds((bound.x + (x * w * mul)), (bound.y + (y * h * mul)),
                        bound.x + ((x + 1) * w * mul), bound.y + ((y + 1) * h * mul));
                if (Mouse.IsInArea(t)) {

                    if (Mouse.IsClicked(MouseUtil.MIDDLE) || Keys.isPressed(KeyUtil.P)) {
                        v.TileSelected = n.tileOff;
                        v.TileSelectedEnd = n.tileOff;
                        v.MapSelected = o;

                        v.SelBounds = new bounds((t.x - bound.x) / mul, (t.y - bound.y) / mul, t.w - t.x, t.h - t.y);
                        repaintAll();

                    } else if (Mouse.IsHeld(MouseUtil.LEFT)) {
                        if(Keys.isHeld(KeyUtil.CONTROL) || Keys.isHeld(KeyUtil.SHIFT)){
                            if(dr == null){

                                if (v.SelBounds == null) {
                                    dr = new PlaneDrag(new bounds((t.x - bound.x) / mul,
                                            (t.y - bound.y) / mul, t.w - t.x, t.h - t.y));
                                    repaint();
                                } else {
                                    dr = new PlaneDrag(v.SelBounds);
                                    repaint();
                                }
                            }

                        } else {
                            n.tileOff = v.TileSelected;
                            n.palLine = v.PalLine;
                            mappings.SetMap(n, o);

                            v.MapSelected = o;
                            v.SelBounds = new bounds((t.x - bound.x) / mul, (t.y - bound.y) / mul, t.w - t.x, t.h - t.y);
                            repaint();
                        }

                    } else if (Mouse.IsHeld(MouseUtil.RIGHT)) {
                        if(Keys.isHeld(KeyUtil.CONTROL) || Keys.isHeld(KeyUtil.SHIFT)){
                            if(dr == null){
                                dr = new PlaneDrag(v.SelBounds);
                                repaint();
                            }

                        } else {
                            if (v.SelStart == null) {
                                v.SelStart = new bounds(t.x, t.y, 0, 0);
                            }

                            v.SelEnd = new bounds(t.w, t.h, 0, 0);
                            v.SelBounds = new bounds((v.SelStart.x - bound.x) / mul, (v.SelStart.y - bound.y) / mul,
                                    (v.SelEnd.x - v.SelStart.x) / mul, (v.SelEnd.y - v.SelStart.y) / mul);

                            if (-v.SelBounds.w > -1) {
                                v.SelBounds.x = (v.SelEnd.x - bound.x) / mul - w;
                                v.SelBounds.w = ((v.SelStart.x - bound.x) / mul + w) - v.SelBounds.x;
                            }

                            if (-v.SelBounds.h > -1) {
                                v.SelBounds.y = (v.SelEnd.y - bound.y) / mul - h;
                                v.SelBounds.h = ((v.SelStart.y - bound.y) / mul + h) - v.SelBounds.y;
                            }

                            if (!Event.SelMenu) {
                                Event.projectMenu();
                            }
                            repaint();
                        }
                    } else {
                        v.SelStart = null;
                        v.SelEnd = null;
                    }

                    if(last != o) {
                        last = o;
                        repaint();
                    }
                }
            }
        }

        if(dr != null){
            if(Keys.isHeld(KeyUtil.CONTROL) || Keys.isHeld(KeyUtil.SHIFT)){
                dr.logic(bound, Mouse.IsHeld(MouseUtil.LEFT) || Mouse.IsHeld(MouseUtil.RIGHT));
                lastHeld = Keys.isHeld(KeyUtil.SHIFT);

            } else {
                dr.set(lastHeld);
                dr = null;
                repaint();
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

    @Override
    public boolean drawBound() {
        return true;
    }

    private void repaint() {
        SP.getWM().getPanelManager(this).repaint();
    }

    private void repaintAll() {
        SP.getWM().repaintAll();
    }
}
