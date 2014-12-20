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
    /* quick boolean check to reset program preferences when needed */
    private boolean resetPrefs;
    /* draw and logic queues */
    private static ArrayList<Drawable> DrawQueue;
    private static ArrayList<Logicable> LogicList;
    /* timer to call logic at interval rate */
    private static Timer Logic;
    /* menu list for all program menus */
    private static menu MenuList;
    /* special menu list when warning users for incorrect information in project creation/editing */
    private static menu WarnMenu;
    /* thread for autosave, update checking and saving project */
    public static Thread SaveThread;
    /* quick boolean to resize screen after preferences change */
    private static boolean resizePref = false;
    /* flag used to make sure logic is not ran while exiting program
     * (makes sure the program wont close without issues and no damage to files) */
    private static boolean runlogic;

    /* initialize the program */
    public SP(String[] arg, boolean resetPref){
        /* copy if we should reset preferences */
        resetPrefs = resetPref;

        /* if more than 1 argument */
        if(arg.length >= 2){
            /* if we should open a project */
            if(arg[0].equals("-open")){
                Event.SetEvent(Event.ReturnEvent(Event.E_PROJ_LOAD, Event.EP_MAX, arg[1]));

            /* if we should reconfigure a project */
            } else if(arg[0].equals("-edit")){
                v.project = arg[1];
                Event.SetEvent(Event.ReturnEvent(Event.E_CONF, Event.EP_MAX, ""));

            /* if we should create a new project */
            } else if(arg[0].equals("-new")){
                v.project = arg[1];
                Event.SetEvent(Event.ReturnEvent(Event.E_CONF, Event.EP_MAX, ""));
            }
        /* if argument length is more than 0 and argument and argument 0 is "-new"
         * create a new project */
        } else if(arg.length > 0 && arg[0].equals("-new")) {
            v.project = "";
            Event.SetEvent(Event.ReturnEvent(Event.E_CONF, Event.EP_MAX, ""));
        }
    }

    /* get windowManager if one is in logicable list */
    public static windowManager getWM() {
        /* run for all logicable targets */
        for(Logicable L : LogicList.toArray(new Logicable[LogicList.size()])){
            /* if logicable is instance of windowManager */
            if(L instanceof windowManager){
                /* return the windowManager */
                return (windowManager) L;
            }
        }

        /* no windowManager is present; return null instead */
        return null;
    }

    /* create the program */
    public void create(){
        /* get launching address */
        v.LaunchAdr = FileUtil.getJarFolder().replace("\\", "/");
        /* does the folder contain SoniPlane.jar (aka make sure we don't open in C:/Windows/System32 or similar incorrect folders */
        if(file.IsRightFolder(v.LaunchAdr)) {
            /* save preferences address to memory */
            v.prefs = v.LaunchAdr +"/prefs.txt";
            /* create queues */
            DrawQueue = new ArrayList<Drawable>();
            LogicList = new ArrayList<Logicable>();

            /* create folders used in the program */
            FileUtil.mkdir(v.LaunchAdr +"/temp");
            FileUtil.mkdir(v.LaunchAdr +"/projects");
            FileUtil.mkdir(v.LaunchAdr +"/autosave");

            /* if should, reset the preferences */
            if (resetPrefs) {
                ResetPrefs();
            }

            /* replace all /r/n sequences with /n */
            try {
                project.RemoveRN();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            /* create the updateChecker */
            SaveThread = new Thread(new UpdateChecker(), "UpdateChecker");
            SaveThread.start();
            resizePref = true;

            /* set program title */
            SetNormalTitle();
            /* create the GUI for the program */
            CreateGUI();
            /* set display icon */
            SetWindowIcon();
            /* create main menu */
            CreateMainMenu();

            /* create a new timer for logic stuff */
            Logic = new Timer("LogicTimer");
            Logic.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    /* set running logic to true */
                    runlogic = true;

                    /* cycle through logicable objects and run their logic */
                    for (Logicable L : LogicList.toArray(new Logicable[LogicList.size()])) {
                        L.logic();
                    }

                    /* handle events and get is clicked status */
                    v.IsClicked = Mouse.IsClicked(true);
                    Event.HandleEvent();

                    /* check if BlockControls should be set back to false */
                    if (v.UnlockEndFrame && !v.IsClicked) {
                        v.BlockControls = false;
                        v.UnlockEndFrame = false;
                    }

                    /* check controls (to be set to obsolete soon) */
                    if (v.mode == Event.E_PROJ) {
                        CheckControl();
                    }

                    /* clear pressed buttons */
                    MouseUtil.clearPresses();
                    KeyUtil.clearPresses();

                    /* check autosaving and removing autosaves if too much saves */
                    if (v.mode == Event.E_PROJ) {
                        autoSave();
                        autoSaveClean();
                    }
                    /* set logic to be finished */
                    runlogic = false;
                }
            }, 1, 66);
        }
    }

    /* set icon for the program */
    private void SetWindowIcon() {
        App.getJFrame().setIconImage(gfx.getImage(v.LaunchAdr +"/res/logo.png"));
    }

    /* clean autosaves if too much autosaves exist */
    private void autoSaveClean() {
        /* if we should check right now */
        if(v.AutoSaveDel > 0 && System.currentTimeMillis() - v.LastSaveDel > v.AutoSaveDel){
            /* if too much data is stored */
            if(file.getFolderSize(v.LaunchAdr +"/autosave") > v.MaxASSize){

                /* wait until savethread is not running */
                while(SaveThread != null && SaveThread.isAlive());
                /* start new thread which will delete saves */
                SaveThread = new Thread(new SaveDel(v.MaxASSize / 2), "SaveDeleter");
                SaveThread.start();
                /* update the last time we deleted saves */
                v.LastSaveDel = System.currentTimeMillis();
            }
        }
    }

    /* autosave the project */
    private void autoSave() {
        /* check if we should save right now */
        if(v.AutoSave > 0 && System.currentTimeMillis() - v.LastSave > v.AutoSave){
            try {
                /* wait until save thread stopped */
                while(SaveThread != null && SaveThread.isAlive());
                /* make a new thread to save current progress */
                SaveThread = new Thread(new Save("as"+ (System.currentTimeMillis() / 1000) +"-"+
                        project.GetField("name", new String(file.readFile(v.project)).split("\n")), true), "AutoSave");
                SaveThread.start();
                /* update the last time we checked saves */
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

                    AddProjMenu("Transparent line", "0", "\\D", 0, 60, App.GetBounds().w / 2, 20,
                            Style.GetStyle("menu_left"), 1f, null, 0xC0000000).

                    AddProjMenu("Transparent entry", "0", "\\D", App.GetBounds().w / 2, 60, App.GetBounds().w / 2, 20,
                             Style.GetStyle("menu_left"), 1f, null, 0xC0000000).

                    AddProjMenu("Palette line offset", "0", "\\D", 0, 80, App.GetBounds().w / 2, 20,
                            Style.GetStyle("menu_left"), 1f, null, 0xE0000000).

                    AddProjMenu("Art file", "", 0, 100, menu.GetScreenSize(), 20, Style.GetStyle("menu_left"), 1f,
                            new EventHandler[]{null, Event.ReturnEvent(Event.E_CE_FILE, Event.EP_MAX, "Art file")}, 0xC0000004).

                    CompDropDown("Art compression", 0, 120, App.GetBounds().w / 2, 20, Style.GetStyle("menu_left"), 1f, E_CE_ARTC).
                    AddProjMenu("Mapping file", "", 0, 140, menu.GetScreenSize(), 20, Style.GetStyle("menu_left"), 1f,
                            new EventHandler[]{null, Event.ReturnEvent(Event.E_CE_FILE, Event.EP_MAX, "Mapping file")}, 0xC0000004).

                    CompDropDown("Mapping compression", 0, 160, App.GetBounds().w / 2, 20, Style.GetStyle("menu_left"), 1f, E_CE_MAPC).
                    AddProjMenu("Plane Width", "0", "\\D", App.GetBounds().w / 2, 180, App.GetBounds().w / 2, 20,
                             Style.GetStyle("menu_left"), 1f, null, 0xC0000000).

                    AddProjMenu("Plane Height", "0", "\\D", 0, 180, App.GetBounds().w / 2, 20,
                            Style.GetStyle("menu_left"), 1f, null, 0xC0000000).

                    AddProjMenu("Map offset", "0", "\\D",  App.GetBounds().w / 2, 200, App.GetBounds().w / 2, 20,
                            Style.GetStyle("menu_left"), 1f, null, 0xE0000000).

                    AddProjMenu("Autosave delay (Minutes)", "0", "\\D", 0, 200, App.GetBounds().w / 2, 20,
                            Style.GetStyle("menu_left"), 1f, null, 0xC0000000).

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
                            Event.loading();

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

                        AddProjMenu("Transparent line", project.GetField("trans line", s), "\\D", 0, 60, App.GetBounds().w / 2, 20,
                                Style.GetStyle("menu_left"), 1f, null, 0xC0000000).

                        AddProjMenu("Transparent entry", project.GetField("trans off", s), "\\D",
                                App.GetBounds().w / 2, 60, App.GetBounds().w / 2, 20, Style.GetStyle("menu_left"), 1f, null, 0xC0000000).

                        AddProjMenu("Palette line offset", project.GetField("trans off", s, "0"), "\\D",
                                0, 80, App.GetBounds().w / 2, 20, Style.GetStyle("menu_left"), 1f, null, 0xE0000000).

                        AddProjMenu("Art file", project.GetField("art file", s), 0, 100, menu.GetScreenSize(), 20,
                                Style.GetStyle("menu_left"), 1f, new EventHandler[]{
                                        null, Event.ReturnEvent(Event.E_CE_FILE, Event.EP_MAX, "Art file")}, 0xC0000004).

                        CompDropDown("Art compression", 0, 120, App.GetBounds().w / 2, 20, Style.GetStyle("menu_left"), 1f, E_CE_ARTC).
                        AddProjMenu("Mapping file", project.GetField("map file", s), 0, 140, menu.GetScreenSize(), 20,
                                Style.GetStyle("menu_left"), 1f, new EventHandler[]{
                                        null, Event.ReturnEvent(Event.E_CE_FILE, Event.EP_MAX, "Mapping file")}, 0xC0000004).

                        CompDropDown("Mapping compression", 0, 160, App.GetBounds().w / 2, 20, Style.GetStyle("menu_left"), 1f, E_CE_MAPC).

                        AddProjMenu("Plane Width", project.GetField("map width", s), "\\D",
                                App.GetBounds().w / 2, 180, App.GetBounds().w / 2, 20, Style.GetStyle("menu_left"), 1f, null, 0xC0000000).

                        AddProjMenu("Plane Height", project.GetField("map height", s), "\\D",
                                0, 180, App.GetBounds().w / 2, 20, Style.GetStyle("menu_left"), 1f, null, 0xC0000000).

                        AddProjMenu("Map offset", project.GetField("map offset", s), "\\D", App.GetBounds().w / 2, 200,
                                App.GetBounds().w / 2, 20, Style.GetStyle("menu_left"), 1f, null, 0xE0000000).

                        AddProjMenu("Autosave delay (Minutes)", ""+ (Long.parseLong(project.GetField("autosave", s)) / 60000), "\\D",
                                0, 200, App.GetBounds().w / 2, 20,  Style.GetStyle("menu_left"), 1f, null, 0xC0000000).

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

        if(MenuList.GetBaseText("Autosave delay (Minutes)").replaceAll("\\D", "").equals("")){
            y = AddWarn(""+ y, "Autosave delay must be a number!", y, height, null);
        }
        if(MenuList.GetBaseText("Map offset").replaceAll("\\D", "").equals("")){
            y = AddWarn(""+ y, "Map offset must be a number!", y, height, null);
        }
        if(MenuList.GetBaseText("Plane Width").replaceAll("\\D", "").equals("")){
            y = AddWarn(""+ y, "Plane Width must be a number!", y, height, null);
        }
        if(MenuList.GetBaseText("Plane Height").replaceAll("\\D", "").equals("")){
            y = AddWarn(""+ y, "Plane Height must be a number!", y, height, null);
        }
        if(MenuList.GetBaseText("Transparent entry").replaceAll("\\D", "").equals("")){
            y = AddWarn(""+ y, "Transparent entry must be a number!", y, height, null);
        }
        if(MenuList.GetBaseText("Transparent line").replaceAll("\\D", "").equals("")){
            y = AddWarn(""+ y, "Transparent line must be a number!", y, height, null);
        }
        if(MenuList.GetBaseText("Palette file").replaceAll("\\D", "").equals("")){
            y = AddWarn(""+ y, "Palette file must be a number!", y, height, null);
        }

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

        MenuList = new menu().AddProjMenu("Program title", project.GetField("title", s), 0, 0, App.GetBounds().w, 20,
                Style.GetStyle("menu_left"), 1f, null, 0xC0000000).

                AddProjMenu("Window width", project.GetField("windowWidth", s), "\\D",
                        0, 20, App.GetBounds().w / 2, 20, Style.GetStyle("menu_left"), 1f, null, 0xC0000000).

                AddProjMenu("Window height", project.GetField("windowHeight", s), "\\D",
                        App.GetBounds().w / 2, 20, App.GetBounds().w / 2, 20, Style.GetStyle("menu_left"), 1f, null, 0xC0000000).

                AddProjMenu("Window X position", project.GetField("windowX", s), "\\D", 0, 40, App.GetBounds().w / 2, 20,
                        Style.GetStyle("menu_left"), 1f, null, 0xC0000000).

                AddProjMenu("Window Y position",project.GetField("windowY", s), "\\D",
                        App.GetBounds().w / 2, 40, App.GetBounds().w / 2, 20, Style.GetStyle("menu_left"), 1f, null, 0xC0000000).

                AddProjMenu("Maximum autosave data (KB)", ""+ (Long.parseLong(project.GetField("asMaxSpace", s)) / 1024), "\\D",
                        App.GetBounds().w / 2, 60, App.GetBounds().w / 2, 20, Style.GetStyle("menu_left"), 1f, null, 0xC0000000).

                AddProjMenu("Autosave Delete Delay (Minutes)",""+ (Long.parseLong(project.GetField("asUsageCheckDelay", s)) / 60000),"\\D",
                        0, 60, App.GetBounds().w / 2, 20, Style.GetStyle("menu_left"), 1f, null, 0xC0000000).

                AddCheckBox("Allow auto-removing autosave", 0, 80, App.GetBounds().w / 2, 20,
                        Boolean.parseBoolean(project.GetField("asAutoDelete",s)), Style.GetStyle("menu_left"), 1f, null).

                AddCheckBox("Allow usage of internet", App.GetBounds().w / 2, 80, App.GetBounds().w / 2, 20,
                        Boolean.parseBoolean(project.GetField("allowWebUtils",s)), Style.GetStyle("menu_left"), 1f, null).

                AddMenu("cancel", "Cancel", 0, 100, 80, 40, Style.GetStyle("menu_center"), 1f, Event.ReturnEvent(Event.E_MENU, 0x1, null)).
                AddMenu("reset", "Reset", App.GetBounds().w - 80, 100, 80, 40, Style.GetStyle("menu_center"), 1f, PREFR).
                AddMenu("save", "Save", 80, 100, App.GetBounds().w - 160, 40, Style.GetStyle("menu_center"), 1f, PREFS);

        repaintLater();
    }

    public void render(Graphics g) {
        g.clearScreen(Color.BLACK);
        DrawDrawQueue(g);
    }

    private static void DrawDrawQueue(Graphics g) {
        Drawable[] nowRender = DrawQueue.toArray(new Drawable[DrawQueue.size()]);

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
        while(runlogic);
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
            project.SetField("chooserDir", System.getProperty("user.home"),
            project.SetField("asAutoDelete", "true",
            project.SetField("asUsageCheckDelay", "300000",
            project.SetField("asMaxSpace", "10485760",
            project.SetField("allowWebUtils", "true",
            project.SetField("windowY", "0",
            project.SetField("windowX", "0",
            project.SetField("windowHeight", "720",
            project.SetField("windowWidth", "1080",
            project.SetField("title", "SoniPlane",
                ("version: "+ v.prefversion +"\nreset: false").split("\r\n"))))))))))), "\n");
    }

    public static void SavePrefs() {
        try {
            file.saveFile(v.prefs,
                project.SetField("asAutoDelete", MenuList.GetBaseText("Allow auto-removing autosave"),
                project.SetField("asUsageCheckDelay", ""+
                    (Long.parseLong(MenuList.GetBaseText("Autosave Delete Delay (Minutes)")) * 60000),
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

    private void CheckControl() {
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
                mappings.Delete(v.SelBounds);
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

    /* remove autosaves, reset preferences and exit */
    private void ResetProgram() {
        try {
            /* if autosave folder exists, remove its contents */
            if(new File(v.LaunchAdr +"/autosave").exists()) {
                file.delete(v.LaunchAdr +"/autosave");
            }

            /* delete preferences */
            file.delete(v.prefs);

        } catch (IOException e) {
            e.printStackTrace();
        }

        /* exit program */
        exit();
    }
}
