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

import javax.swing.*;
import java.awt.*;

public class tileEditor implements Window {
    private int last;
    private bounds bound;

    public tileEditor(){
        bound = new bounds(windowManager.defaultBounds());
    }

    @Override
    public void draw(Graphics g, bounds b, float a) {
        if (bound.y + bound.h > 0 && v.TileSelected < tileLoader.GetTextureAmount(v.PalLine) && v.TileSelected >= 0 &&
                v.TileSelected == v.TileSelectedEnd) {
            Sprite s = new Sprite(tileLoader.GetTexture(v.PalLine, v.TileSelected));
            s.setBounds(b.x, b.y, bound.w, bound.h);
            s.setAlpha(a);
            g.drawImage(s);

            float mul = getMul();
            for (int y = 0; y < tileLoader.GetHeight(); y++) {
                for (int x = 0; x < tileLoader.GetWidth(); x++) {

                    bounds t = new bounds((int)(b.x + (mul * x)), (int)(b.y + (mul * y)), Math.round(mul), Math.round(mul));
                    if (Mouse.IsInArea(new bounds(t.x + bound.x - b.x, t.y + bound.y - b.y,
                            t.x + t.w + bound.x - b.x, t.y + t.h + bound.y - b.y))) {

                        s = new Sprite();
                        s.setColor(Color.RED);
                        v.DrawBounds(g, s, t, 1, 4);
                    }
                }
            }
        }
    }

    public void logic() {
        if(v.TileSelected < tileLoader.GetTextureAmount(v.PalLine) && v.TileSelected >= 0 && v.TileSelected == v.TileSelectedEnd) {
            float mul = getMul();

            for (int y = 0, o = 0; y < tileLoader.GetHeight(); y++) {
                for (int x = 0; x < tileLoader.GetWidth(); x++, o++) {
                    bounds t = new bounds((int)(bound.x + (x * mul)), (int)(bound.y + (y * mul)),
                            (int)(bound.x + ((x + 1) * mul)), (int)(bound.y + ((y + 1) * mul)));

                    if (Mouse.IsInArea(t)) {

                        if (Mouse.IsHeld(MouseUtil.LEFT)) {
                            int[] q = tileLoader.GetTile(v.TileSelected);
                            q[o] = v.PalSelcted;

                            tileLoader.SetTile(q, v.TileSelected);
                            tileLoader.render(v.TileSelected);
                            repaintAll();
                        }

                        if (Mouse.IsClicked(MouseUtil.RIGHT) || Mouse.IsClicked(MouseUtil.MIDDLE)) {
                            v.PalSelcted = tileLoader.GetTile(v.TileSelected)[o];
                            repaintAll();
                        }

                        if (last != o) {
                            last = o;
                            repaint();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void create() {

    }

    private void repaint() {
        SP.getWM().getPanelManager(this).repaint();
    }

    private void repaintAll() {
        SP.getWM().repaintAll();
    }

    @Override
    public bounds getBounds() {
        return bound;
    }

    @Override
    public boolean canUnFocus(){
        return !(Mouse.IsHeld(MouseUtil.RIGHT) || Mouse.IsHeld(MouseUtil.MIDDLE) || Mouse.IsHeld(MouseUtil.LEFT));
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
        bound.w = tileLoader.GetWidth() * 16;
        bound.h = tileLoader.GetHeight() * 16;
    }

    @Override
    public void resize(int width, int height) {
        if(width > bound.w){
            bound.w = width;
            bound.h = width;

            if(width > App.GetBounds().h - PanelManager.BOUNDS_SIZE - PanelManager.UPPER_HEIGHT){
                bound.w = App.GetBounds().h - PanelManager.BOUNDS_SIZE - PanelManager.UPPER_HEIGHT;
                bound.h = bound.w;
            }
        } else if(width < bound.w){
            bound.w = width;
            bound.h = width;

            if(width < tileLoader.GetWidth() * 10){
                bound.w = tileLoader.GetWidth() * 10;
                bound.h = bound.w;
            }

        } else if(height > bound.h){
            bound.w = height;
            bound.h = height;

            if(height > App.GetBounds().h - PanelManager.BOUNDS_SIZE - PanelManager.UPPER_HEIGHT){
                bound.w = App.GetBounds().h - PanelManager.BOUNDS_SIZE - PanelManager.UPPER_HEIGHT;
                bound.h = bound.w;
            }
        } else if(height < bound.h){
            bound.w = height;
            bound.h = height;

            if(height < tileLoader.GetWidth() * 10){
                bound.w = tileLoader.GetWidth() * 10;
                bound.h = bound.w;
            }
        }
    }

    @Override
    public void move(int x, int y) {

    }

    public float getMul() {
        return bound.w / tileLoader.GetWidth();
    }
}
