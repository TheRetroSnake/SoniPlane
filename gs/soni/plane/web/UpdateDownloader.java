package gs.soni.plane.web;

import gs.app.lib.util.FileUtil;
import gs.soni.plane.SP;
import gs.soni.plane.menu.menu;
import gs.soni.plane.util.Event;
import gs.soni.plane.util.Style;
import gs.soni.plane.v;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class UpdateDownloader implements Runnable {
    private final String[] UpdateData;
    private long DownBytes = 0;

    public UpdateDownloader(String[] data) {
        UpdateData = data;
    }

    @Override
    public void run() {
        int amount = 0;
        for(String f : UpdateData){
            SP.GetMenuList().SetMenuText("update", f);
            SP.repaintLater();

            try {
                DownloadAndSave(Util.GetConnection(v.updateAdr + f), v.LaunchAdr + "/" + f);
            } catch (IOException e) {
                e.printStackTrace();
                SP.GetMenuList().SetMenuText("update", "Unable to download or save "+ f);
            }

            amount ++;
            SP.GetMenuList().SetMenuText("amount", amount +"/"+ UpdateData.length);
        }

        SP.GetMenuList().destroy();
        SP.SetMenu(new menu().AddMenu("down", "Downloading updates", 0, 0, menu.GetScreenSize(), 40,
                Style.GetStyle("menu_center"), 1f, null).

                AddMenu("amount", amount +"/"+ UpdateData.length, 0, 40, menu.GetScreenSize(), 40,
                        Style.GetStyle("menu_center"), 1f, null).

                AddMenu("loaded", "Downloaded "+ DownBytes +" bytes", 0, 80, menu.GetScreenSize(), 40,
                        Style.GetStyle("menu_center"), 1f, null).

                AddMenu("update", "Done", 0, 120, menu.GetScreenSize(), 40,
                        Style.GetStyle("menu_center"), 1f, Event.ReturnEvent(Event.E_MENU, 0x10, null)));
    }

    private void DownloadAndSave(URLConnection conn, String file) throws IOException {
        InputStream in = new BufferedInputStream(conn.getInputStream());
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        byte[] buf = new byte[1024];
        int n;
        long len = conn.getContentLengthLong();

        while (-1 != (n = in.read(buf))){
            DownBytes += n;
            SP.GetMenuList().SetMenuText("loaded", "Downloaded "+ Util.GetBytes(DownBytes) +" out of "+ Util.GetBytes(len));
            out.write(buf, 0, n);
        }

        out.close();
        in.close();
        FileUtil.writeBytes(file, out.toByteArray(), false);
    }


}
