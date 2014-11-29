package gs.soni.plane.project;

import gs.soni.plane.util.file;
import gs.soni.plane.v;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;

public class palette {
    private static Class c;
    private static Color[][] palette;

    public static void setPalette(int line, Color[] pal){
        palette[line] = pal;
    }

    public static void setPalette(int line, int offset, Color pal){
        palette[line][offset] = pal;
    }

    public static void setPalette(Color[][] pal){
        palette = pal;
    }

    public static Color getPalette(int line, int off){
        if(palette[line][off] != null) {
            return palette[line][off];
        }

        return new Color(0);
    }

    public static Color[][] getPalette(){
        return palette;
    }

    public static Class LoadPaletteModule(String JarAbsolute, String Class)
            throws IOException, ClassNotFoundException {

        if(!new File(JarAbsolute).exists()){
            throw new FileNotFoundException(JarAbsolute);
        }

        if(!file.isJar(JarAbsolute)){
            throw new IOException(JarAbsolute +" is not JAR!");
        }

        c = new URLClassLoader( new URL[]{ new File(JarAbsolute).toURI().toURL() } ).loadClass(Class);
        return c;
    }

    public static Class GetPaletteModule(){
        return c;
    }

    public static void UnLoadPaletteModule() {
        c = null;
    }

    public static int ColorToInt(int c) {
        try {
            return (Integer) GetPaletteModule().getMethod("convert", int.class).invoke(GetPaletteModule().newInstance(), c);

        } catch (Exception e) {
            e.printStackTrace();

        }

        return -1;
    }

    public static int GetGrid(int color) {
        try {
            return (Integer) GetPaletteModule().getMethod("grid", int.class).invoke(GetPaletteModule().newInstance(), color);

        } catch (Exception e) {
            e.printStackTrace();

        }

        return 0;
    }

    public static void LoadPalette() {
        String[] DescriptionFile;
        String adr = v.LaunchAdr +"/modules/palette/";

        try {
            DescriptionFile = new String(file.readFile(adr +
                    project.GetField("palette type", new String(file.readFile(v.project)).split("\n")) +".txt")).split("\n");

        } catch (NoSuchFieldError e){
            e.printStackTrace();
            return;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        try {
            palette = ((Color[][]) LoadPaletteModule(adr + project.GetField("jar", DescriptionFile),
                    project.GetField("class", DescriptionFile)).getMethod("load", String.class).
                    invoke(GetPaletteModule().newInstance(),
                            project.GetField("palette file", new String(file.readFile(v.project)).split("\n"))));

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("ClassNotFoundException: "+ adr + project.GetField("jar", DescriptionFile) +":"+
                    project.GetField("class", DescriptionFile) +" not found!");

        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public static boolean Save(String file) {
        try {
            gs.soni.plane.util.file.saveFile(file, (byte[]) c.getMethod("save", Color[][].class).
                    invoke(c.newInstance(), (Object) palette));
            return true;

        } catch (Exception e) {
            e.printStackTrace();

        }
        return false;
    }
}
