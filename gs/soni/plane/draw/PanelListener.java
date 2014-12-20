package gs.soni.plane.draw;

public interface PanelListener {
    void close();
    void draw();
    void logic(boolean focus);
    void resize(int width, int height);
    void move(int x, int y);
}
