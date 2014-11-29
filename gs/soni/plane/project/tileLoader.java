package gs.soni.plane.project;

import gs.soni.plane.util.file;
import gs.soni.plane.v;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

public class tileLoader {

    private static Class c;
    private static int[][] tiles;
    private static BufferedImage[][] tex;
    private static BufferedImage trans;

    public static Class LoadTileModule(String JarAbsolute, String Class) throws IOException, ClassNotFoundException {

        if(!new File(JarAbsolute).exists()){
            throw new FileNotFoundException(JarAbsolute);
        }

        if(!file.isJar(JarAbsolute)){
            throw new IOException(JarAbsolute +" is not JAR!");
        }

        c = new URLClassLoader( new URL[]{ new File(JarAbsolute).toURI().toURL() } ).loadClass(Class);
        return c;
    }

    public static Class GetTileModule(){
        return c;
    }

    public static void UnLoadTileModule() {
        c = null;
    }

    public static void SetTileArray(int[][] data) {
        tiles = data;
    }

    public static int[][] GetTileArray() {
        return tiles;
    }

    public static int[][] GetTiles(){
        String[] DescriptionFile;
        String adr = v.LaunchAdr +"/modules/tile/";

        try {
            DescriptionFile = new String(file.readFile(adr +
                    project.GetField("art type", new String(file.readFile(v.project)).split("\n")) +".txt")).split("\n");

        } catch (NoSuchFieldError e){
            e.printStackTrace();
            return null;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        try {
            return ((int[][]) LoadTileModule(adr + project.GetField("jar", DescriptionFile),
                    project.GetField("class", DescriptionFile)).getMethod("load", String.class).invoke(GetTileModule().newInstance(),
                    project.DoCMD(project.GetField("art file", new String(file.readFile(v.project)).split("\n")),
                    project.GetField("art compression", new String(file.readFile(v.project)).split("\n")) + ".txt",
                    "decompress", v.LaunchAdr + "/temp/art")));

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("ClassNotFoundException: "+ adr + project.GetField("jar", DescriptionFile) +":"+
                    project.GetField("class", DescriptionFile) +" not found!");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static int GetWidth() {
        try {
            return (Integer) GetTileModule().getMethod("GetWidth").invoke(GetTileModule().newInstance());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public static int GetHeight() {
        try {
            return (Integer) GetTileModule().getMethod("GetHeight").invoke(GetTileModule().newInstance());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public static void render() {
        tex = new BufferedImage[palette.getPalette().length][tiles.length];
        renderTrans();

        for (int l = 0;l < palette.getPalette().length;l ++) {
            renderLine(l);
        }
    }

    public static void render(int tile) {
        int w = tileLoader.GetWidth(), h = tileLoader.GetHeight(), pl, po;

        try {
            pl = Integer.parseInt(project.GetField("trans line", new String(file.readFile(v.project)).split("\n")));
            po = Integer.parseInt(project.GetField("trans off", new String(file.readFile(v.project)).split("\n")));

        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        for (int l = 0;l < palette.getPalette().length;l ++) {
            tex[l][tile] = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

            for (int y = 0, o = 0; y < w; y++) {
                for (int x = 0; x < h; x++, o++) {
                    if (tileLoader.GetTileArray()[tile][o] != 0) {
                        tex[l][tile].setRGB(x, y, palette.getPalette(l, tileLoader.GetTileArray()[tile][o]).getRGB());
                    } else {
                        tex[l][tile].setRGB(x, y, palette.getPalette(pl, po).getRGB());
                    }
                }
            }
        }
    }

    public static BufferedImage GetTexture(int palLine, int tileOff) {
        return tex[palLine][tileOff];
    }

    public static int GetTextureAmount(int palLine) {
        if(tex != null && tex[palLine] != null) {
            return tex[palLine].length;
        }
        return 0;
    }

    public static BufferedImage GetTrans() {
        return trans;
    }

    public static int[] GetTile(int off) {
        return tiles[off];
    }

    public static void SetTile(int[] tile, int off) {
        tiles[off] = tile;
    }

    public static void dispose(){
        tex = null;
        trans = null;
    }

    public static void renderLine(int line) {
        int w = tileLoader.GetWidth(), h = tileLoader.GetHeight(), pl, po;

        try {
            pl = Integer.parseInt(project.GetField("trans line", new String(file.readFile(v.project)).split("\n")));
            po = Integer.parseInt(project.GetField("trans off", new String(file.readFile(v.project)).split("\n")));

        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        for (int i = 0; i < tiles.length;i ++) {
            tex[line][i] = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

            for (int y = 0, o = 0; y < w; y++) {
                for (int x = 0; x < h; x++, o++) {

                    if (tileLoader.GetTileArray()[i][o] != 0) {
                        tex[line][i].setRGB(x, y, palette.getPalette(line, tileLoader.GetTileArray()[i][o]).getRGB());
                    } else {
                        tex[line][i].setRGB(x, y, palette.getPalette(pl, po).getRGB());
                    }
                }
            }
        }
    }

    public static void renderTrans() {
        int w = tileLoader.GetWidth(), h = tileLoader.GetHeight(), pl, po;

        try {
            pl = Integer.parseInt(project.GetField("trans line", new String(file.readFile(v.project)).split("\n")));
            po = Integer.parseInt(project.GetField("trans off", new String(file.readFile(v.project)).split("\n")));

        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        trans = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        // full Solid Color
        for (int y = 0, o = 0; y < w; y++) {
            for (int x = 0; x < h; x++, o++) {
                trans.setRGB(x, y, palette.getPalette(pl, po).getRGB());
            }
        }
    }

    public static boolean Save(String file) {
        try {
            gs.soni.plane.util.file.saveFile(v.LaunchAdr +"/temp/art", (byte[]) c.getMethod("save", int[][].class).
                    invoke(c.newInstance(), (Object) tiles));
            project.DoCMD(v.LaunchAdr + "/temp/art", project.GetField("art compression",
                    new String(gs.soni.plane.util.file.readFile(v.project)).split("\n")) + ".txt", "compress", file);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}
