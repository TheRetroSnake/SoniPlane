package gs.soni.plane.util;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;

public class FileOpen implements Runnable {
    private String dir;
    private String desc;
    private String ext;
    private EventHandler event;

    public FileOpen(EventHandler event, String extension, String description, String dir) {
        this.event = event;
        this.dir = dir;
        ext = extension;
        desc = description;
    }

    @Override
    public void run() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Select file");

        if(!ext.equals("")) {
            fc.setFileFilter(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    return f.isDirectory() || f.getName().toUpperCase().endsWith(ext);
                }

                @Override
                public String getDescription() {
                    return desc;
                }
            });
        }

        fc.setDragEnabled(true);
        fc.setCurrentDirectory(new File(dir));

        int r = fc.showOpenDialog(null);
        if(r == JFileChooser.APPROVE_OPTION) {

            String file = fc.getSelectedFile().getAbsolutePath();
            if(!file.endsWith(ext)){
                file += ext;
            }

            event.setString(file);
            Event.SetEvent(event);
        }
    }
}

