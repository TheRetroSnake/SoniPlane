package gs.soni.plane.project;

import gs.soni.plane.util.file;
import gs.soni.plane.v;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class SaveDel implements Runnable {
    private long toSpace;

    public SaveDel(long toSpace) {
        this.toSpace = toSpace;
    }

    @Override
    public void run() {
        String[] files = file.GetFileList_(v.LaunchAdr +"/autosave", "SPP");
        String[] f = file.GetFileList_(v.LaunchAdr +"/autosave", "SPP");

        for(int i = 0;i < files.length;i ++){
            files[i] = files[i].replace("bk", "").replace("as", "").replace(v.LaunchAdr +"/autosave/", "");
        }

        Arrays.sort(files);

        while(file.getFolderSize(v.LaunchAdr +"/autosave") > toSpace){
            for(int i = 0;i < f.length;i ++){
                if(f[i].contains(files[0])){
                    try {
                        new File(f[i]).delete();
                        file.delete(f[i].replace(".SPP", ""));

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            files = Arrays.copyOfRange(files, 1, files.length);
        }
    }
}
