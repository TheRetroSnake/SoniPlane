package gs.soni.plane.draw;

import gs.app.lib.gfx.Graphics;
import gs.app.lib.gfx.Sprite;
import gs.app.lib.math.bounds;
import gs.app.lib.util.MouseUtil;
import gs.soni.plane.SP;
import gs.soni.plane.project.palette;
import gs.soni.plane.project.tileLoader;
import gs.soni.plane.util.Mouse;
import gs.soni.plane.util.Style;
import gs.soni.plane.util.StyleItem;
import gs.soni.plane.v;

import java.awt.*;

public class palChange implements Window {
    private final int Size = 16;
    private bounds bound;
    private int sel = -1;

    public palChange(){
        bound = new bounds(windowManager.defaultBounds());
    }

    @Override
    public bounds getBounds() {
        return bound;
    }

    @Override
    public void draw(Graphics g, bounds b, float a) {
        if(((bound.w / 5) * 4) - 50 > 0 && bound.y + bound.h > 0) {
            Sprite s = new Sprite();
            StyleItem st = Style.GetStyle("menu_center");

            // render big palette and red border next to it
            s.setBounds(b.x, b.y, bound.w / 5, bound.h);
            s.setColor(palette.getPalette(v.PalLine, v.PalSelcted));
            g.fillRect(s);
            s.setBounds(b.x + (bound.w / 5), b.y, 2, bound.h);
            s.setColor(Color.RED);
            g.fillRect(s);

            // draw drag-thingies
            s.setBounds(b.x + (bound.w / 5) + 52 - (Size / 2), b.y + (bound.h / 4) - (Size / 2),
                    ((bound.w / 5) * 4) - 50 + (Size / 2), Size);
            g.fillRect(s);
            s.setBounds(b.x + (bound.w / 5) + 52 - (Size / 2), b.y + ((bound.h / 4) * 2) - (Size / 2),
                    ((bound.w / 5) * 4) - 50 + (Size / 2), Size);
            s.setColor(Color.GREEN);
            g.fillRect(s);
            s.setBounds(b.x + (bound.w / 5) + 52 - (Size / 2), b.y + ((bound.h / 4) * 3) - (Size / 2),
                    ((bound.w / 5) * 4) - 50 + (Size / 2), Size);
            s.setColor(Color.BLUE);
            g.fillRect(s);


            // draw palette values
            Graphics.setFont(st.GetFont());
            g.setColor(st.GetColor());
            int x = b.x + (bound.w / 5) + 8;

            String tx = Integer.toHexString(palette.ColorToInt(palette.getPalette(v.PalLine, v.PalSelcted).getRed())).toUpperCase();
            int off1 = b.y + (int) ((bound.h / 4) - (Graphics.GetTextHeight(tx) / 2));
            g.drawText(tx, x, off1);

            tx = Integer.toHexString(palette.ColorToInt(palette.getPalette(v.PalLine, v.PalSelcted).getGreen())).toUpperCase();
            int off2 = b.y + (int) (((bound.h / 4) * 2) - (Graphics.GetTextHeight(tx) / 2));
            g.drawText(tx, x, off2);

            tx = Integer.toHexString(palette.ColorToInt(palette.getPalette(v.PalLine, v.PalSelcted).getBlue())).toUpperCase();
            int off3 = b.y + (int) (((bound.h / 4) * 3) - (Graphics.GetTextHeight(tx) / 2));
            g.drawText(tx, x, off3);

            x = (bound.w / 5) + 58 - (Size / 2);
            PalSelector(new bounds(x, off1 + 2, ((bound.w / 5) * 4) - 54, Size + 2), b,
                    palette.getPalette(v.PalLine, v.PalSelcted).getRed(), g);

            PalSelector(new bounds(x, off2 + 2, ((bound.w / 5) * 4) - 54, Size + 2), b,
                    palette.getPalette(v.PalLine, v.PalSelcted).getGreen(), g);

            PalSelector(new bounds(x, off3 + 2, ((bound.w / 5) * 4) - 54, Size + 2), b,
                    palette.getPalette(v.PalLine, v.PalSelcted).getBlue(), g);
        }
    }

    private void PalSelector(bounds bounds, bounds b, int color, Graphics g) {
        if(Mouse.IsInArea(new bounds(bound.x + bounds.x - (b.x * 2), bound.y + bounds.y - b.y,
                bound.x + bounds.x + bounds.w + b.x, bound.y + bounds.y + bounds.h - b.y))){
            if(Mouse.IsHeld(MouseUtil.LEFT) || Mouse.IsHeld(MouseUtil.RIGHT)){
                g.setColor(new Color(155, 155, 155));

            } else {
                g.setColor(new Color(205, 205, 205));
            }
        } else {
            g.setColor(Color.WHITE);
        }

        float pos = (((float)palette.GetGrid(color) / 256) * bounds.w) - 4;
        g.fillRect((int) (bounds.x + pos), bounds.y, Size, bounds.h);
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

    @Override
    public boolean cursorOverride() {
        return false;
    }

    @Override
    public void defaultSize() {
        bound.w = 256 + 58 - (Size / 2);
        bound.h = Size * 6;
    }

    @Override
    public void resize(int width, int height) {
        bound.w = width;
        bound.h = height;

        if(bound.h < Size * 6){
            bound.h = Size * 6;
        }
    }

    @Override
    public void move(int x, int y) {

    }

    private void CheckDraw(bounds bounds, int mode) {
        boolean draw = false;

        if (Mouse.IsInArea(new bounds(bounds.x, bounds.y, bounds.x + bounds.w, bounds.y + bounds.h)) && (canUnFocus() || sel == mode)) {
            if (Mouse.IsHeld(MouseUtil.LEFT) || Mouse.IsHeld(MouseUtil.RIGHT)) {
                Color c = palette.getPalette(v.PalLine, v.PalSelcted);
                int pal = palette.GetGrid((int) (((float) Mouse.GetPos().x - bounds.x) / bounds.w * 256));

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
                tileLoader.startTileRender();

            } else {
                sel = mode;
                draw = true;
            }
        } else {
            draw = true;
        }

        if(draw){
            repaint();
        }
    }

    private void repaint() {
        SP.getWM().getPanelManager(this).repaint();
    }

    private void repaintAll() {
        SP.getWM().repaintAll();
    }
}
