package gs.soni.plane.draw;

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
        bound = v.TilEdBounds;
    }

    @Override
    public void draw(Graphics g) {
        bound = v.TilEdBounds;
        if (bound.y + bound.h > 0 && v.TileSelected < tileLoader.GetTextureAmount(v.PalLine) && v.TileSelected >= 0 &&
                v.TileSelected == v.TileSelectedEnd) {
            Sprite s = new Sprite(tileLoader.GetTexture(v.PalLine, v.TileSelected));
            s.setBounds(0, 0, bound.w, bound.h);
            s.setAlpha(1f);
            g.drawImage(s);

            for (int y = 0; y < tileLoader.GetHeight(); y++) {
                for (int x = 0; x < tileLoader.GetWidth(); x++) {

                    bounds t = new bounds(x * 16, y * 16, 16, 16);
                    if (Mouse.IsInArea(new bounds(t.x + bound.x, t.y + bound.y, t.x + t.w + bound.x, t.y + t.h + bound.y))) {

                        s = new Sprite();
                        s.setColor(Color.RED);
                        v.DrawBounds(g, s, t, 0, 2);
                    }
                }
            }
        }
    }

    public void logic() {
        if(v.TileSelected < tileLoader.GetTextureAmount(v.PalLine) && v.TileSelected >= 0 && v.TileSelected == v.TileSelectedEnd) {
            for (int y = 0, o = 0; y < tileLoader.GetHeight(); y++) {
                for (int x = 0; x < tileLoader.GetWidth(); x++, o++) {
                    bounds t = new bounds(bound.x + (x * 16), bound.y + (y * 16),
                            bound.x + ((x + 1) * 16), bound.y + ((y + 1) * 16));

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
}
