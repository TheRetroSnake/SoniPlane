package gs.soni.plane.web;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

public class Util {
    public static byte[] download(URLConnection conn) throws IOException {
        InputStream in = new BufferedInputStream(conn.getInputStream());
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        byte[] buf = new byte[1024];
        int n;

        while (-1 != (n = in.read(buf))){
            out.write(buf, 0, n);
        }

        out.close();
        in.close();
        return out.toByteArray();
    }

    public static URLConnection GetConnection(String adr) throws IOException {
        return new URL(adr).openConnection();
    }

    public static String GetBytes(long size) {
        if(size >= 1048576){
            return String.format("%.02f", ((float)size / 1048576)) +" MegaBytes";
        }  if(size >= 1024){
            return String.format("%.02f", ((float)size / 1024)) +" KiloBytes";
        }

        return size +" Bytes";
    }
}
