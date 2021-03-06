package gs.soni.plane.project;

import gs.app.lib.math.bounds;
import gs.soni.plane.SP;
import gs.soni.plane.draw.PanelManager;
import gs.soni.plane.draw.plane;
import gs.soni.plane.util.file;
import gs.soni.plane.v;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

public class mappings {

    private static Class c;
    private static map[] m;

    public static bounds GetSize() {
        try {
            return new bounds(Integer.parseInt(project.GetField("map width", new String(file.readFile(v.project)).split("\n"))),
                    Integer.parseInt(project.GetField("map height", new String(file.readFile(v.project)).split("\n"))), 0, 0);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Class LoadMapModule(String JarAbsolute, String Class)
            throws IOException, ClassNotFoundException {

        if (!new File(JarAbsolute).exists()) {
            throw new FileNotFoundException(JarAbsolute);
        }

        if (!file.isJar(JarAbsolute)) {
            throw new IOException(JarAbsolute + " is not JAR!");
        }

        c = new URLClassLoader(new URL[]{new File(JarAbsolute).toURI().toURL()}).loadClass(Class);
        return c;
    }

    public static Class GetMapModule() {
        return c;
    }

    public static void UnLoadMapModule() {
        c = null;
    }

    public static void SetMapArray(map[] data) {
        m = data;
    }

    public static map[] GetMapArray() {
        return m;
    }

    public static map[] GetMap() {
        String[] DescriptionFile;
        String adr = v.LaunchAdr +"/modules/map/";

        try {
            DescriptionFile = new String(file.readFile(adr +
                    project.GetField("map type", new String(file.readFile(v.project)).split("\n")) +".txt")).split("\n");

        } catch (NoSuchFieldError e) {
            e.printStackTrace();
            return null;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        try {
            return ((map[]) LoadMapModule(adr + project.GetField("jar", DescriptionFile),
                    project.GetField("class", DescriptionFile)).getMethod("load", String.class).
                    invoke(GetMapModule().newInstance(), project.DoCMD(
                            project.GetField("map file", new String(file.readFile(v.project)).split("\n")),
                            project.GetField("map compression", new String(file.readFile(v.project)).split("\n")) + ".txt",
                            "decompress", v.LaunchAdr + "/temp/map")));

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("ClassNotFoundException: "+ adr + project.GetField("jar", DescriptionFile) +":"+
                    project.GetField("class", DescriptionFile) +" not found!");

        } catch (Exception e) {
            e.printStackTrace();

        }

        return null;
    }

    public static boolean Save(String file) {
        try {
            gs.soni.plane.util.file.saveFile(v.LaunchAdr + "/temp/map", (byte[]) c.getMethod("save", map[].class, int.class).
                    invoke(c.newInstance(), m, Integer.parseInt(project.GetField("map offset",
                            new String(gs.soni.plane.util.file.readFile(v.project)).split("\n")))));

            project.DoCMD(v.LaunchAdr + "\\temp\\map", project.GetField("map compression",
                    new String(gs.soni.plane.util.file.readFile(v.project)).split("\n")) + ".txt", "compress", file);
            return true;

        } catch (Exception e) {
            e.printStackTrace();

        }

        return false;
    }

    public static void SetMapOffset(int off) {
        for (map map : m) {
            map.tileOff += off;
        }
    }

    public static void SetMap(map map, int off) {
        m[off] = map;
    }

    public static void ShiftMap(int x, int y, bounds b) {
        int move = x + (y * v.mapSize.x), w = tileLoader.GetWidth(), h = tileLoader.GetHeight();
        if (move < 0) {

            for (int o = 0; o < m.length; o++) {
                int x_ = o % v.mapSize.x, y_ = o / v.mapSize.x;
                if (isInside(new bounds((x_ * w), (y_ * h), 0, 0), b) && o + move >= 0) {
                    m[o + move] = new map(m[o]);
                }
            }

        } else {
            for (int o = m.length - 1; o > 0; o--) {
                int x_ = o % v.mapSize.x, y_ = o / v.mapSize.x;
                if (isInside(new bounds((x_ * w), (y_ * h), 0, 0), b) && o + move < m.length) {
                    m[o + move] = new map(m[o]);
                }
            }
        }
    }

    public static void Adjust() {
        int size = v.mapSize.y * v.mapSize.x;

        if (size > m.length) {
            AddMap(m.length, size - m.length);

        } else if (size < m.length) {
            for (int i = m.length - 1; i > size; i--) {

                if (!m[i].isDefault()) {
                    if (i + 1 <= m.length) {
                        RmvMap(i + 1, m.length - i - 1);
                    }
                    return;
                }
            }
            RmvMap(size, m.length - size);
        }
    }

    private static void RmvMap(int off, int len) {
        map[] temp = m;
        m = new map[temp.length - len];

        System.arraycopy(temp, 0, m, 0, off);
        System.arraycopy(temp, off + len, m, off, temp.length - off - len);
    }

    private static void AddMap(int off, int len) {
        map[] temp = m;
        m = new map[temp.length + len];

        System.arraycopy(temp, 0, m, 0, off);
        System.arraycopy(temp, off, m, off + len, m.length - off - len);

        for (int i = 0; i < m.length; i++) {
            if (m[i] == null) {
                m[i] = new map();
            }
        }
    }

    public static void Delete(bounds b) {
        int w = tileLoader.GetWidth(), h = tileLoader.GetHeight();
        for (int o = 0; o < m.length; o++) {

            int x_ = o % v.mapSize.x, y_ = o / v.mapSize.x;
            if (isInside(new bounds((x_ * w), (y_ * h), 0, 0), b)) {
                m[o] = new map();
            }
        }
    }

    public static void Delete(bounds b, bounds no) {
        int w = tileLoader.GetWidth(), h = tileLoader.GetHeight();
        for (int o = 0; o < m.length; o++) {

            int x_ = o % v.mapSize.x, y_ = o / v.mapSize.x;
            if (isInside(new bounds((x_ * w), (y_ * h), 0, 0), b) && !isInside(new bounds((x_ * w), (y_ * h), 0, 0), no)) {
                m[o] = new map();
            }
        }
    }

    public static void TileIndex(int add, bounds sel) {
        int w = tileLoader.GetWidth(), h = tileLoader.GetHeight();
        for (int o = 0; o < m.length; o++) {

            int x_ = o % v.mapSize.x, y_ = o / v.mapSize.x;
            if (isInside(new bounds((x_ * w), (y_ * h), 0, 0), sel)) {
                m[o].tileOff += add;
            }
        }
    }

    public static void TileFlip(boolean Xflip, boolean Yflip, bounds sel) {
        int w = tileLoader.GetWidth(), h = tileLoader.GetHeight();
        for (int o = 0; o < m.length; o++) {

            int x_ = o % v.mapSize.x, y_ = o / v.mapSize.x;
            if (isInside(new bounds((x_ * w), (y_ * h), 0, 0), sel)) {
                m[o].XFlip ^= Xflip;
                m[o].YFlip ^= Yflip;
            }
        }
    }

    public static void SelFlip(boolean Xflip, boolean Yflip) {

    }

    public static void PalIndex(int add, bounds sel) {
        int w = tileLoader.GetWidth(), h = tileLoader.GetHeight();
        for (int o = 0; o < m.length; o++) {
            int x_ = o % v.mapSize.x, y_ = o / v.mapSize.x;
            if (isInside(new bounds((x_ * w), (y_ * h), 0, 0), sel)) {

                m[o].palLine = (m[o].palLine + add) % palette.getPalette().length;
                if(m[o].palLine - v.LineOff < 0){
                    m[o].palLine += palette.getPalette().length;
                }
            }
        }
    }

    public static void ChangePlane(bounds sel) {
        int w = tileLoader.GetWidth(), h = tileLoader.GetHeight();
        for (int o = 0; o < m.length; o++) {
            int x_ = o % v.mapSize.x, y_ = o / v.mapSize.x;
            if (isInside(new bounds((x_ * w), (y_ * h), 0, 0), sel)) {
                m[o].HighPlane ^= true;
            }
        }
    }

    public static void Remove(bounds sel) {
        int w = tileLoader.GetWidth(), h = tileLoader.GetHeight(), apos = 0;
        int[] rmv = new int[m.length];
        Arrays.fill(rmv, -1);
        for (int o = 0; o < m.length; o++) {

            int x_ = o % v.mapSize.x, y_ = o / v.mapSize.x;
            if (isInside(new bounds((x_ * w), (y_ * h), 0, 0), sel)) {
                rmv[apos] = o;
                apos++;
            }
        }

        for (int o = 0; o < rmv.length; o++) {
            if (rmv[o] != -1) {
                RmvMap(rmv[o] - o, 1);
            }
        }

        Adjust();
    }

    public static void Insert(bounds sel) {
        int w = tileLoader.GetWidth(), h = tileLoader.GetHeight(), apos = 0;
        boolean[] ins = new boolean[m.length];
        Arrays.fill(ins, false);
        for (int o = 0; o < m.length; o++) {

            int x_ = o % v.mapSize.x, y_ = o / v.mapSize.x;
            if (isInside(new bounds((x_ * w), (y_ * h), 0, 0), sel)) {
                ins[o] = true;
            }
        }

        for (int o = 0, off = 0, len = 0; o < ins.length; o++) {
            if (ins[o]) {
                AddMap(o + off, 1);
                len++;

            } else {
                off = len;
            }
        }
    }

    public static void Fill(int tile, int inc, bounds sel) {
        int w = tileLoader.GetWidth(), h = tileLoader.GetHeight();
        for (int o = 0; o < m.length; o++) {
            int x_ = o % v.mapSize.x, y_ = o / v.mapSize.x;

            if (isInside(new bounds((x_ * w), (y_ * h), 0, 0), sel)) {
                m[o].tileOff = tile;
                tile += inc;
            }
        }
    }

    public static void Optimize() {
        int size = v.mapSize.y * v.mapSize.x;
        RmvMap(size, m.length - size);
    }

    public static boolean isInside(bounds b, bounds cmpBounds) {
        return (b.x >= cmpBounds.x) && (b.w + b.x < cmpBounds.w + cmpBounds.x) &&
                (b.y >= cmpBounds.y) && (b.h + b.y < cmpBounds.h + cmpBounds.y);
    }
}

