package gs.soni.plane.draw;

import gs.app.lib.application.App;
import gs.app.lib.gfx.Graphics;
import gs.app.lib.gfx.Sprite;
import gs.app.lib.math.bounds;
import gs.app.lib.util.KeyUtil;
import gs.app.lib.util.MouseUtil;
import gs.soni.plane.SP;
import gs.soni.plane.project.map;
import gs.soni.plane.project.mappings;
import gs.soni.plane.project.tileLoader;
import gs.soni.plane.util.*;
import gs.soni.plane.v;

import java.awt.*;

public class plane implements Window {
    private bounds bound;
    private int last;
    private PlaneDrag dr;
    private boolean lastHeld = false;
    private boolean cOver;

    /* bounds for plane selection */
    public bounds selBounds;
    public bounds selStart;
    public bounds selEnd;

    public plane(){
        bound = new bounds(windowManager.defaultBounds());
    }

    @Override
    public bounds getBounds() {
        return bound;
    }

    @Override
    public void draw(Graphics g, bounds b, float a) {
        int w = tileLoader.GetWidth(), h = tileLoader.GetHeight();
        float mul = getMul();
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

                bounds t = new bounds(b.x + (int)(mul * x * w ), b.y + (int)(mul * y * h),
                        b.x + (int)(mul * (x + 1) * w), b.y + (int)(mul * (y + 1) * h));

                spr.setBounds(t.x, t.y, (int)(mul * w), (int)(mul * h));
                spr.setFlip(n.XFlip, n.YFlip);
                spr.setAlpha(a);
                g.drawImage(spr);

                if (Mouse.IsInArea(new bounds(t.x + bound.x - b.x, t.y + bound.y - b.y, t.w + bound.x - b.x, t.h + bound.y - b.y))) {
                    v.DrawBounds(g, s, new bounds(t.x, t.y, t.w - t.x, t.h - t.y), 0, 2);

                } else if (isSelected(n.tileOff)) {
                    s.setColor(Color.GREEN);
                    v.DrawBounds(g, s, new bounds(t.x, t.y, t.w - t.x, t.h - t.y), 0, 2);
                    s.setColor(Color.RED);
                }
            }
        }

        if(dr != null) {
            dr.draw(g, new bounds(b.x, b.y, bound.w, bound.h), mul);

        } else if(selBounds != null) {
            v.DrawBounds(g, s, new bounds(b.x + (int)(mul * selBounds.x), b.y + (int)(mul * selBounds.y),
                    (int)(mul * selBounds.w), (int)(mul * selBounds.h )), 0, 2);
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
        int w = tileLoader.GetWidth(), h = tileLoader.GetHeight();
        float mul = getMul();
        for(int y = 0, o = 0;y < v.mapSize.y;y ++) {
            for (int x = 0; x < v.mapSize.x; x++, o++) {
                if (o >= mappings.GetMapArray().length) {
                    break;
                }

                map n = mappings.GetMapArray()[o];
                bounds t = new bounds((bound.x + (int)(x * w * mul)), (bound.y + (int)(y * h * mul)),
                        bound.x + (int)((x + 1) * w * mul), bound.y + (int)((y + 1) * h * mul));
                if (Mouse.IsInArea(t)) {

                    if (Mouse.IsClicked(MouseUtil.MIDDLE) || Keys.isPressed(KeyUtil.P)) {
                        v.TileSelected = n.tileOff;
                        v.TileSelectedEnd = n.tileOff;
                        v.MapSelected = o;

                        selBounds = new bounds((int)((t.x - bound.x) / mul), (int)((t.y - bound.y) / mul),
                                (int)((t.w - t.x) / mul), (int)((t.h - t.y) / mul));
                        repaintAll();

                    } else if (Mouse.IsHeld(MouseUtil.LEFT)) {
                        if(Keys.isHeld(KeyUtil.CONTROL) || Keys.isHeld(KeyUtil.SHIFT)){
                            if(dr == null){

                                if (selBounds == null) {
                                    dr = new PlaneDrag(new bounds((int)((float)(t.x - bound.x) / mul),
                                                    (int)((float)(t.y - bound.y) / mul), t.w - t.x, t.h - t.y));
                                    repaint();
                                } else {
                                    dr = new PlaneDrag(selBounds);
                                    repaint();
                                }
                            }

                        } else {
                            n.tileOff = v.TileSelected;
                            n.palLine = v.PalLine;
                            mappings.SetMap(n, o);

                            v.MapSelected = o;
                            selBounds = new bounds((int)((t.x - bound.x) / mul), (int)((t.y - bound.y) / mul),
                                    (int)((t.w - t.x) / mul), (int)((t.h - t.y) / mul));
                            repaint();
                            SP.repaint();
                        }

                    } else if (Mouse.IsHeld(MouseUtil.RIGHT)) {
                        if(Keys.isHeld(KeyUtil.CONTROL) || Keys.isHeld(KeyUtil.SHIFT)){
                            if(dr == null){
                                dr = new PlaneDrag(selBounds);
                                repaint();
                            }

                        } else {
                            if (selStart == null) {
                                selStart = new bounds(t.x, t.y, 0, 0);
                            }

                            selEnd = new bounds(t.w, t.h, 0, 0);
                            selBounds = new bounds((int)((float)(selStart.x - bound.x) / mul),
                                    (int)((float)(selStart.y - bound.y) / mul), (int)((float)(selEnd.x - selStart.x) / mul),
                                    (int)((float)(selEnd.y - selStart.y) / mul));

                            if (-selBounds.w > -1) {
                                selBounds.x = (int)((float)(selEnd.x - bound.x) / mul) - w;
                                selBounds.w = (int)((float)(selStart.x - bound.x) / mul + w) - selBounds.x;
                            }

                            if (-selBounds.h > -1) {
                                selBounds.y = (int)((float)(selEnd.y - bound.y) / mul) - h;
                                selBounds.h = (int)((float)(selStart.y - bound.y) / mul + h) - selBounds.y;
                            }

                            App.getJFrame().getMenuBar().getMenu(defMenu.MENU_SEL).setEnabled(true);
                            repaint();
                            SP.repaint();
                        }
                    } else {
                        selStart = null;
                        selEnd = null;
                    }

                    if(last != o) {
                        last = o;
                        repaint();
                    }
                }
            }
        }

        if(dr != null){
            cOver = true;
            if(Mouse.IsHeld(MouseUtil.LEFT) || Mouse.IsHeld(MouseUtil.RIGHT)){
                SP.getWM().getPanelManager(this).toCursor(CursorList.get(CursorList.GRAB2_CURSOR));

            } else {
                SP.getWM().getPanelManager(this).toCursor(CursorList.get(CursorList.GRAB_CURSOR));
            }

            if(Keys.isHeld(KeyUtil.CONTROL) || Keys.isHeld(KeyUtil.SHIFT)){
                dr.logic(bound, Mouse.IsHeld(MouseUtil.LEFT) || Mouse.IsHeld(MouseUtil.RIGHT), getMul());
                lastHeld = Keys.isHeld(KeyUtil.SHIFT);

            } else {
                dr.set(lastHeld, this);
                dr = null;
                cOver = false;
                SP.getWM().getPanelManager(this).toCursor(CursorList.get(Cursor.DEFAULT_CURSOR));
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

    @Override
    public boolean cursorOverride() {
        return cOver;
    }

    @Override
    public void defaultSize() {
        bound.w = v.mapSize.x * tileLoader.GetWidth();
        bound.h = v.mapSize.y * tileLoader.GetHeight();
    }

    @Override
    public void resize(int width, int height) {
        bound.w = (width / (v.mapSize.x * tileLoader.GetWidth())) * (v.mapSize.x * tileLoader.GetWidth());
        bound.h = (height / (v.mapSize.y * tileLoader.GetHeight())) * (v.mapSize.y * tileLoader.GetHeight());
    }

    @Override
    public void move(int x, int y) {

    }

    private void repaint() {
        SP.getWM().getPanelManager(this).repaint();
    }

    private void repaintAll() {
        SP.getWM().repaintAll();
    }

    public float getMul() {
        float w = bound.w / ((float)v.mapSize.x * tileLoader.GetWidth()), h = bound.h / ((float)v.mapSize.y * tileLoader.GetHeight());
        return w > h ? h : w;
    }
}
