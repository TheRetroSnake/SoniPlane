package gs.soni.plane.util;

import java.awt.*;

public class defMenu {

    public static final int MENU_FILE =      0;
    public static final int MENU_HELP =      1;
    public static final int PROJ_FILE =      2;
    public static final int PROJ_SETT =      3;
    public static final int PROJ_PLANE =     4;
    public static final int PROJ_SEL =       5;

    public static final int MEIT_EXIT =      0;
    public static final int MEIT_ABOUT =     1;
    public static final int PROJ_RELOAD =    2;
    public static final int PROJ_SAVE =      3;
    public static final int PROJ_EDIT =      4;
    public static final int PROJ_OPEN =      5;
    public static final int PROJ_PSIZE =    10;
    public static final int PROJ_DHP =      11;
    public static final int PROJ_DLP =      12;
    public static final int PROJ_PWIDTHP =  13;
    public static final int PROJ_PWIDTHM =  14;
    public static final int PROJ_PHEIGHTP = 15;
    public static final int PROJ_PHEIGHTM = 16;
    public static final int SEL_HILOP =     20;
    public static final int SEL_FLIPX =     21;
    public static final int SEL_FLIPY =     22;
    public static final int SEL_PLINEP =    23;
    public static final int SEL_PLINEM =    24;
    public static final int SEL_DESEL =     25;
    public static final int SEL_TILINP =    26;
    public static final int SEL_TILINM =    27;
    public static final int SEL_FILLSEL =   28;
    public static final int SEL_CLEAR =     29;
    public static final int SEL_REMOVE =    30;
    public static final int SEL_INSERT =    31;

    public static Menu GetMenu(int id){
        switch (id){
            case MENU_HELP:
                Menu help = new Menu("Help");
                help.add(GetMenuItem(MEIT_ABOUT));
                return help;

            case MENU_FILE:
                Menu file = new Menu("File");
                file.add(GetMenuItem(MEIT_EXIT));
                return file;

            case PROJ_FILE:
                Menu pfile = new Menu("File");
                pfile.add(GetMenuItem(PROJ_SAVE));
                pfile.add(GetMenuItem(PROJ_OPEN));
                pfile.add(GetMenuItem(PROJ_EDIT));
                pfile.add(GetMenuItem(PROJ_RELOAD));
                pfile.add(GetMenuItem(MEIT_EXIT));
                return pfile;

            case PROJ_SETT:
                Menu psett = new Menu("Settings");
                psett.add(GetMenuItem(PROJ_PSIZE));
                psett.add(GetMenuItem(PROJ_DHP));
                psett.add(GetMenuItem(PROJ_DLP));
                return psett;

            case PROJ_PLANE:
                Menu plane = new Menu("Edit");
                plane.add(GetMenuItem(PROJ_PWIDTHP));
                plane.add(GetMenuItem(PROJ_PWIDTHM));
                plane.add(GetMenuItem(PROJ_PHEIGHTP));
                plane.add(GetMenuItem(PROJ_PHEIGHTM));
                return plane;

            case PROJ_SEL:
                Menu sel = new Menu("Selection");
                sel.add(GetMenuItem(SEL_DESEL));
                sel.add(GetMenuItem(SEL_CLEAR));
                sel.add(GetMenuItem(SEL_REMOVE));
                sel.add(GetMenuItem(SEL_INSERT));
                sel.add(GetMenuItem(SEL_FILLSEL));
                sel.add(GetMenuItem(SEL_HILOP));
                sel.add(GetMenuItem(SEL_FLIPX));
                sel.add(GetMenuItem(SEL_FLIPY));
                sel.add(GetMenuItem(SEL_PLINEP));
                sel.add(GetMenuItem(SEL_PLINEM));
                sel.add(GetMenuItem(SEL_TILINP));
                sel.add(GetMenuItem(SEL_TILINM));
                return sel;

        }

        throw new NullPointerException("Menu with ID "+ id +" not found!");
    }

    private static MenuItem GetMenuItem(int id) {
        switch (id){
            case MEIT_ABOUT:
                MenuItem about = new MenuItem("About");
                about.addActionListener(defActList.Get(defActList.ABOUT));
                return about;

            case MEIT_EXIT:
                MenuItem exit = new MenuItem("Exit");
                exit.addActionListener(defActList.Get(defActList.EXIT));
                return exit;

            case PROJ_SAVE:
                MenuItem save = new MenuItem("Save");
                save.addActionListener(defActList.Get(defActList.SAVE));
                return save;

            case PROJ_OPEN:
                MenuItem open = new MenuItem("Open new");
                open.addActionListener(defActList.Get(defActList.OPEN));
                return open;

            case PROJ_EDIT:
                MenuItem edit = new MenuItem("Reconfigure");
                edit.addActionListener(defActList.Get(defActList.EDIT));
                return edit;

            case PROJ_RELOAD:
                MenuItem reload = new MenuItem("Reload");
                reload.addActionListener(defActList.Get(defActList.RELOAD));
                return reload;

            case PROJ_PSIZE:
                MenuItem psize = new MenuItem("Change Plane Size");
                psize.addActionListener(defActList.Get(defActList.CHPLSZ));
                return psize;

            case PROJ_DHP:
                MenuItem dhp = new MenuItem("Draw High Plane");
                dhp.addActionListener(defActList.Get(defActList.DRHIPLANE));
                return dhp;

            case PROJ_DLP:
                MenuItem dlp = new MenuItem("Draw Low Plane");
                dlp.addActionListener(defActList.Get(defActList.DRLOPLANE));
                return dlp;

            case PROJ_PWIDTHP:
                MenuItem pwidthp = new MenuItem("Plane Width +");
                pwidthp.addActionListener(defActList.Get(defActList.PWIDTHP));
                return pwidthp;

            case PROJ_PWIDTHM:
                MenuItem pwidthm = new MenuItem("Plane Width -");
                pwidthm.addActionListener(defActList.Get(defActList.PWIDTHM));
                return pwidthm;

            case PROJ_PHEIGHTP:
                MenuItem pheightp = new MenuItem("Plane Height +");
                pheightp.addActionListener(defActList.Get(defActList.PHEIGHTP));
                return pheightp;

            case PROJ_PHEIGHTM:
                MenuItem pheightm = new MenuItem("Plane Height -");
                pheightm.addActionListener(defActList.Get(defActList.PHEIGHTM));
                return pheightm;

            case SEL_HILOP:
                MenuItem hilo = new MenuItem("Set To High/Low Plane");
                hilo.addActionListener(defActList.Get(defActList.HILOPL));
                return hilo;

            case SEL_FLIPX:
                MenuItem flipx = new MenuItem("Flip Horizontally");
                flipx.addActionListener(defActList.Get(defActList.FLIPX));
                return flipx;

            case SEL_FLIPY:
                MenuItem flipy = new MenuItem("Flip Vertically");
                flipy.addActionListener(defActList.Get(defActList.FLIPY));
                return flipy;

            case SEL_PLINEP:
                MenuItem plinep = new MenuItem("Palette line +");
                plinep.addActionListener(defActList.Get(defActList.PLINEP));
                return plinep;

            case SEL_PLINEM:
                MenuItem plinem = new MenuItem("Palette line -");
                plinem.addActionListener(defActList.Get(defActList.PLINEM));
                return plinem;

            case SEL_DESEL:
                MenuItem deselect = new MenuItem("Deselect");
                deselect.addActionListener(defActList.Get(defActList.DESEL));
                return deselect;

            case SEL_TILINP:
                MenuItem tilinp = new MenuItem("Tile Index +");
                tilinp.addActionListener(defActList.Get(defActList.TILINP));
                return tilinp;

            case SEL_TILINM:
                MenuItem tilinm = new MenuItem("Tile Index -");
                tilinm.addActionListener(defActList.Get(defActList.TILINM));
                return tilinm;

            case SEL_FILLSEL:
                MenuItem fillsel = new MenuItem("Fill Selection with tile");
                fillsel.addActionListener(defActList.Get(defActList.FILLSEL));
                return fillsel;

            case SEL_CLEAR:
                MenuItem clear = new MenuItem("Clear Tiles From Selection");
                clear.addActionListener(defActList.Get(defActList.CLEARSEL));
                return clear;

            case SEL_REMOVE:
                MenuItem remove = new MenuItem("Remove Tiles From Selection");
                remove.addActionListener(defActList.Get(defActList.REMOVESEL));
                return remove;

            case SEL_INSERT:
                MenuItem insert = new MenuItem("Insert Tiles From Selection");
                insert.addActionListener(defActList.Get(defActList.INSERTSEL));
                return insert;

        }

        throw new NullPointerException("MenuItem with ID "+ id +" not found!");
    }
}
