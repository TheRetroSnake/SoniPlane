package gs.soni.plane.util;

import gs.app.lib.math.bounds;
import gs.app.lib.util.MouseUtil;
import gs.soni.plane.SP;
import gs.soni.plane.v;

public class Mouse {

    public static boolean IsInArea(int xr, int yu, int xl, int yd) {
        return IsInArea(new bounds(yu, yd, xr, xl), false);
    }

    public static boolean IsInArea(bounds bounds) {
        return IsInArea(bounds, false);
    }

    public static boolean IsInArea(bounds bounds, boolean Priority) {
        bounds c = new bounds(MouseUtil.getX(), MouseUtil.getY(), 0, 0);

        return (!v.BlockControls || Priority) && (c.x > bounds.x) && (c.x < bounds.w) && (c.y > bounds.y) && (c.y < bounds.h);
    }

    public static boolean IsClicked(int button) {
        return IsClicked(button, false);
    }

    public static boolean IsClicked(int button, boolean Priority) {
        return !v.IsClicked && MouseUtil.isPressed(button) && (!v.BlockControls || Priority);
    }

    public static boolean IsClicked() {
        return IsClicked(false);
    }

    public static boolean IsClicked(boolean Priority) {
        if((!v.BlockControls || Priority)) {
            for (int i = 0; i < MouseUtil.MIDDLE + 1; i++) {
                if(MouseUtil.isPressed(i)){
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean IsHeld(int button) {
        return IsHeld(button, false);
    }

    public static boolean IsHeld(int button, boolean Priority) {
        return MouseUtil.isHeld(button) && (!v.BlockControls || Priority);
    }

    public static bounds GetPos(){
        return GetPos(false);
    }

    public static bounds GetPos(boolean Priority) {
        if(!v.BlockControls || Priority) {
            bounds c = new bounds(MouseUtil.getX(), MouseUtil.getY(), 0, 0);

            return new bounds(c.x, c.y, 0, 0);
        }

        return new bounds(-100, -100, 0, 0);
    }
}
