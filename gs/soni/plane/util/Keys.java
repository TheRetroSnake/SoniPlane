package gs.soni.plane.util;

import gs.app.lib.util.KeyUtil;
import gs.soni.plane.v;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class Keys {

    public static boolean isPressed(int Key){
        return isPressed(Key, false);
    }

    public static boolean isPressed(int Key, boolean Priority){
        return KeyUtil.isPressed(Key) && (!v.BlockControls || Priority);
    }

    public static String GetNextPress(boolean Priority) {
        if (!v.BlockControls || Priority) {
            if (isHeld(KeyUtil.CONTROL, true) && isPressed(KeyUtil.V, true)) {
                return GetClipBoard();

            } else if(isHeld(KeyUtil.CONTROL, true) && isPressed(KeyUtil.N, true)) {
                return "-";
            }

            for (int i = KeyUtil.COMMA;i <= KeyUtil.EQUALS;i ++) {
                if (isPressed(i, true)) {
                    return KeyUtil.toShortString(i);
                }
            }

            for (int i = KeyUtil.A; i <= KeyUtil.Z; i++) {
                if (isPressed(i, true)) {

                    if(isHeld(KeyUtil.SHIFT, true)) {
                        return KeyUtil.toString(i).toUpperCase();
                    } else {
                        return KeyUtil.toString(i).toLowerCase();
                    }
                }
            }

            for (int i = KeyUtil.OPEN_BRACKET;i <= KeyUtil.DIVIDE;i ++) {
                if (isPressed(i, true)) {
                    return KeyUtil.toShortString(i);
                }
            }

            for (int i = KeyUtil.AT;i <= KeyUtil.UNDERSCORE;i ++) {
                if (isPressed(i, true)) {
                    return KeyUtil.toShortString(i);
                }
            }

            if (isPressed(KeyUtil.SPACE, true)) {
                return " ";

            }
        }

        return "";
    }

    private static String GetClipBoard() {
        try {
            return (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
        } catch (UnsupportedFlavorException e) {
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    public static boolean isHeld(int Key){
        return isHeld(Key, false);
    }

    public static boolean isHeld(int Key, boolean Priority){
        return KeyUtil.isHeld(Key) && (!v.BlockControls || Priority);
    }

}
