package gs.soni.plane.util;

import gs.app.lib.application.App;
import gs.app.lib.util.FileUtil;
import gs.app.lib.util.KeyUtil;
import gs.soni.plane.SP;
import gs.soni.plane.project.Save;
import gs.soni.plane.project.project;
import gs.soni.plane.v;
import javafx.scene.control.RadioButton;

import javax.swing.*;
import javax.swing.plaf.basic.BasicRadioButtonMenuItemUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.FileNotFoundException;
import java.io.IOException;

public class defMenu {
    public static final int MENU_FILE =      0;
    public static final int MENU_PLANE =     1;
    public static final int MENU_SETT =      2;
    public static final int MENU_SEL =       3;
    public static final int MENU_STATE =     4;
    public static final int MENU_HELP =      5;

    public static final int MEIT_EXIT =      0;
    public static final int MEIT_ABOUT =     1;
    public static final int PROJ_RELOAD =    2;
    public static final int PROJ_SAVE =      3;
    public static final int PROJ_EDIT =      4;
    public static final int PROJ_OPEN =      5;

    public static final int PROJ_PWIDTHP =  10;
    public static final int PROJ_PWIDTHM =  11;
    public static final int PROJ_PHEIGHTP = 12;
    public static final int PROJ_PHEIGHTM = 13;

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
                Menu pfile = new Menu("File");
                pfile.add(GetMenuItem(PROJ_SAVE));
                pfile.add(GetMenuItem(PROJ_OPEN));
                pfile.add(GetMenuItem(PROJ_EDIT));
                pfile.add(GetMenuItem(PROJ_RELOAD));
                pfile.add(GetMenuItem(MEIT_EXIT));
                return pfile;

            case MENU_SETT:
                return getSettingsMenu();

            case MENU_PLANE:
                Menu plane = new Menu("Edit");
                plane.add(GetMenuItem(PROJ_PWIDTHP));
                plane.add(GetMenuItem(PROJ_PWIDTHM));
                plane.add(GetMenuItem(PROJ_PHEIGHTP));
                plane.add(GetMenuItem(PROJ_PHEIGHTM));
                return plane;

            case MENU_SEL:
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

            case MENU_STATE:
                return new Menu("Save States");

        }

        throw new NullPointerException("Menu with ID "+ id +" not found!");
    }

    private static MenuItem GetMenuItem(int id) {
        switch (id){
            case MEIT_ABOUT:
                return createItem("About", -1, false, defActList.Get(defActList.ABOUT));

            case MEIT_EXIT:
                return createItem("Exit", -1, false, defActList.Get(defActList.EXIT));


            case PROJ_SAVE:
                return createItem("Save", KeyUtil.S, false, defActList.Get(defActList.SAVE));

            case PROJ_OPEN:
                return createItem("Open new", KeyUtil.O, false, defActList.Get(defActList.OPEN));

            case PROJ_EDIT:
                return createItem("Reconfigure", KeyUtil.E, false, defActList.Get(defActList.EDIT));

            case PROJ_RELOAD:
                return createItem("Reload", KeyUtil.R, false, defActList.Get(defActList.RELOAD));

            case PROJ_PWIDTHP:
                return createItem("Increase Plane Width", KeyUtil.NUM6, false, defActList.Get(defActList.PWIDTHP));

            case PROJ_PWIDTHM:
                return createItem("Decrease Plane Width", KeyUtil.NUM7, false, defActList.Get(defActList.PWIDTHM));

            case PROJ_PHEIGHTP:
                return createItem("Increase Plane Height", KeyUtil.NUM8, false, defActList.Get(defActList.PHEIGHTP));

            case PROJ_PHEIGHTM:
                return createItem("Decrease Plane Height", KeyUtil.NUM9, false, defActList.Get(defActList.PHEIGHTM));


            case SEL_HILOP:
                return createItem("Set To High/Low Plane", KeyUtil.U, false, defActList.Get(defActList.HILOPL));

            case SEL_TILINP:
                return createItem("Increase Tile Index", KeyUtil.J, false, defActList.Get(defActList.TILINP));

            case SEL_TILINM:
                return createItem("Decrease Tile Index", KeyUtil.M, false, defActList.Get(defActList.TILINM));

            case SEL_PLINEP:
                return createItem("Increase Palette line", KeyUtil.H, false, defActList.Get(defActList.PLINEP));

            case SEL_PLINEM:
                return createItem("Decrease Palette line", KeyUtil.N, false, defActList.Get(defActList.PLINEM));

            case SEL_FLIPX:
                return createItem("Flip Horizontally", KeyUtil.G, false, defActList.Get(defActList.FLIPX));

            case SEL_FLIPY:
                return createItem("Flip Vertically", KeyUtil.B, false, defActList.Get(defActList.FLIPY));

            case SEL_DESEL:
                return createItem("Deselect", KeyUtil.Y, false, defActList.Get(defActList.DESEL));

            case SEL_FILLSEL:
                return createItem("Fill Selection with tile", KeyUtil.O, false, defActList.Get(defActList.FILLSEL));

            case SEL_CLEAR:
                return createItem("Clear Tiles From Selection", KeyUtil.L, false, defActList.Get(defActList.CLEARSEL));

            case SEL_REMOVE:
                return createItem("Remove Tiles From Selection", KeyUtil.I, false, defActList.Get(defActList.REMOVESEL));

            case SEL_INSERT:
                return createItem("Insert Tiles To Selection", KeyUtil.K, false, defActList.Get(defActList.INSERTSEL));

        }

        throw new NullPointerException("MenuItem with ID "+ id +" not found!");
    }

    private static MenuItem createItem(String text, int key, boolean shift, ActionListener a) {
        MenuItem r = new MenuItem(text);
        if(key != -1) {
            r.setShortcut(new MenuShortcut(key, shift));
        }

        if(a != null) {
            r.addActionListener(a);
        }

        return r;
    }

    public static void createStates(Menu m) {
        m.removeAll();
        int amount = project.getStateAmount();

        for(int i = 1;i < amount + 1;i ++){
            CreateState(i, true, m);
        }

        CreateState(amount + 1, false, m);
    }

    private static void CreateState(final int id, boolean full, Menu menu) {
        Menu m = new Menu("State "+ id);
        menu.add(m);

        MenuItem load = new MenuItem("Load");
        load.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                v.project = v.LaunchAdr +"/autosave/st"+ id +"-"+ project.GetField("name", project.getFields(v.project)) +".SPP";
                Event.SetEvent(Event.ReturnEvent(Event.E_PROJ_LOAD, Event.EP_MAX, v.project));
            }
        });
        m.add(load);

        MenuItem save = new MenuItem("Save");
        save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(new Save("st"+ id +"-"+ project.GetField("name", project.getFields(v.project)), true, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        defMenu.createStates(App.getJFrame().getMenuBar().getMenu(defMenu.MENU_STATE));
                    }
                }), "SaveState").start();
            }
        });
        m.add(save);

        MenuItem del = new MenuItem("Delete");
        del.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    file.delete(v.LaunchAdr +"/autosave/st"+ id +"-"+ project.GetField("name", project.getFields(v.project)) +".SPP");
                    file.delete(v.LaunchAdr +"/autosave/st"+ id +"-"+ project.GetField("name", project.getFields(v.project)));

                } catch (IOException e1) {
                    e1.printStackTrace();
                }

                int minus = 0;
                for(int i = 1;i < project.getStateAmount() + 2;i ++){
                    String o = v.LaunchAdr +"/autosave/st"+ i +"-"+ project.GetField("name", project.getFields(v.project));

                    if(FileUtil.exists(o +".SPP")){
                        String f = o.replace("st"+ i, "st"+ (i - minus));
                        FileUtil.rename(o +".SPP", f +".SPP");
                        FileUtil.rename(o, f);

                        FileUtil.writeString(f +".SPP", FileUtil.readString(f +".SPP").replace("st"+ i, "st"+ (i - minus)), false);

                    } else {
                        minus ++;
                    }
                }
                defMenu.createStates(App.getJFrame().getMenuBar().getMenu(defMenu.MENU_STATE));
            }
        });
        m.add(del);

        if(!full){
            load.setEnabled(false);
            del.setEnabled(false);
        }
    }

    public static Menu getSettingsMenu() {
        Menu m = new Menu("Settings");
        CheckboxMenuItem m1 = new CheckboxMenuItem("Draw High Plane", true);
        m1.setShortcut(new MenuShortcut(KeyUtil.Q, false));
        m1.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                v.DrawHighPlane ^= true;
                SP.repaintLater();
            }
        });

        CheckboxMenuItem m2 = new CheckboxMenuItem("Draw Low Plane", true);
        m2.setShortcut(new MenuShortcut(KeyUtil.W, false));
        m2.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                v.DrawLowPlane ^= true;
                SP.repaintLater();
            }
        });

        m.add(m1);
        m.add(m2);
        return m;
    }
}
