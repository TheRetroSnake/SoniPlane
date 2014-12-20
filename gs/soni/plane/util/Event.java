package gs.soni.plane.util;

import gs.app.lib.application.App;
import gs.app.lib.math.bounds;
import gs.app.lib.util.FileUtil;
import gs.soni.plane.SP;
import gs.soni.plane.draw.*;
import gs.soni.plane.draw.Window;
import gs.soni.plane.menu.menu;
import gs.soni.plane.project.ProjectLoader;
import gs.soni.plane.project.project;
import gs.soni.plane.v;

import java.awt.*;
import java.io.FileNotFoundException;

public class Event {
    public static final int E_PROJ = 1;
    public static final int E_OPENPROJ = 2;
    public static final int E_OPENBACK = 3;
    public static final int E_UPDATE = 4;
    public static final int E_PROJ_LOAD = 5;
    public static final int E_PROJ_LOAD_ = 6;
    public static final int E_SAVE = 7;
    public static final int E_CONF = 8;
    public static final int E_MENU = 9;
    public static final int E_GETBACK = 10;
    public static final int E_SETTINGS = 11;
    public static final int E_PREFR = 12;
    public static final int E_PREFS = 13;

    public static final int E_CE_DONE = 0x81;
    public static final int E_CE_ARTC = 0x82;
    public static final int E_CE_MAPC = 0x83;
    public static final int E_CE_PALT = 0x84;
    public static final int E_CE_ARTT = 0x85;
    public static final int E_CE_MAPT = 0x86;
    public static final int E_CE_FILE = 0x8A;
    public static final int E_CE_FILE_ = 0x90;

    public static final byte EP_MAX = (byte) 0xFF;
    private static EventHandler event;
    private static byte EventPriority = 0;
    public static boolean SelMenu;

    public static void SetEvent(EventHandler EventID) {
        if (EventID != null && (EventPriority & 0xFF) < (EventID.getPriority() & 0xFF)) {
            event = EventID;
            EventPriority = EventID.getPriority();
        }
    }

    public static void ClearEvent() {
        event = null;
        EventPriority = 0;
    }

    public static void HandleEvent() {
        if (event == null) {
            return;
        }

        EventHandler a = event;
        event.invoke();

        if (a == event) {
            v.mode = event.getMode();
            event = null;
            EventPriority = 0;
        }
    }

    public static EventHandler GetEvent() {
        return event;
    }

    public static EventHandler ReturnEvent(int EventID, int priority, String string) {
        switch (EventID) {
            case E_MENU:
                return new EventHandler(E_MENU, priority, string) {
                    @Override
                    public void invoke() {
                        removeWM();
                        SP.ClearData();
                        SP.CreateMainMenu();
                    }
                };

            case E_PROJ:
                return new EventHandler(E_PROJ, priority, string) {
                    @Override
                    public void invoke() {
                        projectMenu();

                        v.BlockControls = false;
                        v.LastSave = System.currentTimeMillis();
                        v.LastSaveDel = System.currentTimeMillis();
                        SP.SetMenu(new menu());

                        windowManager w = SP.getWM();
                        w.addWindow(new debug());
                        w.addWindow(new tileEditor());
                        w.addWindow(new palette());
                        w.addWindow(new palChange());
                        w.addWindow(new tileDisp());
                        w.addWindow(new plane());

                        try {
                            v.project = project.GetField("switch", new String(file.readFile(v.project)).split("\n"), v.project);
                        } catch (FileNotFoundException e1) {
                            e1.printStackTrace();
                        }
                    }
                };

            case E_PROJ_LOAD:
                return new EventHandler(E_PROJ_LOAD, priority, string) {
                    @Override
                    public void invoke() {
                        v.project = this.getString();
                        App.SetTitle("Loading project: " + v.project);
                        new Thread(new ProjectLoader()).start();
                        loading();
                    }
                };

            case E_PROJ_LOAD_:
                return new EventHandler(E_PROJ_LOAD_, priority, string) {
                    @Override
                    public void invoke() {
                        ReloadProject();
                    }
                };

            case E_SAVE:
                return new EventHandler(E_SAVE, priority, string) {
                    @Override
                    public void invoke() {
                        defActList.SaveProject();
                    }
                };

            case E_CONF:
                return new EventHandler(E_CONF, priority, string) {
                    @Override
                    public void invoke() {
                        EditProject(v.project);
                    }
                };

            case E_CE_FILE:
                final String field = string;
                return new EventHandler(E_CE_FILE, priority, string) {
                    @Override
                    public void invoke() {
                        try {
                            new Thread(new FileOpen(new EventHandler(E_CE_FILE_, Event.EP_MAX, null) {
                                @Override
                                public void invoke() {
                                    SP.GetMenuList().SetBaseText(field, getString());
                                    try {
                                        file.saveFile(v.prefs,
                                                project.SetField("chooserDir", file.GetFolder(getString()),
                                                        new String(file.readFile(v.prefs)).split("\n")), "\n");
                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                    SP.repaint();
                                }

                            }, "", "", project.GetField("chooserDir", new String(file.readFile(v.prefs)).split("\n")))).start();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                };

        }
        return null;
    }

    public static void ReloadProject() {
        removeWM();
        SP.ClearData();
        App.SetTitle("Loading project: " + v.project);
        new Thread(new ProjectLoader()).start();
        loading();
    }

    public static void EditProject(String proj) {
        removeWM();
        SP.ClearData();
        SP.CreateEditor(proj);
        SP.repaintLater();
    }

    private static void DelProject(String proj, int EventID) {
        SP.ClearData();
        FileUtil.delete(proj);
        CreateList(EventID);
        SP.repaintLater();
    }

    private static void CreateList(int EventID) {
        if (EventID == E_OPENPROJ) {
            OpenProj();
        } else {
            OpenBack();
        }
    }

    public static void projectMenu() {
        MenuBar m = new MenuBar();
        m.add(defMenu.GetMenu(defMenu.PROJ_FILE));
        m.add(defMenu.GetMenu(defMenu.PROJ_PLANE));
        SelMenu = v.SelBounds != null;
        if (SelMenu) {
            m.add(defMenu.GetMenu(defMenu.PROJ_SEL));
        }
        m.add(defMenu.GetMenu(defMenu.PROJ_SETT));
        m.add(defMenu.GetMenu(defMenu.MENU_HELP));
        App.getJFrame().setMenuBar(m);
    }

    public static EventHandler[] GetFileMenuArr(final int EventID) {
        return new EventHandler[]{ReturnEvent(E_PROJ_LOAD, EP_MAX, ""),
                new EventHandler(EventID, EP_MAX, "") {
                    @Override
                    public void invoke() {
                        DelProject(getString(), EventID);
                    }
                },
                new EventHandler(EventID, EP_MAX, "") {
                    @Override
                    public void invoke() {
                        EditProject(getString());
                    }
                },
        };
    }

    public static void OpenProj() {
        String[] t = file.GetFileList(v.LaunchAdr + "/projects/", "SPP");
        if (!t[0].equals("")) {

            SP.SetMenu(file.FormMenu(t, Style.GetStyle("menu_big"), 1f, GetFileMenuArr(E_OPENPROJ), new SortType("ABC", "Y"), false, 3).
                    AddMenu("back", "Back", menu.GetScreenSize(), menu.GetScreenSize(), 80, 40,
                            Style.GetStyle("menu_center"), 1f, Event.ReturnEvent(Event.E_MENU, 0x10, null)));
            SP.GetMenuList().GetMenu(SP.GetMenuList().GetMenuID("back")).SetXYOff(-80, -40);

            SP.repaintLater();

        } else {
            SP.CreateMainMenu();
        }
    }

    public static void OpenBack() {
        String[] f = file.GetFileList(v.LaunchAdr + "/projects/", "SPP");
        if (!f[0].equals("")) {
            SP.SetMenu(file.FormMenu(f, Style.GetStyle("menu_big"), 1f, new EventHandler[]{
                    new EventHandler(Event.E_GETBACK, Event.EP_MAX, null) {
                        @Override
                        public void invoke() {
                            try {
                                String[] c = file.GetFileList(v.LaunchAdr + "/autosave/", "SPP", "name", project.GetField("name",
                                        new String(file.readFile(this.getString())).split("\n")));

                                if (!c[0].equals("")) {
                                    SP.SetMenu(file.FormMenu(c, Style.GetStyle("menu_big"), 1f,
                                            GetFileMenuArr(E_OPENBACK), new SortType("ABC", "Y"), true, 1).
                                            AddMenu("back", "Back", menu.GetScreenSize(), menu.GetScreenSize(), 80, 40,
                                                    Style.GetStyle("menu_center"), 1f, Event.ReturnEvent(Event.E_MENU, 0x10, null)));
                                    SP.GetMenuList().GetMenu(SP.GetMenuList().GetMenuID("back")).SetXYOff(-80, -40);
                                    SP.repaintLater();
                                }
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    }}, new SortType("ABC", "Y"), false, 0).AddMenu("back", "Back", menu.GetScreenSize(), menu.GetScreenSize(), 80, 40,
                    Style.GetStyle("menu_center"), 1f, Event.ReturnEvent(Event.E_MENU, 0x10, null)));
            SP.GetMenuList().GetMenu(SP.GetMenuList().GetMenuID("back")).SetXYOff(-80, -40);
            SP.repaintLater();
        } else {
            SP.CreateMainMenu();
        }
    }

    public static void loading() {
        removeWM();
        windowManager w = new windowManager();
        SP.addToLogicList(w);
        w.addWindow(new loading(Event.E_PROJ));
    }

    public static void removeWM() {
        if (SP.getWM() != null) {
            SP.getWM().destroy();
            SP.rmvFromRenderList(SP.getWM());
            SP.rmvFromLogicList(SP.getWM());
        }
    }
}
