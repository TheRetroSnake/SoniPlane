package gs.soni.plane.util;

import gs.app.lib.gfx.Graphics;
import gs.app.lib.util.FileUtil;
import gs.soni.plane.menu.menu;
import gs.soni.plane.project.project;
import gs.soni.plane.v;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.ZipFile;

public class file {

    public static String[] GetFileList(String folder, String extension) {
        File[] files = new File(folder).listFiles();
        String o = "";

        if (files == null) {
            return new String[]{ "" };
        }

        for(File f : files){
            try {
                if(f.getAbsolutePath().endsWith("."+ extension) &&
                        project.isProject(new String(file.readFile(f.getAbsolutePath())).split("\n"))){
                    o += f.getAbsolutePath().replace("\\", "/") +"<";
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return o.split("<");
    }

    public static String[] GetFileList_(String folder, String extension) {
        File[] files = new File(folder).listFiles();
        String o = "";

        if (files == null) {
            return new String[]{ "" };
        }

        for(File f : files) {
            if (f.getAbsolutePath().endsWith("." + extension)) {
                o += f.getAbsolutePath().replace("\\", "/") + "<";
            }
        }
        return o.split("<");
    }

    public static String[] GetFileList(String folder, String extension, String field, String value) {
        File[] files = new File(folder).listFiles();
        String o = "";

        if (files == null) {
            return new String[]{ "" };
        }

        for (File f : files) {
            try {
                if (f.getAbsolutePath().endsWith("." + extension) &&
                        project.isProject(new String(file.readFile(f.getAbsolutePath())).split("\n")) &&
                        project.GetField(field, new String(file.readFile(f.getAbsolutePath())).split("\n")).equals(value)) {
                    o += f.getAbsolutePath().replace("\\", "/") + "<";
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        return o.split("<");
    }

    public static String[] GetFileList_(String folder, String extension, String field, String value) {
        File[] files = new File(folder).listFiles();
        String o = "";

        if (files == null) {
            return new String[]{ "" };
        }

        for (File f : files) {
            try {
                if (f.getAbsolutePath().endsWith("." + extension) &&
                        project.GetField(field, new String(file.readFile(f.getAbsolutePath())).split("\n")).equals(value)) {
                    o += f.getAbsolutePath().replace("\\", "/") + "<";
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        return o.split("<");
    }

    public static menu FormMenu(String[] fileNames, StyleItem style, float alpha, EventHandler[] event,
                                SortType s, boolean b, int btnStyle) {
        menu out = new menu();
        Graphics.setFont(style.GetFont());

        for(String f : fileNames){
            EventHandler[] t = new EventHandler[event.length];
            try {
                for (int i = 0; i < event.length; i++) {
                    t[i] = (EventHandler) event[i].clone();
                    t[i].setString(f.replace("\\", "/"));
                }

                try {
                    if (b) {
                        out = out.AddProjMenu(f.replace("\\", "/"), f.replace("\\", "/").replace(v.LaunchAdr + "/autosave/", ""),
                                0, 0, menu.GetScreenSize(), menu.GetStyleSize(), style, alpha, t, btnStyle);

                    } else {
                        out = out.AddProjMenu(f.replace("\\", "/"), project.GetField("name", new String(file.readFile(f)).
                                split("\n")), 0, 0, menu.GetScreenSize(), menu.GetStyleSize(), style, alpha, t, btnStyle);
                    }

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }

        try {
            return out.sort(s);
        } catch (menu.SortTypeException e) {
            e.printStackTrace();
        }

        return out;
    }

    public static byte[] readFile(String filename) throws FileNotFoundException {
        if(FileUtil.exists(filename) && !FileUtil.isDir(filename)) {
            return FileUtil.readBytes(filename);
        }

        throw new FileNotFoundException(filename);
    }

    public static byte[] readFile(String filename, String FileNotFoundException) {
        if(FileUtil.exists(filename) && !FileUtil.isDir(filename)) {
            return FileUtil.readBytes(filename);
        }

        return FileNotFoundException.getBytes();
    }

    public static boolean isJar(String file) throws IOException {
        return new ZipFile(file).getEntry("META-INF/MANIFEST.MF") != null;
    }

    public static void delete(File f) throws IOException {
        if (f.isDirectory()) {
            for (File c : f.listFiles()) {
                delete(c);
            }
        }

        if (!f.delete()) {
            throw new FileNotFoundException("Failed to delete file: " + f);
        }
    }

    public static void delete(String f) throws IOException {
        delete(new File(f));
    }

    public static void saveFile(String file, byte[] data) {
        FileUtil.writeBytes(file, data, false);
    }

    public static void saveFile(String file, String[] data, String sep) {
        String save = "";
        for(String d : data){
            save += d + sep;
        }

        saveFile(file, save.substring(0, save.length() - sep.length()).getBytes());
    }

    public static void copyfile(String source, String destination) {
        byte[] d = FileUtil.readBytes(source);
        FileUtil.writeBytes(destination, d, false);
    }

    public static String[] Replace(String[] strings, String replace) {
        for(int i = 0;i < strings.length;i ++){
            strings[i] = strings[i].replace(replace, "");
        }

        return strings;
    }

    public static String[] AddString(String[] txts, String string, int off) {
        String[] ret = new String[txts.length + 1];

        System.arraycopy(txts, 0, ret, 0, off);
        ret[off] = string;
        System.arraycopy(txts, off, ret,  off + 1, txts.length - off);

        return ret;
    }

    public static long getFolderSize(File dir) {
        long size = 0;
        for (File f : dir.listFiles()) {

            if (f.isFile()) {
                size += f.length();
            } else {
                size += getFolderSize(f);
            }
        }

        return size;
    }

    public static long getFolderSize(String f) {
        if(new File(f).exists() && new File(f).isDirectory()) {
            return getFolderSize(new File(f));
        }

        throw new NullPointerException(f + (new File(f).exists() ? " exists" : " does not exist") +", "+
                (new File(f).exists() ? "is a directory" : "is not a directory"));
    }

    public static String GetFolder(String file) {
        return file.substring(0, file.lastIndexOf("\\"));
    }
}

