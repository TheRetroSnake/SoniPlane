package gs.soni.plane;

import gs.app.lib.application.App;
import gs.app.lib.application.AppRun;
import gs.app.lib.gfx.Graphics;
import gs.app.lib.gfx.Sprite;
import gs.app.lib.gfx.gfx;
import gs.app.lib.math.bounds;
import gs.app.lib.util.*;
import gs.soni.plane.draw.*;
import gs.soni.plane.menu.menu;
import gs.soni.plane.project.*;
import gs.soni.plane.project.palette;
import gs.soni.plane.util.*;
import gs.soni.plane.util.Event;
import gs.soni.plane.web.UpdateChecker;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class SP extends AppRun {

    private boolean resetPrefs;
    private static ArrayList<Drawable> DrawQueue;
    private static ArrayList<Logicable> LogicList;
    private static Timer Logic;
    private static menu MenuList;
    private static menu WarnMenu;
    public static Thread SaveThread;
    private static boolean resizePref = false;
    private static boolean runlogic;

    public SP(String[] arg, boolean resetPref){
        resetPrefs = resetPref;

        if(arg.length >= 2){
            if(arg[0].equals("-open")){
                Event.SetEvent(Event.ReturnEvent(Event.E_PROJ_LOAD, Event.EP_MAX, arg[1]));

            } else if(arg[0].equals("-edit")){
                v.project = arg[1];
                Event.SetEvent(Event.ReturnEvent(Event.E_CONF, Event.EP_MAX, ""));

            }
        } else if(arg.length > 0 && arg[0].equals("-new")){
            ClearData();
            if(arg.length >= 2){
                CreateEditor(arg[1]);
            } else {
                CreateEditor("");
            }
            repaintLater();
        }
    }

    public void create(){
        v.LaunchAdr = FileUtil.getJarFolder();
        v.LaunchAdr = v.LaunchAdr.substring(0, v.LaunchAdr.length() - 2).replace("\\", "/");
        v.prefs = v.LaunchAdr +"/prefs.txt";
        DrawQueue = new ArrayList<Drawable>();
        LogicList = new ArrayList<Logicable>();

        FileUtil.mkdir(v.LaunchAdr +"/temp");
        FileUtil.mkdir(v.LaunchAdr +"/projects");
        FileUtil.mkdir(v.LaunchAdr +"/autosave");

        if(resetPrefs){
            ResetPrefs();
        }

        try {
            project.RemoveRN();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        SaveThread = new Thread(new UpdateChecker(), "UpdateChecker");
        SaveThread.start();
        resizePref = true;

        SetNormalTitle();
        CreateGUI();
        SetWindowIcon();
        CreateMainMenu();

        Logic = new Timer("LogicTimer");
        Logic.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runlogic = true;

                ArrayList<Logicable> nowLogic = (ArrayList<Logicable>) LogicList.clone();
                for (Logicable L : nowLogic) {
                    L.logic();
                }

                v.IsClicked = Mouse.IsClicked(true);
                Event.HandleEvent();
                if(v.UnlockEndFrame && !v.IsClicked){
                    v.BlockControls = false;
                    v.UnlockEndFrame = false;
                }

                if(v.mode == Event.E_PROJ) {
                    CheckControl();
                }

                MouseUtil.clearPresses();
                KeyUtil.clearPresses();

                if (v.mode == Event.E_PROJ) {
                    autoSave();
                    autoSaveClean();
                }
                runlogic = false;
            }
        }, 1, 66);
    }

    private void SetWindowIcon() {
        App.getJFrame().setIconImage(gfx.getImage(v.LaunchAdr +"/res/logo.png"));
    }

    private void autoSaveClean() {
        if(v.AutoSaveDel > 0 && System.currentTimeMillis() - v.LastSaveDel > v.AutoSaveDel){
            if(file.getFolderSize(v.LaunchAdr +"/autosave") > v.MaxASSize){

                while(SaveThread != null && SaveThread.isAlive());
                SaveThread = new Thread(new SaveDel(v.MaxASSize / 2), "SaveDeleter");
                SaveThread.start();
                v.LastSaveDel = System.currentTimeMillis();
            }
        }
    }

    private void autoSave() {
        if(v.AutoSave > 0 && System.currentTimeMillis() - v.LastSave > v.AutoSave){
            try {
                while(SaveThread != null && SaveThread.isAlive());
                SaveThread = new Thread(new Save("as"+ (System.currentTimeMillis() / 1000) +"-"+
                        project.GetField("name", new String(file.readFile(v.project)).split("\r\n")), true), "AutoSave");
                SaveThread.start();
                v.LastSave = System.currentTimeMillis();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public static void CreateMainMenu() {
        EventHandler Open = new EventHandler(Event.E_OPENPROJ, Event.EP_MAX - 2, null){
            @Override
            public void invoke(){
                Event.OpenProj();
            }
        };
        EventHandler OpenBack = new EventHandler(Event.E_OPENBACK, Event.EP_MAX - 2, null){
            @Override
            public void invoke(){
                Event.OpenBack();
            }
        };
        EventHandler CreateNew = new EventHandler(Event.E_PREFS, 0x10, null){
            @Override
            public void invoke(){
                ClearData();
                CreateEditor("");
                repaintLater();
            }
        };
        EventHandler Settings = new EventHandler(Event.E_SETTINGS, Event.EP_MAX - 1, null){
            @Override
            public void invoke(){
                ClearData();
                SettingsMenu();
            }
        };

        boolean net = false;
        try {
            net = project.GetField("allowWebUtils", new String(file.readFile(v.prefs)).split("\n")).equals("true")
                    && SaveThread != null && SaveThread.isAlive();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        int height = net ? App.GetBounds().h / 5 : App.GetBounds().h / 4;

        MenuList = new menu().AddMenu("openproj", "Open Project", 0, 0, menu.GetScreenSize(), height, Style.GetStyle("menu_big"),1f,Open).
                AddMenu("latest", "Open Backup", 0, menu.After(0), menu.GetScreenSize(), height, Style.GetStyle("menu_big"), 1f,OpenBack).
                AddMenu("create", "Create New", 0, menu.After(1), menu.GetScreenSize(), height, Style.GetStyle("menu_big"), 1f,CreateNew).
                AddMenu("sett", "Settings", 0, menu.After(2), menu.GetScreenSize(), height, Style.GetStyle("menu_big"), 1f, Settings);

        if(net){
            MenuList.AddMenu("checkupdate", "Checking for updates", 0, menu.After(3), menu.GetScreenSize(), height,
                    Style.GetStyle("menu_big"), 1f, new EventHandler(Event.E_UPDATE, Event.EP_MAX, null){
                        public void invoke(){
                            if(MenuList.GetMenuText("checkupdate").equals("Updates available")) {
                                ClearData();
                                UpdateChecker.doMenu();
                            }
                        }
                    });
        }

        repaintLater();
    }

    public static void CreateEditor(final String path) {
        EventHandler done = new EventHandler(Event.E_CE_DONE, Event.EP_MAX - 2, null){
            @Override
            public void invoke(){
                Event.ClearEvent();
                SP.CreateProject();
            }
        };
        EventHandler E_CE_PALT = new EventHandler(Event.E_CE_PALT, Event.EP_MAX, null){
            @Override
            public void invoke(){
                v.CE_PALT = getString();
                MenuList.SetMenuText("Palette type", "Palette type: " + getString());
            }
        };
        EventHandler E_CE_ARTT = new EventHandler(Event.E_CE_ARTT, Event.EP_MAX, null){
            @Override
            public void invoke(){
                v.CE_ARTT = getString();
                MenuList.SetMenuText("Art type", "Art type: " + getString());
            }
        };
        EventHandler E_CE_MAPT = new EventHandler(Event.E_CE_MAPT, Event.EP_MAX, null){
            @Override
            public void invoke(){
                v.CE_MAPT = getString();
                MenuList.SetMenuText("Mapping type", "Mapping type: " + getString());
            }
        };
        EventHandler E_CE_ARTC = new EventHandler(Event.E_CE_ARTC, Event.EP_MAX, null){
            @Override
            public void invoke(){
                v.CE_ARTC = getString();
                MenuList.SetMenuText("Art compression", "Art compression: " + getString());
            }
        };
        EventHandler E_CE_MAPC = new EventHandler(Event.E_CE_MAPC, Event.EP_MAX, null){
            @Override
            public void invoke(){
                v.CE_MAPC = getString();
                MenuList.SetMenuText("Mapping compression", "Mapping compression: " + getString());
            }
        };
        EventHandler E_CE_FILE = new EventHandler(Event.E_CE_FILE, Event.EP_MAX, null){
            @Override
            public void invoke(){
                try {
                    new Thread(new FileOpen(new EventHandler(Event.E_CE_FILE, Event.EP_MAX, null){
                        @Override
                        public void invoke(){
                            MenuList.SetBaseText("Project file", getString());
                            repaint();
                        }
                    },
                            ".SPP", project.GetField("title", new String(file.readFile(v.prefs)).split("\n")) +" Project file",
                            v.LaunchAdr +"/projects/"), "File Opener").start();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    exit();
                }
            }
        };

        if(path.equals("")){
            MenuList = new menu().AddProjMenu("Project file", "", 0, 0, menu.GetScreenSize(), 20,
                    Style.GetStyle("menu_left"), 1f, new EventHandler[]{null, E_CE_FILE}, 0xC0000004).
                    AddProjMenu("Project name", "", 0, 20, menu.GetScreenSize(), 20, Style.GetStyle("menu_left"), 1f, null, 0xC0000000).
                    AddProjMenu("Palette file", "", 0, 40, menu.GetScreenSize(), 20, Style.GetStyle("menu_left"), 1f,
                            new EventHandler[]{null, Event.ReturnEvent(Event.E_CE_FILE, Event.EP_MAX, "Palette file")}, 0xC0000004).

                    AddAdjustButton("Transparent line", 0, 60, App.GetBounds().w / 2, 20, "0",
                            Style.GetStyle("menu_left"), 1f, null, menu.AT_TEXT, "\\D").

                    AddAdjustButton("Transparent entry", App.GetBounds().w / 2, 60, App.GetBounds().w / 2, 20,
                            "0", Style.GetStyle("menu_left"), 1f, null, menu.AT_TEXT, "\\D").

                    AddAdjustButton("Palette line offset", 0, 80, App.GetBounds().w / 2, 20,
                            "0", Style.GetStyle("menu_left"), 1f, null, menu.AT_TMIN | menu.AT_TEXT, "\\D").

                    AddProjMenu("Art file", "", 0, 100, menu.GetScreenSize(), 20, Style.GetStyle("menu_left"), 1f,
                            new EventHandler[]{null, Event.ReturnEvent(Event.E_CE_FILE, Event.EP_MAX, "Art file")}, 0xC0000004).

                    CompDropDown("Art compression", 0, 120, App.GetBounds().w / 2, 20, Style.GetStyle("menu_left"), 1f, E_CE_ARTC).
                    AddProjMenu("Mapping file", "", 0, 140, menu.GetScreenSize(), 20, Style.GetStyle("menu_left"), 1f,
                            new EventHandler[]{null, Event.ReturnEvent(Event.E_CE_FILE, Event.EP_MAX, "Mapping file")}, 0xC0000004).

                    CompDropDown("Mapping compression", 0, 160, App.GetBounds().w / 2, 20, Style.GetStyle("menu_left"), 1f, E_CE_MAPC).
                    AddAdjustButton("Plane Width", App.GetBounds().w / 2, 180, App.GetBounds().w / 2, 20,
                            "0", Style.GetStyle("menu_left"), 1f, null, menu.AT_TEXT, "\\D").

                    AddAdjustButton("Plane Height", 0, 180, App.GetBounds().w / 2, 20, "0",
                            Style.GetStyle("menu_left"), 1f, null, menu.AT_TEXT, "\\D").

                    AddAdjustButton("Map offset", App.GetBounds().w / 2, 200, App.GetBounds().w / 2, 20,
                            "0", Style.GetStyle("menu_left"), 1f, null, menu.AT_TMIN | menu.AT_TEXT, "\\D").

                    AddAdjustButton("Autosave delay (Minutes)", 0, 200, App.GetBounds().w / 2, 20, "0",
                            Style.GetStyle("menu_left"), 1f, null, menu.AT_TEXT, "\\D").

                    AddMenu("done", "Done!", 80, 220, menu.GetScreenSize(), 40, Style.GetStyle("menu_center"), 1f, done).
                    AddMenu("cancel", "Cancel", 0, 220, 80, 40, Style.GetStyle("menu_center"), 1f,
                            Event.ReturnEvent(Event.E_MENU, 0x10, null)).


                    ModuleList("Palette type", App.GetBounds().w / 2, 80, App.GetBounds().w / 2, 20,
                            Style.GetStyle("menu_left"), 1f, E_CE_PALT, v.LaunchAdr + "/modules/palette/", "txt").

                    ModuleList("Art type", App.GetBounds().w / 2, 120, App.GetBounds().w / 2, 20,
                            Style.GetStyle("menu_left"), 1f, E_CE_ARTT, v.LaunchAdr + "/modules/tile/", "txt").

                    ModuleList("Mapping type", App.GetBounds().w / 2, 160, App.GetBounds().w / 2, 20,
                            Style.GetStyle("menu_left"), 1f, E_CE_MAPT, v.LaunchAdr + "/modules/map/", "txt");
        } else {
            try {
                String[] s = new String(file.readFile(path)).split("\n");
                EventHandler cancel = new EventHandler(Event.E_CE_FILE, Event.EP_MAX, null){
                    @Override
                    public void invoke(){
                        if(FileUtil.exists(v.project)) {
                            SP.ClearData();
                            App.SetTitle("Loading project: " + v.project);
                            new Thread(new ProjectLoader(), "ProjectLoader").start();
                            addToLogicList(new loading(Event.E_PROJ));

                        } else {
                            SP.ClearData();
                            CreateMainMenu();
                            SP.repaintLater();
                        }
                    }
                };


                MenuList = new menu().AddProjMenu("Project file", path, 0, 0, menu.GetScreenSize(), 20,
                        Style.GetStyle("menu_left"), 1f, new EventHandler[]{null, E_CE_FILE}, 0xC0000004).

                        AddProjMenu("Project name", project.GetField("name", s), 0, 20, menu.GetScreenSize(), 20,
                                Style.GetStyle("menu_left"), 1f, null, 0xC0000000).

                        AddProjMenu("Palette file", project.GetField("palette file", s), 0, 40, menu.GetScreenSize(), 20,
                                Style.GetStyle("menu_left"), 1f, new EventHandler[]{
                                        null, Event.ReturnEvent(Event.E_CE_FILE, Event.EP_MAX, "Palette file") }, 0xC0000004).

                        AddAdjustButton("Transparent line", 0, 60, App.GetBounds().w / 2, 20, project.GetField("trans line", s),
                                Style.GetStyle("menu_left"), 1f, null, menu.AT_TEXT, "\\D").

                        AddAdjustButton("Transparent entry", App.GetBounds().w / 2, 60, App.GetBounds().w / 2, 20,
                                project.GetField("trans off", s), Style.GetStyle("menu_left"), 1f, null, menu.AT_TEXT, "\\D").

                        AddAdjustButton("Palette line offset", 0, 80, App.GetBounds().w / 2, 20,
                                project.GetField("trans off", s, "0"), Style.GetStyle("menu_left"), 1f, null,
                                menu.AT_TMIN | menu.AT_TEXT, "\\D").

                        AddProjMenu("Art file", project.GetField("art file", s), 0, 100, menu.GetScreenSize(), 20,
                                Style.GetStyle("menu_left"), 1f, new EventHandler[]{
                                        null, Event.ReturnEvent(Event.E_CE_FILE, Event.EP_MAX, "Art file")}, 0xC0000004).

                        CompDropDown("Art compression", 0, 120, App.GetBounds().w / 2, 20, Style.GetStyle("menu_left"), 1f, E_CE_ARTC).
                        AddProjMenu("Mapping file", project.GetField("map file", s), 0, 140, menu.GetScreenSize(), 20,
                                Style.GetStyle("menu_left"), 1f, new EventHandler[]{
                                        null, Event.ReturnEvent(Event.E_CE_FILE, Event.EP_MAX, "Mapping file")}, 0xC0000004).

                        CompDropDown("Mapping compression", 0, 160, App.GetBounds().w / 2, 20, Style.GetStyle("menu_left"), 1f, E_CE_MAPC).

                        AddAdjustButton("Plane Width", App.GetBounds().w / 2, 180, App.GetBounds().w / 2, 20,
                                project.GetField("map width", s), Style.GetStyle("menu_left"), 1f, null, menu.AT_TEXT, "\\D").

                        AddAdjustButton("Plane Height", 0, 180, App.GetBounds().w / 2, 20, project.GetField(
                                "map height", s), Style.GetStyle("menu_left"), 1f, null, menu.AT_TEXT, "\\D").

                        AddAdjustButton("Map offset", App.GetBounds().w / 2, 200, App.GetBounds().w / 2, 20, project.GetField(
                                "map offset", s), Style.GetStyle("menu_left"), 1f, null, menu.AT_TMIN | menu.AT_TEXT, "\\D").

                        AddAdjustButton("Autosave delay (Minutes)", 0, 200, App.GetBounds().w / 2, 20, "" + (Long.parseLong(
                                project.GetField("autosave", s)) / 60000), Style.GetStyle("menu_left"), 1f, null, menu.AT_TEXT, "\\D").

                        AddMenu("done", "Done!", 80, 220, App.GetBounds().w - 80, 40, Style.GetStyle("menu_center"), 1f, done).
                        AddMenu("cancel", "Cancel", 0, 220, 80, 40, Style.GetStyle("menu_center"), 1f, cancel).


                        ModuleList("Palette type", App.GetBounds().w / 2, 80, App.GetBounds().w / 2, 20, Style.GetStyle("menu_left"),
                                1f, E_CE_PALT, v.LaunchAdr + "/modules/palette/", "txt").

                        ModuleList("Art type", App.GetBounds().w / 2, 120, App.GetBounds().w / 2, 20, Style.GetStyle("menu_left"),
                                1f, E_CE_ARTT, v.LaunchAdr + "/modules/tile/", "txt").

                        ModuleList("Mapping type", App.GetBounds().w / 2, 160, App.GetBounds().w / 2, 20, Style.GetStyle("menu_left"),
                                1f, E_CE_MAPT, v.LaunchAdr + "/modules/map/", "txt");


                MenuList.SetMenuText("Palette type", "Palette type: " + project.GetField("name", new String(file.readFile(
                        v.LaunchAdr + "/modules/palette/" + project.GetField("palette type", s) + ".txt")).split("\n")));
                MenuList.SetMenuText("Art type", "Art type: " + project.GetField("name", new String(file.readFile(
                        v.LaunchAdr + "/modules/tile/" + project.GetField("art type", s) + ".txt")).split("\n")));
                MenuList.SetMenuText("Mapping type", "Mapping type: " + project.GetField("name", new String(file.readFile(
                        v.LaunchAdr + "/modules/map/"+ project.GetField("map type", s) + ".txt")).split("\n")));
                MenuList.SetMenuText("Art compression", "Art compression: " + project.GetField("name", new String(file.readFile(
                        v.LaunchAdr + "/modules/comp/"+ v.OS +"/"+ project.GetField("art compression", s) + ".txt")).split("\n")));
                MenuList.SetMenuText("Mapping compression", "Mapping compression: " + project.GetField("name", new String(file.readFile(
                        v.LaunchAdr + "/modules/comp/"+ v.OS +"/"+ project.GetField("map compression", s) + ".txt")).split("\n")));

                v.CE_ARTC = project.GetField("name", new String(file.readFile(v.LaunchAdr + "/modules/comp/"+ v.OS +"/"+
                        project.GetField("art compression", s) + ".txt")).split("\n"));
                v.CE_MAPC = project.GetField("name", new String(file.readFile(v.LaunchAdr + "/modules/comp/"+ v.OS +"/"+
                        project.GetField("map compression", s) + ".txt")).split("\n"));
                v.CE_PALT = project.GetField("name", new String(file.readFile(v.LaunchAdr + "/modules/palette/" +
                        project.GetField("palette type", s) + ".txt")).split("\n"));
                v.CE_ARTT = project.GetField("name", new String(file.readFile(v.LaunchAdr + "/modules/tile/" +
                        project.GetField("art type", s) + ".txt")).split("\n"));
                v.CE_MAPT = project.GetField("name", new String(file.readFile(v.LaunchAdr + "/modules/map/" +
                        project.GetField("map type", s) + ".txt")).split("\n"));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public static void CreateProject() {
        if(WarnMenu != null) {
            WarnMenu.destroy();
        }
        WarnMenu = new menu();

        // verify the project
        int y = 260, height = 20;

        if(v.CE_MAPC == null || v.CE_MAPC.equals("")){
            y = AddWarn(""+ y, "Mappings compression must not be null!", y, height, null);
        }
        if(v.CE_ARTC == null || v.CE_ARTC.equals("")){
            y = AddWarn(""+ y, "Art compression must not be null!", y, height, null);
        }

        if(v.CE_MAPT == null || v.CE_MAPT.equals("")){
            y = AddWarn(""+ y, "Mappings type must not be null!", y, height, null);
        }
        if(v.CE_ARTT == null || v.CE_ARTT.equals("")){
            y = AddWarn(""+ y, "Art type must not be null!", y, height, null);
        }
        if(v.CE_PALT == null || v.CE_PALT.equals("")){
            y = AddWarn(""+ y, "Palette type must not be null!", y, height, null);
        }

        if(!new File(MenuList.GetBaseText("Mapping file")).exists()){
            y = AddWarn(""+ y, "Mappings file \""+ MenuList.GetBaseText("Mapping file") +"\" does not exist!", y, height, null);
        }
        if(!new File(MenuList.GetBaseText("Art file")).exists()){
            y = AddWarn(""+ y, "Art file \""+ MenuList.GetBaseText("Art file") +"\" does not exist!", y, height, null);
        }
        if(!new File(MenuList.GetBaseText("Palette file")).exists()){
            y = AddWarn(""+ y, "Palette file \""+ MenuList.GetBaseText("Palette file") +"\" does not exist!", y, height, null);
        }

        if(MenuList.GetBaseText("Project file") == null || MenuList.GetBaseText("Project file").equals("") ||
                !new File(MenuList.GetBaseText("Project file")).isAbsolute()){
            y = AddWarn(""+ y, "File \""+ MenuList.GetBaseText("Project file") +"\" cannot be created!", y, height, null);
        }

        if(y != 260) {
            return;
        }

        // save project
        file.saveFile(MenuList.GetBaseText("Project file"),
            project.SetField("autosave", ""+ Long.parseLong(MenuList.GetBaseText("Autosave delay (Minutes)")) * 60000,
            project.SetField("map offset", ""+ Integer.parseInt(MenuList.GetBaseText("Map offset")),
            project.SetField("map width", ""+ Integer.parseInt(MenuList.GetBaseText("Plane Width")),
            project.SetField("map height", ""+ Integer.parseInt(MenuList.GetBaseText("Plane Height")),
            project.SetField("map compression", file.GetFileList_(v.LaunchAdr +"/modules/comp/"+ v.OS +"/", "txt", "name",
                v.CE_MAPC)[0].replace(v.LaunchAdr +"/modules/comp/"+ v.OS +"/", "").replace(".txt", ""),
            project.SetField("map type", file.GetFileList_(v.LaunchAdr +"/modules/map/", "txt", "name", v.CE_MAPT)[0].
                replace(v.LaunchAdr + "/modules/map/", "").replace(".txt", ""),
            project.SetField("map file", MenuList.GetBaseText("Mapping file"),
            project.SetField("art compression", file.GetFileList_(v.LaunchAdr+"/modules/comp/"+ v.OS +"/", "txt", "name",
                v.CE_ARTC)[0].replace(v.LaunchAdr +"/modules/comp/"+ v.OS +"/", "").replace(".txt", ""),
            project.SetField("art type", file.GetFileList_(v.LaunchAdr +"/modules/tile/", "txt", "name", v.CE_ARTT)[0].
                replace(v.LaunchAdr + "/modules/tile/", "").replace(".txt", ""),
            project.SetField("art file", MenuList.GetBaseText("Art file"),
            project.SetField("trans off", ""+ Integer.parseInt(MenuList.GetBaseText("Transparent entry")),
            project.SetField("trans line", ""+ Integer.parseInt(MenuList.GetBaseText("Transparent line")),
            project.SetField("line offset", MenuList.GetBaseText("Palette line offset"),
            project.SetField("palette file", MenuList.GetBaseText("Palette file"),
            project.SetField("palette type", file.GetFileList_(v.LaunchAdr+"/modules/palette/", "txt", "name", v.CE_PALT)[0].
                replace(v.LaunchAdr + "/modules/palette/", "").replace(".txt", ""),
            project.SetField("name", MenuList.GetBaseText("Project name"),
                    ("SoniPlaneProject: "+ v.projversion +"\n").split("\n"))))))))))))))))), "\n");

        Event.SetEvent(Event.ReturnEvent(Event.E_PROJ_LOAD, Event.EP_MAX, MenuList.GetBaseText("Project file")));
    }

    private static int AddWarn(String ID, String text, int y, int height, EventHandler event) {
        WarnMenu.AddMenu(ID, text, 0, y, App.GetBounds().w, height, Style.GetStyle("menu_center"), 1f, event);
        return y + height;
    }

    public static void SetMenu(menu m){
        MenuList.destroy();     // make sure no traces are left
        MenuList = m;           // set new item
    }

    public static void ClearData() {
        MenuList.destroy();
        tileLoader.dispose();

        LogicList.clear();
        DrawQueue.clear();

        v.BlockControls = false;
        v.UnlockEndFrame = false;
        v.mode = 0;
    }

    public static void CreateGUI() {
        MenuBar m = new MenuBar();

        m.add(defMenu.GetMenu(defMenu.MENU_FILE));
        m.add(defMenu.GetMenu(defMenu.MENU_HELP));

        App.getJFrame().setMenuBar(m);
        ResizePrefs();
    }

    private static void ResizePrefs() {
        String[] d = FileUtil.readString(v.LaunchAdr + "/prefs.txt").split("\n");
        App.SetPosition(new bounds(Integer.parseInt(project.GetField("windowX", d)), Integer.parseInt(project.GetField("windowY", d)),
                Integer.parseInt(project.GetField("windowWidth", d)), Integer.parseInt(project.GetField("windowHeight", d))));
    }

    public static void SetNormalTitle() {
        try {
            App.SetTitle(project.GetField("title", new String(file.readFile(v.prefs)).split("\n")) +" beta "+ v.version);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void SettingsMenu() {
        String[] s;
        try {
            s = new String(file.readFile(v.prefs)).split("\n");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        EventHandler PREFR = new EventHandler(Event.E_PREFR, 0x10, null){
            @Override
            public void invoke(){
                SP.ResetPrefs();
                SP.ClearData();
                ResizePrefs();
                SP.SettingsMenu();
                SP.repaintLater();
            }
        };
        EventHandler PREFS = new EventHandler(Event.E_PREFS, 0x10, null){
            @Override
            public void invoke(){
                SP.SavePrefs();
                SP.ClearData();
                SP.CreateMainMenu();
            }
        };

        MenuList = new menu().AddAdjustButton("Program title", 0, 0, App.GetBounds().w, 20,
                project.GetField("title", s), Style.GetStyle("menu_left"), 1f, null, menu.AT_TEXT).

                AddAdjustButton("Window width", 0, 20, App.GetBounds().w / 2, 20, project.GetField("windowWidth", s),
                        Style.GetStyle("menu_left"), 1f, null, menu.AT_TEXT, "\\D").

                AddAdjustButton("Window height", App.GetBounds().w / 2, 20, App.GetBounds().w / 2, 20, project.GetField(
                        "windowHeight", s), Style.GetStyle("menu_left"), 1f, null, menu.AT_TEXT, "\\D").

                AddAdjustButton("Window X position", 0, 40, App.GetBounds().w / 2, 20, project.GetField("windowX", s),
                        Style.GetStyle("menu_left"), 1f, null, menu.AT_TEXT, "\\D").

                AddAdjustButton("Window Y position", App.GetBounds().w / 2, 40, App.GetBounds().w / 2, 20, project.GetField(
                        "windowY", s), Style.GetStyle("menu_left"), 1f, null, menu.AT_TEXT, "\\D").

                AddAdjustButton("Maximum autosave data (KB)", App.GetBounds().w / 2, 60, App.GetBounds().w / 2, 20,
                        ""+ (Long.parseLong(project.GetField("asMaxSpace", s)) / 1024),
                        Style.GetStyle("menu_left"), 1f, null, menu.AT_TEXT, "\\D").

                AddAdjustButton("Delay to check autosave usage (Minutes)", 0, 60, App.GetBounds().w / 2, 20,
                        ""+ (Long.parseLong(project.GetField("asUsageCheckDelay", s)) / 60000),
                        Style.GetStyle("menu_left"), 1f, null, menu.AT_TEXT, "\\D").

                AddCheckBox("Allow auto-removing autosave", 0, 80, App.GetBounds().w / 2, 20, Boolean.parseBoolean(
                        project.GetField("asAutoDelete",s)), Style.GetStyle("menu_left"), 1f, null).

                AddCheckBox("Allow usage of internet", App.GetBounds().w / 2, 80, App.GetBounds().w / 2, 20, Boolean.parseBoolean(
                        project.GetField("allowWebUtils",s)), Style.GetStyle("menu_left"), 1f, null).

                AddMenu("cancel", "Cancel", 0, 100, 80, 40, Style.GetStyle("menu_center"), 1f,
                        Event.ReturnEvent(Event.E_MENU, 0x10, null)).
                AddMenu("reset", "Reset", App.GetBounds().w - 80, 100, 80, 40, Style.GetStyle("menu_center"), 1f, PREFR).
                AddMenu("save", "Save", 80, 100, App.GetBounds().w - 160, 40, Style.GetStyle("menu_center"), 1f, PREFS);

        repaintLater();
    }

    public void render(Graphics g) {
        g.clearScreen(Color.BLACK);
        DrawDrawQueue(g);

        if (v.mode == Event.E_PROJ) {
            DrawDebug(g);
        }
    }

    private static void DrawDrawQueue(Graphics g) {
        ArrayList<Drawable> nowRender = (ArrayList<Drawable>) DrawQueue.clone();

        for(int p = v.RENDERPR_MIN;p <= v.RENDERPR_MAX && DrawQueue != null;p ++) {
            for (Drawable D : nowRender) {
                if(D.renderPriority() == p) {
                    D.draw(g);
                }
            }
        }
    }

    public void dispose(){
        Logic.cancel();
        while (runlogic);
        while(SaveThread != null && SaveThread.isAlive());

        try {
            file.delete(v.LaunchAdr +"/temp");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void resize(int width, int height){
        if(resizePref) {
            ResizePrefs();
            resizePref = false;
        }

        if(v.PlaneBounds != null) {
            v.setPlaneBounds();
            v.setTileListBounds();
            v.SetPalListBounds();
            v.SetPalChgBounds();
            v.SetTilEdBounds();
        }
    }

    public void focus(){

    }

    public void unfocus(){

    }

    public static void ResetPrefs() {
        file.saveFile(v.prefs,
            project.SetField("asAutoDelete", "true",
            project.SetField("asUsageCheckDelay", "300000",
            project.SetField("asMaxSpace", "10485760",
            project.SetField("allowWebUtils", "true",
            project.SetField("windowY", "0",
            project.SetField("windowX", "0",
            project.SetField("windowHeight", "720",
            project.SetField("windowWidth", "1080",
            project.SetField("title", "SoniPlane",
                ("version: " + v.prefversion + "\nreset: false\n").split("\r\n")))))))))), "\n");
    }

    public static void SavePrefs() {
        try {
            file.saveFile(v.prefs,
                project.SetField("asAutoDelete", MenuList.GetBaseText("Allow auto-removing autosave"),
                project.SetField("asUsageCheckDelay", ""+
                    (Long.parseLong(MenuList.GetBaseText("Delay to check autosave usage (Minutes)")) * 60000),
                project.SetField("asMaxSpace", ""+ (Long.parseLong(MenuList.GetBaseText("Maximum autosave data (KB)")) * 1024),
                project.SetField("allowWebUtils", MenuList.GetBaseText("Allow usage of internet"),
                project.SetField("windowY", MenuList.GetBaseText("Window Y position"),
                project.SetField("windowX", MenuList.GetBaseText("Window X position"),
                project.SetField("windowHeight", MenuList.GetBaseText("Window height"),
                project.SetField("windowWidth", MenuList.GetBaseText("Window width"),
                project.SetField("title", MenuList.GetBaseText("Program title"),
                    (new String(file.readFile(v.prefs))).split("\n")))))))))), "\n");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        ResizePrefs();
    }

    public static void exit() {
        App.exit(0);
    }

    public static void addToRenderList(Drawable draw) {
        DrawQueue.add(0, draw);
    }

    public static void rmvFromRenderList(Drawable draw) {
        DrawQueue.remove(draw);
    }

    public static void addToLogicList(Logicable logic) {
        LogicList.add(0, logic);
    }

    public static void rmvFromLogicList(Logicable logic) {
        LogicList.remove(logic);
    }

    public static void repaint() {
        App.repaint();
    }

    public static void repaintLater() {
        new Timer("RenderTimer (100ms) at "+ System.currentTimeMillis() / 1000).schedule(new TimerTask() {
            @Override
            public void run() {
                repaint();
            }
        }, 100);
    }

    public static menu GetMenuList() {
        return MenuList;
    }

    public static void startTileRender() {
        new Thread(new TileRenderer(), "TileRenderer").start();
    }

    private void DrawDebug(Graphics g) {
        DrawDebug(g, Style.GetStyle("debug"), Color.WHITE);
    }

    private void DrawDebug(Graphics g, StyleItem s, Color color) {
        try {
            g.setColor(color);
            Graphics.setFont(s.GetFont());
            String[] p = new String(file.readFile(v.project)).split("\n");

            switch (v.DrawDebug % 4) {
                case 0:
                    DrawDebug(g, "Mappings width: "+ v.mapSize.x +" - Mappings height: "+ v.mapSize.y +
                            " Entries: "+ mappings.GetMapArray().length, 0, 0, 15);
                    DrawDebug(g, "Selected palette: "+ v.PalLine +":"+ v.PalSelcted, 0, 15, 15);
                    DrawDebug(g, "Selected tile: "+ v.TileSelected +"/"+ tileLoader.GetTileArray().length, 0, 30, 15);
                    DrawDebug(g, "Draw plane: High: "+ v.DrawHighPlane +" Low: "+ v.DrawLowPlane, 0, 45, 15);

                    if(-(System.currentTimeMillis() - v.LastSave - v.AutoSave) > 60000){
                        DrawDebug(g, "Autosave in: "+ (-(System.currentTimeMillis() - v.LastSave - v.AutoSave) / 60000) +
                                " minutes", 0, 60, 15);
                    } else if(-(System.currentTimeMillis() - v.LastSave - v.AutoSave) >= 0){
                        DrawDebug(g, "Autosave in: "+ (-(System.currentTimeMillis() - v.LastSave - v.AutoSave) / 1000) +
                                " seconds", 0, 60, 15);
                    } else {
                        DrawDebug(g, "Autosave in: Never", 0, 60, 15);
                    }

                    if(v.SelBounds != null) {
                        DrawDebug(g, "Plane selection: x "+ (v.SelBounds.x / tileLoader.GetWidth()) +"-"+ ((v.SelBounds.x +
                                v.SelBounds.w) / tileLoader.GetWidth()) +" y "+ (v.SelBounds.y / tileLoader.GetHeight()) + "-"+
                                ((v.SelBounds.y + v.SelBounds.h) / tileLoader.GetHeight()), 0, 75, 15);
                    }
                    break;

                case 1:
                    DrawDebug(g, "Project name: "+ project.GetField("name",
                            new String(file.readFile(v.project)).split("\n")), 0, 0, 15);

                    DrawDebug(g, "Project file: "+ v.project, 0, 15, 15);
                    DrawDebug(g, "Project version: "+ new String(file.readFile(v.project)).split("\n")[0].
                            replace("SoniPlaneProject v", ""), 0, 30, 15);

                    DrawDebug(g, "Autosave delay: "+ v.AutoSave +"ms", 0, 45, 15);
                    DrawDebug(g, "Last autosave: "+ (v.LastSave / 1000) +" unix", 0, 60, 15);
                    DrawDebug(g, "TileDisplay tiles drawn: "+ tileDisp.drawn, 0, 75, 15);

                    DrawDebugNormal(g);
                    break;

                case 2:
                    DrawDebug(g, "Palette file: "+ project.GetField("palette file", new String(file.readFile(v.project)).
                            split("\n")), 0, 0, 15);

                    DrawDebug(g, "Palette type: "+ project.GetField("name", new String(file.readFile(v.LaunchAdr +"/modules/palette/" +
                                    project.GetField("palette type", p) + ".txt")).split("\n")), 0, 15, 15);

                    DrawDebug(g, "Total lines: "+ palette.getPalette().length +" - Entries per line: "+ palette.getPalette()[0].length,
                            0, 30, 15);

                    DrawDebug(g, "Transparent: "+ project.GetField("trans line", new String(file.readFile(v.project)).split("\n")) +
                            ":" + project.GetField("trans off", new String(file.readFile(v.project)).split("\n")), 0, 45, 15);

                    DrawDebugNormal(g);
                    break;

                case 3:
                    DrawDebug(g, "Art file: " + project.GetField("art file", new String(file.readFile(v.project)).split("\n")),
                            0, 0, 15);

                    DrawDebug(g, "Art type: "+ project.GetField("name", new String(file.readFile(v.LaunchAdr +"/modules/tile/"+
                                    project.GetField("art type", p) + ".txt")).split("\n")), 0, 15, 15);

                    DrawDebug(g, "Art compression: " + project.GetField("name", new String(file.readFile(
                            v.LaunchAdr +"/modules/comp/"+ v.OS +"/" + project.GetField("art compression",
                                    new String(file.readFile(v.project)).split("\n")) + ".txt")).split("\n")), 0, 30, 15);

                    DrawDebug(g, "Tile amount: " + tileLoader.GetTileArray().length, 0, 45, 15);

                    DrawDebugNormal(g);
                    break;

                case 4:
                    DrawDebug(g, "Mappings file: " + project.GetField("map file",
                            new String(file.readFile(v.project)).split("\n")), 0, 0, 15);

                    DrawDebug(g, "Mappings type: " + project.GetField("name", new String(file.readFile(v.LaunchAdr +"/modules/map/" +
                                    project.GetField("map type", p) + ".txt")).split("\n")), 0, 15, 15);

                    DrawDebug(g, "Mappings compression: " + project.GetField("name", new String(file.readFile(
                            v.LaunchAdr +"/modules/comp/"+ v.OS +"/"+ project.GetField("map compression",
                                    new String(file.readFile(v.project)).split("\n")) + ".txt")).split("\n")), 0, 30, 15);

                    DrawDebug(g, "Mappings width: " + v.mapSize.x + " - Mappings height: " + v.mapSize.y, 0, 45, 15);

                    DrawDebug(g, "Mappings offset: " + project.GetField("map offset",
                            new String(file.readFile(v.project)).split("\n")), 0, 60, 15);

                    DrawDebugNormal(g);
                    break;

                default:
                    v.DrawDebug = 0;
                    break;

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void DrawDebug(Graphics g, String text, int X, int Y, int Height) {
        int Width = (int) Graphics.GetTextWidth(text);

        Sprite spr = new Sprite();
        spr.setBounds(X, Y, Width, Height);
        spr.setColor(new Color(0, 0, 0, 0.5f));
        g.fillRect(spr);

        g.drawText(text, X, Y);
    }

    private void DrawDebugNormal(Graphics g) throws FileNotFoundException {
        int mem =  (int)(((double)Runtime.getRuntime().freeMemory() / (double)Runtime.getRuntime().totalMemory()) * 100f);
        int mem2 = (int)(((double)Runtime.getRuntime().totalMemory() / (double)Runtime.getRuntime().maxMemory()) * 100f);

        DrawDebug(g, "Allocated: " + mem2 + "% " + Runtime.getRuntime().totalMemory() / 1024 + "KB/" +
                Runtime.getRuntime().maxMemory() / 1024 + "KB", 0, App.GetBounds().h - 14, 15);

        DrawDebug(g, "Free memory: " + mem + "% " + Runtime.getRuntime().freeMemory() / 1024 + "KB/" +
                Runtime.getRuntime().totalMemory() / 1024 + "KB", 0, App.GetBounds().h - 29, 15);

        DrawDebug(g, "Java version: "+ System.getProperty("java.version") +" "+ System.getProperty("sun.arch.data.model") +
                "bit - Program version: "+ v.version, 0, App.GetBounds().h - 44, 15);

        DrawDebug(g, "(c) Green Snake (Natsumi) 2014", 0, App.GetBounds().h - 59, 15);

        SP.repaintLater();
    }

    private void CheckControl() {
        // cycle debug mode
        if(Keys.isPressed(KeyUtil.F3)){
            v.DrawDebug ++;
            repaintLater();
        }
        // change tile pallet line
        if(Keys.isPressed(KeyUtil.TAB)){
            v.PalLine ++;
            if(v.PalLine >= palette.getPalette().length){
                v.PalLine = 0;
            }

        } else if(Keys.isHeld(KeyUtil.CONTROL)){
            // save
            if(Keys.isPressed(KeyUtil.S)) {
                Event.SetEvent(Event.ReturnEvent(Event.E_SAVE, Event.EP_MAX, ""));

                // reload
            } else if(Keys.isPressed(KeyUtil.R)) {
                Event.SetEvent(Event.ReturnEvent(Event.E_PROJ_LOAD_, Event.EP_MAX, ""));

                // edit configuration
            } else if(Keys.isPressed(KeyUtil.E)) {
                Event.SetEvent(Event.ReturnEvent(Event.E_CONF, Event.EP_MAX, ""));

                // back to menu
            } else if(Keys.isPressed(KeyUtil.O)) {
                Event.SetEvent(Event.ReturnEvent(Event.E_MENU, Event.EP_MAX, ""));

                // draw high
            } else if(Keys.isPressed(KeyUtil.Q)) {
                v.DrawHighPlane ^= true;
                SP.repaintLater();

                // draw low
            } else if(Keys.isPressed(KeyUtil.W)) {
                v.DrawLowPlane ^= true;
                SP.repaintLater();

                // Plane size
            } else if(Keys.isPressed(KeyUtil.T)) {
                        defActList.PlaneSize();

            } else if(Keys.isPressed(KeyUtil.F9)){
                ResetProgram();
            }

            // null selection
        } else if(Keys.isPressed(KeyUtil.T)){
            v.SelBounds = null;
            Event.projectMenu();

            // increase selected tile index
        } else if(Keys.isPressed(KeyUtil.A)){
            v.TileSelected ++;
            repaintLater();

            // decrease selected tile index
        } else if(Keys.isPressed(KeyUtil.Z)){
            v.TileSelected --;
            repaintLater();

            // increase selected palette index
        } else if(Keys.isPressed(KeyUtil.S)){
            v.PalSelcted ++;
            if(v.PalSelcted >= palette.getPalette()[v.PalLine].length){
                v.PalSelcted = 0;
            }
            repaintLater();

            // decrease selected palette index
        } else if(Keys.isPressed(KeyUtil.X)){
            v.PalSelcted --;
            if(v.PalSelcted < 0){
                v.PalSelcted = palette.getPalette()[v.PalLine].length - 1;
            }
            repaintLater();

            // plane x ++
        } else if(Keys.isPressed(KeyUtil.NUM1)) {
            v.mapSize.x ++;
            defActList.MapSize();
            repaintLater();

            // plane x --
        } else if(Keys.isPressed(KeyUtil.NUM2)) {
            v.mapSize.x --;
            defActList.MapSize();
            repaintLater();

            // plane y ++
        } else if(Keys.isPressed(KeyUtil.NUM3)) {
            v.mapSize.y ++;
            defActList.MapSize();

            // plane y --
        } else if(Keys.isPressed(KeyUtil.NUM4)) {
            v.mapSize.y --;
            defActList.MapSize();

            // FlipY
        } else if(v.SelBounds != null) {

            if (Keys.isPressed(KeyUtil.F)) {
                mappings.TileFlip(true, false);
                repaintLater();

                // FlipX
            } else if (Keys.isPressed(KeyUtil.V)) {
                mappings.TileFlip(false, true);
                SP.repaintLater();

                // selection palette line increase
            } else if (Keys.isPressed(KeyUtil.G)) {
                mappings.PalIndex(1);
                repaintLater();

                // selection palette line decrease
            } else if (Keys.isPressed(KeyUtil.B)) {
                mappings.PalIndex(-1);
                repaintLater();

                // increase selection tile index
            } else if (Keys.isPressed(KeyUtil.H)) {
                mappings.TileIndex(1);
                repaintLater();

                // decrease selection tile index
            } else if (Keys.isPressed(KeyUtil.N)) {
                mappings.TileIndex(-1);
                repaintLater();

                // shift selection
            } else if (Keys.isPressed(KeyUtil.I) || Keys.isPressed(KeyUtil.UP_ARROW)) {
                mappings.ShiftMap(0, -1);
                SP.repaintLater();

                // shift selection
            } else if (Keys.isPressed(KeyUtil.K) || Keys.isPressed(KeyUtil.DOWN_ARROW)) {
                mappings.ShiftMap(0, 1);
                SP.repaintLater();

                // shift selection
            } else if (Keys.isPressed(KeyUtil.J) || Keys.isPressed(KeyUtil.LEFT_ARROW)) {
                mappings.ShiftMap(-1, 0);
                SP.repaintLater();

                // shift selection
            } else if (Keys.isPressed(KeyUtil.L) || Keys.isPressed(KeyUtil.RIGHT_ARROW)) {
                mappings.ShiftMap(1, 0);
                SP.repaintLater();

                // remove map
            } else if(Keys.isPressed(KeyUtil.Q)){
                mappings.Remove();
                Event.projectMenu();
                repaintLater();

                // clear selection
            } else if(Keys.isPressed(KeyUtil.W)) {
                mappings.Delete();
                repaintLater();

                // insert tiles
            } else if(Keys.isPressed(KeyUtil.E)) {
                mappings.Insert();
                repaintLater();

                // fill selection
            } else if(Keys.isPressed(KeyUtil.R)) {
                mappings.Fill(v.TileSelected, 0);
                repaintLater();

            }
        }
    }

    private void ResetProgram() {
        try {
            if(new File(v.LaunchAdr +"/autosave").exists()) {
                file.delete(v.LaunchAdr +"/autosave");
            }
            file.delete(v.prefs);

        } catch (IOException e) {
            e.printStackTrace();
        }
        exit();
    }
}
