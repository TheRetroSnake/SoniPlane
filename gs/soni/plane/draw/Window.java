package gs.soni.plane.draw;

import gs.app.lib.gfx.Graphics;
import gs.app.lib.math.bounds;

public interface Window {
    bounds getBounds();
    void draw(Graphics g);
    void logic();
    void create();
    boolean canUnFocus();
    boolean drawBound();
}
