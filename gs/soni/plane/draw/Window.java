package gs.soni.plane.draw;

import gs.app.lib.gfx.Graphics;
import gs.app.lib.math.bounds;

public interface Window {
    bounds getBounds();
    void draw(Graphics g, bounds b, float a);
    void logic();
    void create();
    boolean canUnFocus();
    boolean drawBound();
    boolean cursorOverride();
    void defaultSize();
    void resize(int width, int height);
    void move(int x, int y);
}
