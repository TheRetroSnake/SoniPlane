package gs.soni.plane.util;

import gs.soni.plane.v;

import java.awt.*;

public class CursorList {
    public static final int BUSY_CURSOR =  0x10;
    public static final int WAIT_CURSOR =  0x11;
    public static final int GRAB_CURSOR =  0x12;
    public static final int GRAB2_CURSOR = 0x13;

    private static Cursor customCursor(String file, String name, Point p) {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        return toolkit.createCustomCursor(toolkit.getImage(file), p, name);
    }

    public static Cursor get(int type) {
        switch (type){
            case Cursor.DEFAULT_CURSOR:
                return customCursor(v.LaunchAdr +"/res/cursor/default.png", "default", new Point(0, 0));

            case Cursor.HAND_CURSOR:
                return customCursor(v.LaunchAdr +"/res/cursor/link.png", "link", new Point(6, 2));

            case Cursor.N_RESIZE_CURSOR: case Cursor.S_RESIZE_CURSOR:
                return customCursor(v.LaunchAdr +"/res/cursor/size4.png", "size4", new Point(16, 16));

            case Cursor.E_RESIZE_CURSOR: case Cursor.W_RESIZE_CURSOR:
                return customCursor(v.LaunchAdr +"/res/cursor/size3.png", "size3", new Point(16, 16));

            case Cursor.SE_RESIZE_CURSOR: case Cursor.NW_RESIZE_CURSOR:
                return customCursor(v.LaunchAdr +"/res/cursor/size2.png", "size2", new Point(16, 16));

            case Cursor.NE_RESIZE_CURSOR: case Cursor.SW_RESIZE_CURSOR:
                return customCursor(v.LaunchAdr +"/res/cursor/size1.png", "size1", new Point(16, 16));

            case BUSY_CURSOR:
                return customCursor(v.LaunchAdr +"/res/cursor/busy.png", "busy", new Point(16, 16));

            case WAIT_CURSOR:
                return customCursor(v.LaunchAdr +"/res/cursor/wait.png", "wait", new Point(0, 0));

            case GRAB_CURSOR:
                return customCursor(v.LaunchAdr +"/res/cursor/grab.png", "grab", new Point(16, 16));

            case GRAB2_CURSOR:
                return customCursor(v.LaunchAdr +"/res/cursor/grab2.png", "grab2", new Point(16, 16));
        }

        throw new NullPointerException("Cursor type "+ type +" not defined!");
    }

    public static Cursor get(String name, Point p) {
        return customCursor(v.LaunchAdr +"/res/cursor/"+ name +".png", name, p);
    }
}
