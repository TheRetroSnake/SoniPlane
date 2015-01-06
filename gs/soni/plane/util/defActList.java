package gs.soni.plane.util;

import gs.app.lib.application.App;
import gs.app.lib.util.Browser;
import gs.soni.plane.SP;
import gs.soni.plane.project.Save;
import gs.soni.plane.project.mappings;
import gs.soni.plane.project.project;
import gs.soni.plane.v;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;

public class defActList {
    public static final int EXIT =         0;
    public static final int ABOUT =        1;
    public static final int SAVE =         2;
    public static final int EDIT =         3;
    public static final int OPEN =         4;
    public static final int RELOAD =       5;

    public static final int PWIDTHP =     13;
    public static final int PWIDTHM =     14;
    public static final int PHEIGHTP =    15;
    public static final int PHEIGHTM =    16;

    public static final int HILOPL =      20;
    public static final int FLIPX =       21;
    public static final int FLIPY =       22;
    public static final int PLINEP =      23;
    public static final int PLINEM =      24;
    public static final int DESEL =       25;
    public static final int TILINP =      26;
    public static final int TILINM =      27;
    public static final int FILLSEL =     28;
    public static final int CLEARSEL =    29;
    public static final int INSERTSEL =   30;
    public static final int REMOVESEL =   31;

    public static ActionListener Get(int id){
        switch (id){
            case EXIT:
                return new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        SP.exit();
                    }
                };

            case ABOUT:
                return new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Browser.Open("http://forums.sonicretro.org/index.php?showtopic=33520&view=findpost&p=802920");
                    }
                };

            case SAVE:
                return new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        SaveProject();
                    }
                };

            case EDIT:
                return new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Event.SetEvent(new EventHandler(Event.E_CONF, Event.EP_MAX, null) {
                            @Override
                            public void invoke() {
                                Event.EditProject(v.project);
                            }
                        });
                    }
                };

            case RELOAD:
                return new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Event.ReloadProject();
                    }
                };

            case OPEN:
                return new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        SP.SetNormalTitle();
                        SP.getWM().destroy();
                        SP.ClearData();
                        SP.FixGUI();
                        SP.CreateMainMenu();
                    }
                };

            case PWIDTHP:
                return new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        v.mapSize.x ++;
                        MapSize();
                    }
                };

            case PWIDTHM:
                return new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        v.mapSize.x --;
                        MapSize();
                    }
                };

            case PHEIGHTP:
                return new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        v.mapSize.y ++;
                        MapSize();
                    }
                };

            case PHEIGHTM:
                return new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        v.mapSize.y --;
                        MapSize();
                    }
                };

            case HILOPL:
                return new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        mappings.ChangePlane();
                        SP.repaintLater();
                    }
                };

            case FLIPX:
                return new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        mappings.TileFlip(true, false);
                        SP.repaintLater();
                    }
                };

            case FLIPY:
                return new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        mappings.TileFlip(false, true);
                        SP.repaintLater();
                    }
                };

            case PLINEP:
                return new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        mappings.PalIndex(1);
                        SP.repaintLater();
                    }
                };

            case PLINEM:
                return new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        mappings.PalIndex(-1);
                        SP.repaintLater();
                    }
                };

            case DESEL:
                return new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        v.SelBounds = null;
                        App.getJFrame().getMenuBar().getMenu(defMenu.MENU_SEL).setEnabled(false);
                        SP.repaintLater();
                    }
                };

            case TILINP:
                return new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        mappings.TileIndex(1);
                        SP.repaintLater();
                    }
                };

            case TILINM:
                return new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        mappings.TileIndex(-1);
                        SP.repaintLater();
                    }
                };

            case FILLSEL:
                return new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        mappings.Fill(v.TileSelected, v.FillIncr);
                        SP.repaintLater();
                    }
                };

            case CLEARSEL:
                return new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        mappings.Delete(v.SelBounds);
                        SP.repaintLater();
                    }
                };

            case INSERTSEL:
                return new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        mappings.Insert();
                        Event.projectMenu();
                        SP.repaintLater();
                    }
                };

            case REMOVESEL:
                return new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        mappings.Remove();
                        Event.projectMenu();
                        SP.repaintLater();
                    }
                };
        }

        throw new NullPointerException("Action listener with id "+ id +" not found!");
    }

    public static void SaveProject() {
        try {
            while(SP.SaveThread != null && SP.SaveThread.isAlive());
            SP.SaveThread = new Thread(new Save("bk"+ (System.currentTimeMillis() / 1000) + "-" +
                    project.GetField("name", new String(file.readFile(v.project)).split("\n")), false), "Save Project "+ v.project);
            SP.SaveThread.start();
            v.LastSave = System.currentTimeMillis();
            v.LastSaveDel = System.currentTimeMillis();

        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
    }

    public static void MapSize() {
        mappings.Adjust();
        v.SelBounds = null;
        App.getJFrame().getMenuBar().getMenu(defMenu.MENU_SEL).setEnabled(false);

        SP.repaintLater();
    }
}
