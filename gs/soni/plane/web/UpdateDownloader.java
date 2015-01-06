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
import java.net.URLConnection;

public class UpdateDownloader implements Runnable {
    /* all files to update */
    private final String[] UpdateData;
    /* total of downloaded bytes */
    private long DownBytes = 0;

    /* get the new data */
    public UpdateDownloader(String[] data) {
        UpdateData = data;
    }

    @Override
    public void run() {
        /* amount of files downloaded */
        int amount = 0;
        for(String f : UpdateData){
            /* set this file to update */
            SP.GetMenuList().SetMenuText("update", f);

            try {
                /* download and save the file */
                DownloadAndSave(Util.GetConnection(v.updateAdr + f), v.LaunchAdr + "/" + f);
            } catch (IOException e) {
                e.printStackTrace();
                /* tell we failed to save */
                SP.GetMenuList().SetMenuText("update", "Unable to download or save "+ f);
            }

            amount ++;
            /* set amount of files downloaded */
            SP.GetMenuList().SetMenuText("amount", amount +"/"+ UpdateData.length);
        }

        /* destroy old menu and create new */
        SP.GetMenuList().destroy();
        SP.SetMenu(new menu().AddMenu("down", "Downloading updates", 0, 0, menu.GetScreenSize(), 40,
                Style.GetStyle("menu_center"), 1f, null).

                AddMenu("amount", amount +"/"+ UpdateData.length, 0, 40, menu.GetScreenSize(), 40,
                        Style.GetStyle("menu_center"), 1f, null).

                AddMenu("loaded", "Downloaded "+ Util.GetBytes(DownBytes) +" bytes", 0, 80, menu.GetScreenSize(), 40,
                        Style.GetStyle("menu_center"), 1f, null).

                AddMenu("update", "Done", 0, 120, menu.GetScreenSize(), 40,
                        Style.GetStyle("menu_center"), 1f, Event.ReturnEvent(Event.E_MENU, 0x10, null)));
        /* repaint */
        SP.repaint();
    }

    /* download and safe a file */
    private void DownloadAndSave(URLConnection conn, String file) throws IOException {
        /* get new inputStream */
        InputStream in = new BufferedInputStream(conn.getInputStream());
        /* get a new outputStream */
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int d = 0;

        /* create new buffer for file */
        byte[] buf = new byte[1024];
        int n;
        /* get file length */
        long len = conn.getContentLengthLong();

        /* loop until file is downloaded */
        while (-1 != (n = in.read(buf))){
            /* increase size counter */
            d += n;
            /* tell file size */
            SP.GetMenuList().SetMenuText("loaded", "Downloaded "+ Util.GetBytes(d) +" out of "+ Util.GetBytes(len));
            /* write to the buffer */
            out.write(buf, 0, n);
            /* repaint the menu */
            SP.repaint();
        }

        /* increase downloaded bytes */
        DownBytes += d;
        /* close streams */
        out.close();
        in.close();
        /* write the file */
        FileUtil.writeBytes(file, out.toByteArray(), false);
    }
}
