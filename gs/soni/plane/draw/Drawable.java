package gs.soni.plane.draw;

import gs.app.lib.gfx.Graphics;

public interface Drawable {

    public void draw(Graphics g);

    public int renderPriority();
}
