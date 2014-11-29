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

import java.awt.*;

public class tileEditor implements Drawable, Logicable {

    private int last;
    private boolean area;

    public tileEditor(){
        SP.addToRenderList(this);
    }

    @Override
    public void draw(Graphics g) {
        if (v.TilEdBounds.y + v.TilEdBounds.h > 0 && v.TileSelected < tileLoader.GetTextureAmount(v.PalLine) && v.TileSelected >= 0) {
            Sprite s = new Sprite(tileLoader.GetTexture(v.PalLine, v.TileSelected));
            s.setBounds(v.TilEdBounds.x, v.TilEdBounds.y, v.TilEdBounds.w, v.TilEdBounds.h);
            s.setAlpha(1f);
            g.drawImage(s);

            for (int y = 0; y < tileLoader.GetHeight(); y++) {
                for (int x = 0; x < tileLoader.GetWidth(); x++) {

                    bounds t = new bounds(v.TilEdBounds.x + (x * 16), v.TilEdBounds.y + (y * 16),
                            v.TilEdBounds.x + ((x + 1) * 16), v.TilEdBounds.y + ((y + 1) * 16));
                    if (Mouse.IsInArea(t)) {

                        s = new Sprite();
                        s.setColor(Color.RED);
                        v.DrawBounds(g, s, new bounds(t.x, t.y, t.w - t.x, t.h - t.y), 0, 2);
                    }
                }
            }
        }

        Sprite s = new Sprite();
        s.setColor(Color.RED);
        v.DrawBounds(g, s, v.TilEdBounds, 2, 2);
    }

    @Override
    public int renderPriority() {
        return v.RENDERPR_MIN;
    }

    @Override
    public void logic() {
        boolean inArea = false;

        for (int y = 0, o = 0; y < tileLoader.GetHeight(); y++) {
            for (int x = 0; x < tileLoader.GetWidth(); x++, o++) {
                bounds t = new bounds(v.TilEdBounds.x + (x * 16), v.TilEdBounds.y + (y * 16),
                        v.TilEdBounds.x + ((x + 1) * 16), v.TilEdBounds.y + ((y + 1) * 16));

                if (Mouse.IsInArea(t)) {
                    inArea = true;

                    if (Mouse.IsHeld(MouseUtil.LEFT)) {
                        int[] q = tileLoader.GetTile(v.TileSelected);
                        q[o] = v.PalSelcted;
                        tileLoader.SetTile(q, v.TileSelected);
                        tileLoader.render(v.TileSelected);
                        SP.repaintLater();
                    }
                    if (Mouse.IsClicked(MouseUtil.RIGHT) || Mouse.IsClicked(MouseUtil.MIDDLE)) {
                        v.PalSelcted = tileLoader.GetTile(v.TileSelected)[o];
                        SP.repaintLater();
                    }

                    if(last != o){
                        last = o;
                        area = true;
                        SP.repaintLater();
                    }
                }
            }
        }

        if(!inArea && area){
            area = false;
            SP.repaintLater();
        }
    }
}
