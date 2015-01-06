package gs.soni.plane.web;

import gs.app.lib.application.App;
import gs.app.lib.util.FileUtil;
import gs.soni.plane.SP;
import gs.soni.plane.menu.menu;
import gs.soni.plane.project.project;
import gs.soni.plane.util.*;
import gs.soni.plane.util.Event;
import gs.soni.plane.v;

import java.awt.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class UpdateChecker implements Runnable {
    @Override
    public void run() {
        try {
            /* get update files */
            String[] update = new String(Util.download(Util.GetConnection(v.updateAdr + "latest.txt"))).split("\n");

            /* check if SoniPlane is up to date */
            if(!project.GetField("SoniPlaneVersion", update).equals(v.version)){
                /* if not, notify to use downloader */
                WaitForMenu();
                SP.GetMenuList().SetMenuText("checkupdate", "Use Downloader to update");

            /* check if there is updates available */
            } else if(CheckFiles(update)){
                /* tell updates are available */
                WaitForMenu();
                SP.GetMenuList().SetMenuText("checkupdate", "Updates available");

            /* tell we are up to date */
            } else {
                WaitForMenu();
                SP.GetMenuList().SetMenuText("checkupdate", "Up to date");
            }

        } catch (IOException e) {
            e.printStackTrace();
            /* tell failed to check for updates */
            WaitForMenu();
            SP.GetMenuList().SetMenuText("checkupdate", "Could not check for updates");

        }

        /* set the cursor to DEFAULT cursor when done */
        App.getJPanel().setCursor(CursorList.get(Cursor.DEFAULT_CURSOR));
        SP.repaintLater();
    }

    /* wait for the menu to "exist" first */
    private void WaitForMenu() {
        /* while menu doesn't exist, wait */
        while (SP.GetMenuList() == null || SP.GetMenuList().GetMenuID("checkupdate") == -1){
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            /* if mode is not 0, go away */
            if(v.mode != 0){
                return;
            }
        }
    }

    /* check if there is files to update */
    private boolean CheckFiles(String[] list) throws FileNotFoundException {
        for(String a : list){
            /* if the file actually exists, and versions match */
            if(FileUtil.exists(v.LaunchAdr +"/"+ a.split(": ")[0].replace("%OS%", v.OS)) &&
                    !project.GetField("version", project.getFields(v.LaunchAdr +"/"+ a.split(": ")[0].replace("%OS%", v.OS))).
                            equals(a.split(": ")[1].split(">")[0])){

                return true;
            }
        }
        return false;
    }

    /* create the menu for updating */
    public static void doMenu() {
        try {
            /* get the updates */
            SP.SetMenu(GetUpdates());
            /* add cancel button */
            SP.GetMenuList().AddMenu("cancel", "Cancel", 0, App.GetBounds().h - 40, App.GetBounds().w / 2, 40,
                    Style.GetStyle("menu_center"), 1f, Event.ReturnEvent(Event.E_MENU, 0x10, null));
            /* add install button */
            SP.GetMenuList().AddMenu("install", "Install", App.GetBounds().w / 2, App.GetBounds().h - 40, App.GetBounds().w / 2, 40,
                    Style.GetStyle("menu_center"), 1f, new EventHandler(Event.E_CE_DONE, Event.EP_MAX - 2, null){
                        @Override
                        public void invoke(){
                            try {
                                /* get the update data */
                                String[] data = GetUpdateData();
                                /* create the new menu for all the functions */
                                SP.SetMenu(new menu().AddMenu("down", "Downloading updates", 0, 0, menu.GetScreenSize(), 40,
                                                Style.GetStyle("menu_center"), 1f, null).

                                        AddMenu("amount", "0/"+ data.length, 0, 40, menu.GetScreenSize(), 40,
                                                Style.GetStyle("menu_center"), 1f, null).

                                        AddMenu("loaded", "Downloaded 0 bytes", 0, 80, menu.GetScreenSize(), 40,
                                                Style.GetStyle("menu_center"), 1f, null).

                                        AddMenu("update", "", 0, 120, menu.GetScreenSize(), 40,
                                                Style.GetStyle("menu_center"), 1f, null));

                                /* create new updater thread and repaint */
                                new Thread(new UpdateDownloader(data), "Updater").start();
                                SP.repaintLater();

                            } catch (IOException e) {
                                e.printStackTrace();
                                Event.SetEvent(Event.ReturnEvent(Event.E_MENU, 0x10, null));
                            }
                        }
                    });

        } catch (IOException e) {
            e.printStackTrace();
        }

        /* repaint */
        SP.repaintLater();
    }

    /* get update menu */
    private static menu GetUpdates() throws IOException {
        /* set position, the new menu and its style */
        int y = 0;
        menu r = new menu();
        StyleItem style = Style.GetStyle("menu_center");

        /* download the files and loop for all of them */
        for(String a : new String(Util.download(Util.GetConnection(v.updateAdr + "latest.txt"))).split("\n")){
            /* if file exists */
            if(FileUtil.exists(v.LaunchAdr +"/"+ a.split(": ")[0].replace("%OS%", v.OS))){

                /* get version number */
                String version = project.GetField("version",
                        project.getFields(v.LaunchAdr +"/"+ a.split(": ")[0].replace("%OS%", v.OS)));
                /* if version is not the same as current version */
                if(!version.equals(a.split(": ")[1].split(">")[0])) {

                    /* add the new menu and increase y position */
                    r.AddMenu(a, a.split(": ")[0].replace("%OS%", v.OS) +" from "+ version +" to "+ a.split(": ")[1].split(">")[0],
                            0, y, menu.GetScreenSize(), 20, style, 1f, null);
                    y += 20;
                }
            }
        }

        /* return the new menu */
        return r;
    }

    /* get files to update */
    private static String[] GetUpdateData() throws IOException {
        ArrayList<String> s = new ArrayList<String>();

        /* get the files and lop for each entry */
        for(String a : new String(Util.download(Util.GetConnection(v.updateAdr + "latest.txt"))).split("\n")){
            /* if file exists and the versions don't match */
            if(FileUtil.exists(v.LaunchAdr +"/"+ a.split(": ")[0].replace("%OS%", v.OS)) &&
                    !project.GetField("version", project.getFields(v.LaunchAdr +"/"+ a.split(": ")[0].replace("%OS%", v.OS))).
                            equals(a.split(": ")[1].split(">")[0])){

                /* add the file to list */
                s.add(a.split(": ")[0].replace("%OS%", v.OS));

                /* check for all the extra files */
                for(String f : a.split(": ")[1].split(">")){
                    /* if the file exists, add to the list */
                    if(FileUtil.exists(f)){
                        s.add(f);
                    }
                }
            }
        }

        /* return list */
        return s.toArray(new String[s.size()]);
    }
}
