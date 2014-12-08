package gs.soni.plane;

import gs.app.lib.application.App;
import gs.app.lib.gfx.Graphics;
import gs.app.lib.gfx.Sprite;
import gs.app.lib.math.bounds;
import gs.soni.plane.project.tileLoader;

public class v {
    public static boolean test = false;
    public static String LaunchAdr;
    public static String prefs;
    public static final String version =     "1.0.3";
    public static final String projversion = "1.0";
    public static final String prefversion = "1.0.3";
    public static final String updateAdr =   "http://discocentral.digibase.ca/SPP/update/";

    /* gets start of OS name. Win, Mac, Linus, SunOS or FreeBSD (some others exist, but fuck them, nobody uses anyway, right? RIGHT?) */
    public static final String OS = System.getProperty("os.name").split(" ")[0].replace("dows", "");

    public static final int RENDERPR_MIN = 0;
    public static final int RENDERPR_MAX = 8;
    public static int mode =               0;

    public static boolean BlockControls = false;
    public static boolean IsClicked =     false;
    public static boolean DrawHighPlane =  true;
    public static boolean DrawLowPlane =   true;
    public static boolean UnlockEndFrame = true;

    public static String project;
    public static String CE_ARTC;
    public static String CE_MAPC;
    public static String CE_PALT;
    public static String CE_ARTT;
    public static String CE_MAPT;

    public static int PalLine =              0;
    public static int DrawDebug =            0;
    public static int PalSelcted =           0;
    public static int TileSelected =        -1;
    public static int MapSelected =         -1;
    public static int PlaneMode =            0;
    public static int LineOff =              0;
    public static int FillIncr =             0;
    public static int TileRender =           Integer.MAX_VALUE;

    public static bounds PlaneBounds;
    public static bounds TileBounds;
    public static bounds PalBounds;
    public static bounds PalChgBounds;
    public static bounds TilEdBounds;
    public static bounds mapSize;

    public static bounds SelBounds;
    public static bounds SelStart;
    public static bounds SelEnd;

    public static long LastSave;
    public static long AutoSave;
    public static long LastSaveDel;
    public static long AutoSaveDel;
    public static long MaxASSize;

    public static void setPlaneBounds() {
        if(PlaneMode == 0) {
            int x = App.GetBounds().w - 4 - (mapSize.x * tileLoader.GetWidth()),
                    y = App.GetBounds().h - 4 - (mapSize.y * tileLoader.GetHeight());
            PlaneBounds = new bounds(x, y, App.GetBounds().w - 4 - x, App.GetBounds().h - 4 - y);

        } else if(PlaneMode == 1) {
            PlaneBounds = new bounds(4, App.GetBounds().h - (mapSize.y * tileLoader.GetHeight() * 2) - (8 * (tileLoader.GetHeight() + 2)) -
                    172, (mapSize.x * tileLoader.GetWidth() * 2), (mapSize.y * tileLoader.GetHeight() * 2));

        } else {
            PlaneBounds = new bounds(4, App.GetBounds().h - (mapSize.y * tileLoader.GetHeight() * 4) - (8 * (tileLoader.GetHeight() + 2)) -
                    172, (mapSize.x * tileLoader.GetWidth() * 4), (mapSize.y * tileLoader.GetHeight() * 4));
        }
    }

    public static void setTileListBounds() {
        if(PlaneMode == 0) {
            TileBounds = new bounds(4, PlaneBounds.y, PlaneBounds.x - 10, PlaneBounds.h);

        } else {
            int y = (8 * (tileLoader.GetHeight() + 2));
            TileBounds = new bounds(4, (App.GetBounds().h - 4) - y, App.GetBounds().w - 8, y);
        }
    }

    public static void SetPalListBounds() {
        if(PlaneMode == 0) {
            PalBounds = new bounds(PlaneBounds.x, PlaneBounds.y - 162, PlaneBounds.w, 156);

        } else {
            PalBounds = new bounds((App.GetBounds().w / 2) + 3, TileBounds.y - 162, (App.GetBounds().w / 2) - 7, 156);
        }
    }

    public static void SetPalChgBounds() {
        if(PlaneMode == 0) {
            PalChgBounds = new bounds(TileBounds.x, TileBounds.y - 162, TileBounds.w, 156);

        } else {
            PalChgBounds = new bounds(4, TileBounds.y - 162, (App.GetBounds().w / 2) - 7, 156);
        }
    }

    public static void SetTilEdBounds() {
        if(PlaneMode == 0) {
            int y = PalChgBounds.y - 6 - (tileLoader.GetHeight() * 16);
            TilEdBounds = new bounds(PalChgBounds.x, y, tileLoader.GetWidth() * 16, PalChgBounds.y - 6 - y);

        } else {
            int y = tileLoader.GetHeight() * 16;
            TilEdBounds = new bounds(PlaneBounds.w + 12, PalChgBounds.y - 6 - y, (tileLoader.GetWidth() * 16) + 6, y);
        }
    }

    public static void DrawBounds(Graphics g, Sprite s, bounds bounds, int Off, int thick, bounds draw){
        if(draw.y != 0) {
            s.setBounds(bounds.x - Off, bounds.y - Off, bounds.w + (Off * 2), thick);
            g.fillRect(s);
        }

        if(draw.x != 0) {
            s.setBounds(bounds.x - Off, bounds.y - Off, thick, bounds.h + (Off * 2));
            g.fillRect(s);
        }

        if(draw.h != 0) {
            s.setBounds(bounds.x + bounds.w + Off, bounds.y + bounds.h + Off, -bounds.w - (Off * 2), -thick);
            g.fillRect(s);
        }

        if(draw.w != 0) {
            s.setBounds(bounds.x + bounds.w + Off, bounds.y + bounds.h + Off, -thick, -bounds.h - (Off * 2));
            g.fillRect(s);
        }
    }

    public static void DrawBounds(Graphics g, Sprite s, bounds bounds, int Off, int thick) {
        s.setBounds(bounds.x - Off, bounds.y - Off, bounds.w + (Off * 2), thick);
        g.fillRect(s);
        s.setBounds(bounds.x - Off, bounds.y - Off, thick, bounds.h + (Off * 2));
        g.fillRect(s);
        s.setBounds(bounds.x + bounds.w + Off, bounds.y + bounds.h + Off, -bounds.w - (Off * 2), -thick);
        g.fillRect(s);
        s.setBounds(bounds.x + bounds.w + Off, bounds.y + bounds.h + Off, -thick, -bounds.h - (Off * 2));
        g.fillRect(s);
    }

    public static int GetSizeMultiplier() {
        int mul = 1;

        if(v.PlaneMode != 0){
            mul = v.PlaneMode * 2;
        }
        return mul;
    }

    public static void setBounds() {
        setPlaneBounds();
        setTileListBounds();
        SetPalListBounds();
        SetPalChgBounds();
        SetTilEdBounds();
    }
}
