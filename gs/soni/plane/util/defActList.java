package gs.soni.plane.util;

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

    public static final int EXIT =       0;
    public static final int ABOUT =      1;
    public static final int SAVE =       2;
    public static final int EDIT =       3;
    public static final int OPEN =       4;
    public static final int RELOAD =     5;
    public static final int CHPLSZ =    10;
    public static final int DRHIPLANE = 11;
    public static final int DRLOPLANE = 12;
    public static final int PWIDTHP =   13;
    public static final int PWIDTHM =   14;
    public static final int PHEIGHTP =  15;
    public static final int PHEIGHTM =  16;
    public static final int HILOPL =    20;
    public static final int FLIPX =     21;
    public static final int FLIPY =     22;
    public static final int PLINEP =    23;
    public static final int PLINEM =    24;
    public static final int DESEL =     25;
    public static final int TILINP =    26;
    public static final int TILINM =    27;
    public static final int FILLSEL =   28;
    public static final int CLEARSEL =  29;
    public static final int INSERTSEL = 30;
    public static final int REMOVESEL = 31;
    public static final int SHIFTSELU = 36;
    public static final int SHIFTSELD = 37;
    public static final int SHIFTSELL = 38;
    public static final int SHIFTSELR = 39;

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
                        SP.ClearData();
                        SP.CreateGUI();
                        SP.CreateMainMenu();
                    }
                };

            case CHPLSZ:
                return new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        PlaneSize();
                    }
                };

            case DRHIPLANE:
                return new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        v.DrawHighPlane ^= true;
                        SP.repaintLater();
                    }
                };

            case DRLOPLANE:
                return new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        v.DrawLowPlane ^= true;
                        SP.repaintLater();
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
                        mappings.Adjust();
                        v.SelBounds = null;
                        v.setBounds();
                        if(Event.SelMenu) {
                            Event.projectMenu();
                        }
                        SP.repaintLater();
                    }
                };

            case PHEIGHTP:
                return new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        v.mapSize.y ++;
                        mappings.Adjust();
                        v.SelBounds = null;
                        v.setBounds();
                        if(Event.SelMenu) {
                            Event.projectMenu();
                        }
                        SP.repaintLater();
                    }
                };

            case PHEIGHTM:
                return new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        v.mapSize.y --;
                        mappings.Adjust();
                        v.SelBounds = null;
                        v.setBounds();
                        if(Event.SelMenu) {
                            Event.projectMenu();
                        }
                        SP.repaintLater();
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
                        Event.projectMenu();
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
                        mappings.Delete();
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

            case SHIFTSELU:
                return new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        mappings.ShiftMap(0, -1);
                        SP.repaintLater();
                    }
                };

            case SHIFTSELD:
                return new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        mappings.ShiftMap(0, 1);
                        SP.repaintLater();
                    }
                };

            case SHIFTSELL:
                return new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        mappings.ShiftMap(-1, 0);
                        SP.repaintLater();
                    }
                };

            case SHIFTSELR:
                return new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        mappings.ShiftMap(1, 0);
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
        v.setBounds();
        if(Event.SelMenu) {
            Event.projectMenu();
        }
        SP.repaintLater();
    }

    public static void PlaneSize() {
        v.PlaneMode = (v.PlaneMode + 1) % 3;
        v.setBounds();
        SP.repaintLater();
    }
}
