package gs.soni.plane.util;

import gs.app.lib.gfx.gfx;
import gs.soni.plane.project.project;
import gs.soni.plane.v;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;

public class Style {
    private static StyleItem[] styles = GetStyles();

    public static StyleItem GetStyle(String styleID) {
        for(StyleItem s : styles){
            if(s.GetID().equals(styleID)){
                return s;
            }
        }
        throw new NullPointerException("Style \""+ styleID +"\" not found!");
    }

    private static StyleItem GetStyleData(String name){
        try {
            if(new File(v.LaunchAdr +"/styles/font/"+ name +".txt").exists()) {
                String[] data = new String(file.readFile(v.LaunchAdr + "/styles/font/" + name + ".txt")).replace("\r", "").split("\n");

                return new StyleItem(name, gfx.getFont(v.LaunchAdr +"/styles/font/"+ project.GetField("font", data),
                        TransferStyle(project.GetField("style", data)), Integer.parseInt(project.GetField("size", data))),
                        new Color (
                                Integer.parseInt(project.GetField("color_r", data)),
                                Integer.parseInt(project.GetField("color_g", data)),
                                Integer.parseInt(project.GetField("color_b", data)),
                                Integer.parseInt(project.GetField("color_a", data))
                        ), GetAlign(project.GetField("alignment", data)));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static int TransferStyle(String style) {
        int ret = 0;
        if(style.contains("BOLD")){
            ret += Font.BOLD;
        }

        if(style.contains("ITALICS")){
            ret += Font.ITALIC;
        }

        return ret;
    }

    private static int GetAlign(String align) {
        int ret = 0;

        if(align.equalsIgnoreCase("CENTER")){
            return ret + 1;
        } else if(align.equalsIgnoreCase("RIGHT")){
            return ret + 2;
        }
        return ret;
    }

    private static StyleItem[] GetStyles() {
        String[] d = file.GetFileList_(v.LaunchAdr +"/styles/font/", "txt");
        StyleItem[] r = new StyleItem[d.length];

        for(int i = 0;i < d.length;i ++){
            r[i] = GetStyleData(d[i].replace(v.LaunchAdr +"/styles/font/", "").replace(".txt", ""));
        }

        return r;
    }
}
