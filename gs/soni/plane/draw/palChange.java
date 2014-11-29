package gs.soni.plane.draw;

import gs.app.lib.gfx.Graphics;
import gs.app.lib.gfx.Sprite;
import gs.app.lib.math.bounds;
import gs.app.lib.util.MouseUtil;
import gs.soni.plane.SP;
import gs.soni.plane.project.palette;
import gs.soni.plane.util.Logicable;
import gs.soni.plane.util.Mouse;
import gs.soni.plane.util.Style;
import gs.soni.plane.util.StyleItem;
import gs.soni.plane.v;

import java.awt.*;

public class palChange implements Drawable, Logicable {
    private final int Size = 16;
    private boolean draw;

    public palChange(){
        SP.addToRenderList(this);
    }

    @Override
    public void draw(Graphics g) {
        if(((v.PalChgBounds.w / 5) * 4) - 50 > 0 && v.PalChgBounds.y + v.PalChgBounds.h > 0) {
            Sprite s = new Sprite();
            StyleItem st = Style.GetStyle("menu_center");

            // render big palette and red border next to it
            s.setBounds(v.PalChgBounds.x, v.PalChgBounds.y, v.PalChgBounds.w / 5, v.PalChgBounds.h);
            s.setColor(palette.getPalette(v.PalLine, v.PalSelcted));
            g.fillRect(s);
            s.setBounds(v.PalChgBounds.x + (v.PalChgBounds.w / 5), v.PalChgBounds.y, 2, v.PalChgBounds.h);
            s.setColor(Color.RED);
            g.fillRect(s);

            // draw drag-thingies
            s.setBounds(v.PalChgBounds.x + (v.PalChgBounds.w / 5) + 54 - (Size / 2), v.PalChgBounds.y + (v.PalChgBounds.h / 4) -
                            (Size / 2), ((v.PalChgBounds.w / 5) * 4) - 50 + (Size / 2), Size);
            g.fillRect(s);
            s.setBounds(v.PalChgBounds.x + (v.PalChgBounds.w / 5) + 54 - (Size / 2), v.PalChgBounds.y + ((v.PalChgBounds.h / 4) * 2) - 
                            (Size / 2), ((v.PalChgBounds.w / 5) * 4) - 50 + (Size / 2), Size);
            s.setColor(Color.GREEN);
            g.fillRect(s);
            s.setBounds(v.PalChgBounds.x + (v.PalChgBounds.w / 5) + 54 - (Size / 2), v.PalChgBounds.y + ((v.PalChgBounds.h / 4) * 3) - 
                            (Size / 2), ((v.PalChgBounds.w / 5) * 4) - 50 + (Size / 2), Size);
            s.setColor(Color.BLUE);
            g.fillRect(s);


            // draw palette values
            Graphics.setFont(st.GetFont());
            g.setColor(st.GetColor());
            int x = v.PalChgBounds.x + (v.PalChgBounds.w / 5) + 8;

            String tx = Integer.toHexString(palette.ColorToInt(palette.getPalette(v.PalLine, v.PalSelcted).getRed())).toUpperCase();
            int off1 = (int) (v.PalChgBounds.y + (v.PalChgBounds.h / 4) - (Graphics.GetTextHeight(tx) / 2));
            g.drawText(tx, x, off1);

            tx = Integer.toHexString(palette.ColorToInt(palette.getPalette(v.PalLine, v.PalSelcted).getGreen())).toUpperCase();
            int off2 = (int) (v.PalChgBounds.y + ((v.PalChgBounds.h / 4) * 2) - (Graphics.GetTextHeight(tx) / 2));
            g.drawText(tx, x, off2);

            tx = Integer.toHexString(palette.ColorToInt(palette.getPalette(v.PalLine, v.PalSelcted).getBlue())).toUpperCase();
            int off3 = (int) (v.PalChgBounds.y + ((v.PalChgBounds.h / 4) * 3) - (Graphics.GetTextHeight(tx) / 2));
            g.drawText(tx, x, off3);

            x = v.PalChgBounds.x + (v.PalChgBounds.w / 5) + 54 - (Size / 2);
            PalSelector(new bounds(x, off1 + 2, ((v.PalChgBounds.w / 5) * 4) - 54, Size + 2),
                    palette.getPalette(v.PalLine, v.PalSelcted).getRed(), s, g);

            PalSelector(new bounds(x, off2 + 2, ((v.PalChgBounds.w / 5) * 4) - 54, Size + 2),
                    palette.getPalette(v.PalLine, v.PalSelcted).getGreen(), s, g);

            PalSelector(new bounds(x, off3 + 2, ((v.PalChgBounds.w / 5) * 4) - 54, Size + 2),
                    palette.getPalette(v.PalLine, v.PalSelcted).getBlue(), s, g);

            s.setColor(Color.RED);
            v.DrawBounds(g, s, v.PalChgBounds, 2, 2);
        }
    }

    private void PalSelector(bounds bounds, int color, Sprite s, Graphics g) {
        if(Mouse.IsInArea(new bounds(bounds.x, bounds.y, bounds.x + bounds.w, bounds.y + bounds.h))){
            if(Mouse.IsHeld(MouseUtil.LEFT) || Mouse.IsHeld(MouseUtil.RIGHT)){
                s.setColor(new Color(155, 155, 155));

            } else {
                s.setColor(new Color(205, 205, 205));
            }
        } else {
            s.setColor(Color.WHITE);
        }

        float pos = (((float)palette.GetGrid(color) / 256) * bounds.w) - 4;
        s.setBounds((int) (bounds.x + pos), bounds.y, Size, bounds.h);
        g.fillRect(s);
    }

    @Override
    public int renderPriority() {
        return v.RENDERPR_MIN;
    }

    @Override
    public void logic() {

        int x = v.PalChgBounds.x + (v.PalChgBounds.w / 5) + 54 - (Size / 2);

        String tx = Integer.toHexString(palette.ColorToInt(palette.getPalette(v.PalLine, v.PalSelcted).getRed())).toUpperCase();
        int off1 = (int) (v.PalChgBounds.y + (v.PalChgBounds.h / 4) - (Graphics.GetTextHeight(tx) / 2));

        tx = Integer.toHexString(palette.ColorToInt(palette.getPalette(v.PalLine, v.PalSelcted).getGreen())).toUpperCase();
        int off2 = (int) (v.PalChgBounds.y + ((v.PalChgBounds.h / 4) * 2) - (Graphics.GetTextHeight(tx) / 2));

        tx = Integer.toHexString(palette.ColorToInt(palette.getPalette(v.PalLine, v.PalSelcted).getBlue())).toUpperCase();
        int off3 = (int) (v.PalChgBounds.y + ((v.PalChgBounds.h / 4) * 3) - (Graphics.GetTextHeight(tx) / 2));

        CheckDraw(new bounds(x, off1 + 4, ((v.PalChgBounds.w / 5) * 4) - 54, Size), 0);
        CheckDraw(new bounds(x, off2 + 4, ((v.PalChgBounds.w / 5) * 4) - 54, Size), 1);
        CheckDraw(new bounds(x, off3 + 4, ((v.PalChgBounds.w / 5) * 4) - 54, Size), 2);
    }

    private void CheckDraw(bounds bounds, int mode) {
        if(Mouse.IsInArea(new bounds(bounds.x, bounds.y, bounds.x + bounds.w, bounds.y + bounds.h))) {
            if (Mouse.IsHeld(MouseUtil.LEFT) || Mouse.IsHeld(MouseUtil.RIGHT)) {
                Color c = palette.getPalette(v.PalLine, v.PalSelcted);
                int pal = palette.GetGrid((int) (((float)Mouse.GetPos().x - bounds.x) / bounds.w * 256));

                switch (mode) {
                    case 0:
                        c = new Color(pal, c.getGreen(), c.getBlue());
                        break;
                    case 1:
                        c = new Color(c.getRed(), pal, c.getBlue());
                        break;
                    case 2:
                        c = new Color(c.getRed(), c.getGreen(), pal);
                        break;
                }

                palette.setPalette(v.PalLine, v.PalSelcted, c);
                SP.repaintLater();
                v.TileRender = -1;
                SP.startTileRender();

            } else {
                if(draw){
                    draw = false;
                    SP.repaintLater();
                }
            }
        }  else {
            if(!draw){
                draw = true;
                SP.repaintLater();
            }
        }
    }
}
