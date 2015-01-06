package gs.soni.plane.draw;

import gs.app.lib.application.App;
import gs.app.lib.gfx.Graphics;
import gs.app.lib.gfx.Sprite;
import gs.app.lib.math.bounds;
import gs.app.lib.util.KeyUtil;
import gs.app.lib.util.MouseUtil;
import gs.soni.plane.SP;
import gs.soni.plane.project.tileLoader;
import gs.soni.plane.util.Keys;
import gs.soni.plane.util.Mouse;
import gs.soni.plane.v;

import java.awt.*;

public class tileDisp implements Window {
    public static int drawn;
    private bounds bound;
    private int last = 0;
    private boolean lastInTile;

    public tileDisp(){
        bound = new bounds(windowManager.defaultBounds());
    }

    @Override
    public bounds getBounds() {
        return bound;
    }

    @Override
    public void draw(Graphics g, bounds b, float a) {
        int w = tileLoader.GetWidth(), h = tileLoader.GetHeight();

        Sprite s = new Sprite();
        s.setColor(Color.RED);
        drawn = 0;

        int w_ = bound.w / (w + 2);
        if (w_ > 0) {
            for (int o = 0; o < tileLoader.GetTextureAmount(v.PalLine); o++) {
                int x = o % w_, y = o / w_;
                bounds t = new bounds(b.x + (x * (w + 2)), b.y + (y * (h + 2)),
                        b.x + ((x + 1) * (w + 2)) - 2, b.y + ((y + 1) * (h + 2)) - 2);

                if (t.y < App.GetBounds().h) {
                    g.drawImage(tileLoader.GetTexture(v.PalLine, o), t.x, t.y, w, h);
                    drawn ++;

                    if (Mouse.IsInArea(new bounds(bound.x + t.x - b.x, bound.y + t.y - b.y, bound.x + t.w - b.x, bound.y + t.h - b.y))) {
                        v.DrawBounds(g, s, new bounds(t.x, t.y, t.w - t.x, t.h - t.y), 2, 2);

                    } else if(isSelected(o)){
                        s.setColor(Color.MAGENTA);
                        v.DrawBounds(g, s, new bounds(t.x, t.y, t.w - t.x, t.h - t.y), 2, 2);
                        s.setColor(Color.RED);
                    }
                }
            }
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
        boolean inTile = false;

        int w = tileLoader.GetWidth(), h = tileLoader.GetHeight();
        int w_ = bound.w / (w + 2);
        if (w_ > 0) {
            for (int o = 0; o < tileLoader.GetTextureAmount(v.PalLine); o++) {
                int x = o % w_, y = o / w_;
                bounds t = new bounds(bound.x + (x * (w + 2)), bound.y + (y * (h + 2)),
                        bound.x + ((x + 1) * (w + 2)) - 2, bound.y + ((y + 1) * (h + 2)) - 2);

                if (Mouse.IsInArea(t)) {
                    if (Keys.isHeld(KeyUtil.SHIFT)) {
                        if (Mouse.IsClicked(MouseUtil.LEFT)) {
                            v.TileSelectedEnd = o;
                            repaintAll();
                        }

                    } else {
                        if (Mouse.IsClicked(MouseUtil.LEFT)) {
                            v.TileSelected = o;
                            v.TileSelectedEnd = o;
                            repaintAll();
                        }
                    }

                    if(last != o){
                        last = o;
                        repaint();
                    }

                    inTile = true;
                    lastInTile = true;
                }
            }
        }

        if(lastInTile && !inTile){
            lastInTile = false;
            repaint();
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

    @Override
    public boolean cursorOverride() {
        return false;
    }

    @Override
    public void defaultSize() {
        OptimizeBound();
    }

    @Override
    public void resize(int width, int height) {
        int w = tileLoader.GetWidth() + 2, h = tileLoader.GetHeight() + 2;
        bound.w = (width / w) * w;
        bound.h = (height / h) * h;

        if(bound.w < w * 8){
            bound.w = w * 8;
        }

        if(bound.h < h){
            bound.h = h;
        }
    }

    @Override
    public void move(int x, int y) {

    }

    private void OptimizeBound() {
        int closest = 0, left = Integer.MAX_VALUE, textures = tileLoader.GetTextureAmount(v.PalLine);
        for(int i = 3;i <= 8;i ++){
            int n = (int) ((((float)textures / i) % 1) * 1000);

            if(n <= left){
                left = n;
                closest = i;
            }
        }

        resizeTo((textures / (closest + 1)) + 1, closest + 1);
    }

    private void resizeTo(int width, int height) {
        bound.w = width * (tileLoader.GetWidth() + 2);
        bound.h = height * (tileLoader.GetHeight() + 2);
    }
}
