package gs.soni.plane.project;

import gs.soni.plane.util.file;
import gs.soni.plane.v;

import java.io.File;
import java.io.FileNotFoundException;

public class Save implements Runnable {
    private final String name;
    private final boolean autoSave;

    public Save(String name, boolean isAutoSve) {
        this.name = v.LaunchAdr +"/autosave/"+ name;
        autoSave = isAutoSve;
    }

    @Override
    public void run() {
        mappings.Optimize();
        if(autoSave){
            AutoSave();
        } else {
            try {
                BackUp();
                tileLoader.Save(project.GetField("art file", new String(file.readFile(v.project)).split("\n")));
                mappings.Save(project.GetField("map file", new String(file.readFile(v.project)).split("\n")));
                palette.Save(project.GetField("palette file", new String(file.readFile(v.project)).split("\n")));

                file.saveFile(v.project, project.SetField("map height", (v.mapSize.y + ""), project.SetField("map width", v.mapSize.x + "",
                                new String(file.readFile(v.project)).split("\n"))), "\n");

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void BackUp() {
        new File(name +"/").mkdir();     // make directory for data
        try {
            file.saveFile(name +".SPP", project.SetField("switch", v.project,
                project.SetField("map height", v.mapSize.y + "",
                project.SetField("map width", v.mapSize.x + "",
                project.SetField("palette file", name + "/pal",
                project.SetField("map file", name + "/map", project.SetField("art file", name + "/art",
                    new String(file.readFile(v.project)).split("\n"))))))), "\n");

            String[] d = new String(file.readFile(v.project)).split("\n");
            file.copyfile(project.GetField("palette file", d), name +"/pal");
            file.copyfile(project.GetField("map file", d), name +"/map");
            file.copyfile(project.GetField("art file", d), name +"/art");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void AutoSave() {
        new File(name +"/").mkdir();     // make directory for data
        try {
            file.saveFile(name +".SPP", project.SetField("switch", v.project,
                project.SetField("map height", v.mapSize.y + "",
                project.SetField("map width", v.mapSize.x + "",
                project.SetField("palette file", name + "/pal",
                project.SetField("map file", name + "/map", project.SetField("art file", name + "/art",
                    new String(file.readFile(v.project)).split("\n"))))))), "\n");

            String[] d = new String(file.readFile(name +".SPP")).split("\n");
            palette.Save(project.GetField("palette file", d));
            mappings.Save(project.GetField("map file", d));
            tileLoader.Save(project.GetField("art file", d));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
