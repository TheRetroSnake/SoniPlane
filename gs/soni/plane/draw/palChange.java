package gs.soni.plane.draw;

import gs.app.lib.gfx.Graphics;
import gs.app.lib.gfx.Sprite;
import gs.app.lib.math.bounds;
import gs.app.lib.util.MouseUtil;
import gs.soni.plane.SP;
import gs.soni.plane.project.palette;
import gs.soni.plane.util.Mouse;
import gs.soni.plane.util.Style;
import gs.soni.plane.util.StyleItem;
import gs.soni.plane.v;

import java.awt.*;

public class palChange implements Window {
    private final int Size = 16;
    private boolean draw;
    private bounds bound;
    private int sel = -1;

    public palChange(){
        bound = v.PalChgBounds;
    }

    @Override
    public bounds getBounds() {
        return bound;
    }

    @Override
    public void draw(Graphics g) {
        bound = v.PalChgBounds;
        if(((bound.w / 5) * 4) - 50 > 0 && bound.y + bound.h > 0) {
            g.clearScreen(Color.BLACK);
            Sprite s = new Sprite();
            StyleItem st = Style.GetStyle("menu_center");

            // render big palette and red border next to it
            s.setBounds(0, 0, bound.w / 5, bound.h);
            s.setColor(palette.getPalette(v.PalLine, v.PalSelcted));
            g.fillRect(s);
            s.setBounds((bound.w / 5), 0, 2, bound.h);
            s.setColor(Color.RED);
            g.fillRect(s);

            // draw drag-thingies
            s.setBounds((bound.w / 5) + 54 - (Size / 2), (bound.h / 4) - (Size / 2), ((bound.w / 5) * 4) - 50 + (Size / 2), Size);
            g.fillRect(s);
            s.setBounds((bound.w / 5) + 54 - (Size / 2), ((bound.h / 4) * 2) - (Size / 2), ((bound.w / 5) * 4) - 50 + (Size / 2), Size);
            s.setColor(Color.GREEN);
            g.fillRect(s);
            s.setBounds((bound.w / 5) + 54 - (Size / 2), ((bound.h / 4) * 3) - (Size / 2), ((bound.w / 5) * 4) - 50 + (Size / 2), Size);
            s.setColor(Color.BLUE);
            g.fillRect(s);


            // draw palette values
            Graphics.setFont(st.GetFont());
            g.setColor(st.GetColor());
            int x = (bound.w / 5) + 8;

            String tx = Integer.toHexString(palette.ColorToInt(palette.getPalette(v.PalLine, v.PalSelcted).getRed())).toUpperCase();
            int off1 = (int) ((bound.h / 4) - (Graphics.GetTextHeight(tx) / 2));
            g.drawText(tx, x, off1);

            tx = Integer.toHexString(palette.ColorToInt(palette.getPalette(v.PalLine, v.PalSelcted).getGreen())).toUpperCase();
            int off2 = (int) (((bound.h / 4) * 2) - (Graphics.GetTextHeight(tx) / 2));
            g.drawText(tx, x, off2);

            tx = Integer.toHexString(palette.ColorToInt(palette.getPalette(v.PalLine, v.PalSelcted).getBlue())).toUpperCase();
            int off3 = (int) (((bound.h / 4) * 3) - (Graphics.GetTextHeight(tx) / 2));
            g.drawText(tx, x, off3);

            x = (bound.w / 5) + 54 - (Size / 2);
            PalSelector(new bounds(x, off1 + 2, ((bound.w / 5) * 4) - 54, Size + 2),
                    palette.getPalette(v.PalLine, v.PalSelcted).getRed(), s, g);

            PalSelector(new bounds(x, off2 + 2, ((bound.w / 5) * 4) - 54, Size + 2),
                    palette.getPalette(v.PalLine, v.PalSelcted).getGreen(), s, g);

            PalSelector(new bounds(x, off3 + 2, ((bound.w / 5) * 4) - 54, Size + 2),
                    palette.getPalette(v.PalLine, v.PalSelcted).getBlue(), s, g);
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
    public void logic() {
        int x = bound.x + (bound.w / 5) + 54 - (Size / 2);

        String tx = Integer.toHexString(palette.ColorToInt(palette.getPalette(v.PalLine, v.PalSelcted).getRed())).toUpperCase();
        int off1 = (int) (bound.y + (bound.h / 4) - (Graphics.GetTextHeight(tx) / 2));

        tx = Integer.toHexString(palette.ColorToInt(palette.getPalette(v.PalLine, v.PalSelcted).getGreen())).toUpperCase();
        int off2 = (int) (bound.y + ((bound.h / 4) * 2) - (Graphics.GetTextHeight(tx) / 2));

        tx = Integer.toHexString(palette.ColorToInt(palette.getPalette(v.PalLine, v.PalSelcted).getBlue())).toUpperCase();
        int off3 = (int) (bound.y + ((bound.h / 4) * 3) - (Graphics.GetTextHeight(tx) / 2));

        CheckDraw(new bounds(x, off1 + 4, ((bound.w / 5) * 4) - 54, Size), 0);
        CheckDraw(new bounds(x, off2 + 4, ((bound.w / 5) * 4) - 54, Size), 1);
        CheckDraw(new bounds(x, off3 + 4, ((bound.w / 5) * 4) - 54, Size), 2);
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

    private void CheckDraw(bounds bounds, int mode) {
        if(Mouse.IsInArea(new bounds(bounds.x, bounds.y, bounds.x + bounds.w, bounds.y + bounds.h)) && (canUnFocus() || sel == mode)) {
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
                repaint();
                v.TileRender = -1;
                SP.startTileRender();

            } else {
                sel = mode;
                if(draw){
                    draw = false;
                    repaint();
                }
            }
        }  else {
            if(!draw){
                draw = true;
                repaint();
            }
        }
    }

    private void repaint() {
        SP.getWM().getPanelManager(this).repaint();
    }

    private void repaintAll() {
        SP.getWM().repaintAll();
    }
}
