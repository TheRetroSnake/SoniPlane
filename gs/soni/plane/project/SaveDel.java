package gs.soni.plane.project;

import gs.soni.plane.util.file;
import gs.soni.plane.v;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class SaveDel implements Runnable {
    private long toSpace;

    /* copy the new space amount */
    public SaveDel(long toSpace) {
        this.toSpace = toSpace;
    }

    @Override
    public void run() {
        /* get autosaves */
        String[] files = file.GetFileList_(v.LaunchAdr +"/autosave", "SPP");
        String[] f = file.GetFileList_(v.LaunchAdr +"/autosave", "SPP");

        /* fix autosave names to automatically sort */
        for(int i = 0;i < files.length;i ++){
            files[i] = files[i].replace("bk", "").replace("as", "").replace(v.LaunchAdr +"/autosave/", "");
        }

        /* sort files */
        Arrays.sort(files);

        /* while the files take up larger amount of space than the set max */
        while(file.getFolderSize(v.LaunchAdr +"/autosave") > toSpace){
            for (String F : f) {
                /* if filename contains the next entry in the file array, and is not a savestate */
                if (F.contains(files[0]) && !F.contains("st")) {
                    try {
                        /* delete the file and its data */
                        file.delete(F);
                        file.delete(F.replace(".SPP", ""));

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            /* if we don't have more files to cycle, exit */
            if(files.length == 1){
                break;
            }

            /* remove the first file */
            files = Arrays.copyOfRange(files, 1, files.length);
        }
    }
}
