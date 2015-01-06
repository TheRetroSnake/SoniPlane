package gs.soni.plane.web;

import gs.app.lib.application.App;
import gs.soni.plane.SP;
import gs.soni.plane.menu.menu;
import gs.soni.plane.project.project;
import gs.soni.plane.util.*;
import gs.soni.plane.util.Event;
import gs.soni.plane.v;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class UpdateChecker implements Runnable {
    @Override
    public void run() {
        try {
            String[] update = new String(Util.download(Util.GetConnection(v.updateAdr + "latest.txt"))).split("\n");

            if(!project.GetField("SoniPlaneVersion", update).equals(v.version)){
                WaitForMenu();
                SP.GetMenuList().SetMenuText("checkupdate", "Use Downloader to update");

            } else if(CheckFiles(update)){
                WaitForMenu();
                SP.GetMenuList().SetMenuText("checkupdate", "Updates available");

            } else {
                WaitForMenu();
                SP.GetMenuList().SetMenuText("checkupdate", "Up to date");
            }

        } catch (IOException e) {
            e.printStackTrace();
            WaitForMenu();
            SP.GetMenuList().SetMenuText("checkupdate", "Could not check for updates");

        }

        /* set the cursor to DEFAULT cursor when done */
        App.getJPanel().setCursor(CursorList.get(Cursor.DEFAULT_CURSOR));
        SP.repaintLater();
    }

    private void WaitForMenu() {
        while (SP.GetMenuList() == null || SP.GetMenuList().GetMenuID("checkupdate") == -1){
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if(v.mode != 0){
                return;
            }
        }
    }

    private boolean CheckFiles(String[] list) throws FileNotFoundException {
        for(String a : list){
            if(new File(v.LaunchAdr +"/"+ a.split(": ")[0].replace("%OS%", v.OS)).exists() && !project.GetField("version",
                    new String(file.readFile(v.LaunchAdr +"/"+ a.split(": ")[0].replace("%OS%", v.OS))).
                            split("\n")).equals(a.split(": ")[1])){

                return true;
            }
        }
        return false;
    }

    public static void doMenu() {
        try {
            SP.SetMenu(GetUpdates());
            SP.GetMenuList().AddMenu("cancel", "Cancel", 0, App.GetBounds().h - 40, App.GetBounds().w / 2, 40,
                    Style.GetStyle("menu_center"), 1f, Event.ReturnEvent(Event.E_MENU, 0x10, null));
            SP.GetMenuList().AddMenu("install", "Install", App.GetBounds().w / 2, App.GetBounds().h - 40, App.GetBounds().w / 2, 40,
                    Style.GetStyle("menu_center"), 1f, new EventHandler(Event.E_CE_DONE, Event.EP_MAX - 2, null){
                        @Override
                        public void invoke(){
                            try {
                                String[] data = GetUpdateData();
                                SP.SetMenu(new menu().AddMenu("down", "Downloading updates", 0, 0, menu.GetScreenSize(), 40,
                                                Style.GetStyle("menu_center"), 1f, null).

                                        AddMenu("amount", "0/"+ data.length, 0, 40, menu.GetScreenSize(), 40,
                                                Style.GetStyle("menu_center"), 1f, null).

                                        AddMenu("loaded", "Downloaded 0 bytes", 0, 80, menu.GetScreenSize(), 40,
                                                Style.GetStyle("menu_center"), 1f, null).

                                        AddMenu("update", "", 0, 120, menu.GetScreenSize(), 40,
                                                Style.GetStyle("menu_center"), 1f, null));

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

        SP.repaintLater();
    }

    private static menu GetUpdates() throws IOException {
        int y = 0;
        menu r = new menu();
        StyleItem style = Style.GetStyle("menu_center");

        for(String a : new String(Util.download(Util.GetConnection(v.updateAdr + "latest.txt"))).split("\n")){
            if(new File(v.LaunchAdr +"/"+ a.split(": ")[0].replace("%OS%", v.OS)).exists()){

                String version = project.GetField("version", new String(file.readFile(v.LaunchAdr +"/"+ a.split(": ")[0].
                        replace("%OS%", v.OS))).split("\n"));
                if(!version.equals(a.split(": ")[1])) {

                    r.AddMenu(a, a.split(": ")[0].replace("%OS%", v.OS) +" from "+ version +" to "+ a.split(": ")[1],
                            0, y, menu.GetScreenSize(), 20, style, 1f, null);
                    y += 20;
                }
            }
        }

        return r;
    }

    private static String[] GetUpdateData() throws IOException {
        ArrayList<String> s = new ArrayList<String>();

        for(String a : new String(Util.download(Util.GetConnection(v.updateAdr + "latest.txt"))).split("\n")){
            if(new File(v.LaunchAdr +"/"+ a.split(": ")[0].replace("%OS%", v.OS)).exists() && !project.GetField("version",
                    new String(file.readFile(v.LaunchAdr +"/"+ a.split(": ")[0].replace("%OS%", v.OS))).
                            split("\n")).equals(a.split(": ")[1])){

                s.add(a.split(": ")[0].replace("%OS%", v.OS));
            }
        }

        return s.toArray(new String[s.size()]);
    }
}
