package gs.soni.plane.project;

import gs.soni.plane.util.Event;
import gs.soni.plane.util.file;
import gs.soni.plane.v;

import java.io.FileNotFoundException;

public class ProjectLoader implements Runnable {

    @Override
    public void run() {
        try {
            v.AutoSave = Long.parseLong(project.GetField("autosave", new String(file.readFile(v.project)).split("\n")));
            if(Boolean.parseBoolean(project.GetField("asAutoDelete", new String(file.readFile(v.prefs)).split("\n")))) {

                v.AutoSaveDel = Long.parseLong(project.GetField("asUsageCheckDelay", new String(file.readFile(v.prefs)).split("\n")));
                v.MaxASSize = Long.parseLong(project.GetField("asMaxSpace", new String(file.readFile(v.prefs)).split("\n")));
            }

            tileLoader.SetTileArray(tileLoader.GetTiles());
            mappings.SetMapArray(mappings.GetMap());

            palette.LoadPalette();      // + ability to choose palette line offset (default 0 to allow v0.1)
            v.LineOff = Integer.parseInt(project.GetField("line offset", new String(file.readFile(v.project)).split("\n"), "0"));

            v.mapSize = mappings.GetSize();
            mappings.SetMapOffset(Integer.parseInt(project.GetField("map offset", new String(file.readFile(v.project)).split("\n"))));

            tileLoader.render();
            v.setPlaneBounds();
            v.setTileListBounds();
            v.SetPalListBounds();
            v.SetPalChgBounds();
            v.SetTilEdBounds();

            Event.ClearEvent();
            Event.SetEvent(Event.ReturnEvent(Event.E_PROJ, 0x10, ""));

        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
    }
}
