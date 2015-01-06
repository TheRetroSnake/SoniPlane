package gs.soni.plane.util;

import gs.soni.plane.project.project;
import gs.soni.plane.v;

import java.awt.*;
import java.io.FileNotFoundException;

public class Colors {
    public static float multiplier = 0.00392156863f;

    public static Color GetColor(String ID, float alpha) {
        try {
            String[] d = new String(file.readFile(v.LaunchAdr + "/styles/colors/" + ID + ".txt")).replace("\r", "").split("\n");

            return new Color(
                    Integer.parseInt(project.GetField("r", d)) * multiplier,
                    Integer.parseInt(project.GetField("g", d)) * multiplier,
                    Integer.parseInt(project.GetField("b", d)) * multiplier,
                    alpha);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Color GetColor(String ID) {
        try {
            String[] d = new String(file.readFile(v.LaunchAdr + "/styles/colors/" + ID + ".txt")).replace("\r", "").split("\n");

            return new Color(
                    Integer.parseInt(project.GetField("r", d)) * multiplier,
                    Integer.parseInt(project.GetField("g", d)) * multiplier,
                    Integer.parseInt(project.GetField("b", d)) * multiplier,
                    Integer.parseInt(project.GetField("a", d)) * multiplier);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }
}
