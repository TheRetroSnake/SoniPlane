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
        bound = v.TileBounds;
    }

    @Override
    public bounds getBounds() {
        return bound;
    }

    @Override
    public void draw(Graphics g) {
        bound = v.TileBounds;
        g.clearScreen(Color.BLACK);
        int w = tileLoader.GetWidth(), h = tileLoader.GetHeight();

        Sprite s = new Sprite();
        s.setColor(Color.RED);
        drawn = 0;

        int w_ = bound.w / (w + 2);
        if (w_ > 0) {
            for (int o = 0; o < tileLoader.GetTextureAmount(v.PalLine); o++) {
                int x = o % w_, y = o / w_;
                bounds t = new bounds((x * (w + 2)), (y * (h + 2)), ((x + 1) * (w + 2)) - 2, ((y + 1) * (h + 2)) - 2);

                if (t.y < App.GetBounds().h) {
                    g.drawImage(tileLoader.GetTexture(v.PalLine, o), t.x, t.y, w, h);
                    drawn ++;

                    if (Mouse.IsInArea(new bounds(bound.x + t.x, bound.y + t.y, bound.x + t.w, bound.y + t.h))) {
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
}
