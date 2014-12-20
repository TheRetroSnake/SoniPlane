package gs.soni.plane;

import gs.app.lib.application.App;
import gs.app.lib.gfx.Graphics;
import gs.app.lib.gfx.Sprite;
import gs.app.lib.math.bounds;
import gs.soni.plane.project.tileLoader;

public class v {
    /* flag to switch to testing mode or off. Used to not redirect output to run.txt when testing */
    public static boolean test = false;
    /* describes the location the application was launched from */
    public static String LaunchAdr;
    /* is the direct address for preferences file.
     * Used to convenience and future possibility of making preferences read from universal address */
    public static String prefs;
    /* version number constant */
    public static final String version =     "1.0.4";
    /* project version constant */
    public static final String projversion = "1.0";
    /* preferences version constant */
    public static final String prefversion = "1.0.3";
    /* web address offset to search updates from */
    public static final String updateAdr =   "http://discocentral.digibase.ca/SPP/update/";

    /* gets start of OS name. Win, Mac, Linus, SunOS or FreeBSD (some others exist, but fuck them, nobody uses anyway, right? RIGHT?) */
    public static final String OS = System.getProperty("os.name").split(" ")[0].replace("dows", "");

    /* if set to true, some menu items wont be activated when pressed */
    public static boolean BlockControls = false;
    /* is used to check if mouse buttons were clicked */
    public static boolean IsClicked =     false;
    /* check if high plane will be drawn */
    public static boolean DrawHighPlane =  true;
    /* check if low plane will be drawn */
    public static boolean DrawLowPlane =   true;
    /* check to set BlockControls flag to false after executing all logic */
    public static boolean UnlockEndFrame = true;

    /* direct address for project constant */
    public static String project;
    /* variables for dropdown menus of project edit screen */
    public static String CE_ARTC;
    public static String CE_MAPC;
    public static String CE_PALT;
    public static String CE_ARTT;
    public static String CE_MAPT;

    /* current palette line */
    public static int PalLine =              0;
    /* current selected palette */
    public static int PalSelcted =           0;
    /* current selected tile */
    public static int TileSelected =        -1;
    /* current tile selection end */
    public static int TileSelectedEnd =     -1;
    /* current mapping selected */
    public static int MapSelected =         -1;
    /* increment to fille using selection fill */
    public static int FillIncr =             0;

    /* offset of tile renderer */
    public static int TileRender =           Integer.MAX_VALUE;
    /* mode of how the planes are arranged */
    public static int PlaneMode =            0;
    /* palette line offset */
    public static int LineOff =              0;
    /* minimum and maximum priority for rendering */
    public static final int RENDERPR_MIN =   0;
    public static final int RENDERPR_MAX =   8;
    /* program mode */
    public static int mode =                 0;

    /* bounds objects for different editors (obsolete soon) */
    public static bounds PlaneBounds;
    public static bounds TileBounds;
    public static bounds PalBounds;
    public static bounds PalChgBounds;
    public static bounds TilEdBounds;
    public static bounds mapSize;

    /* bounds for plane selection (soon to be obsolete) */
    public static bounds SelBounds;
    public static bounds SelStart;
    public static bounds SelEnd;

    /* last time autosaved */
    public static long LastSave;
    /* autosave delay */
    public static long AutoSave;
    /* last save deletion */
    public static long LastSaveDel;
    /* autosave deletion delay */
    public static long AutoSaveDel;
    /* max size of backup */
    public static long MaxASSize;

    /* soon to be obsolete methods of calculating right sizes for plane editing windows */
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

    /* methods to draw boundaries around area */
    public static void DrawBounds(Graphics g, Sprite s, bounds bounds, int Off, int thick, bounds draw){
        /* hackish way to draw all the 4 sides with checks whether sides should be drawn */
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
        /* hackish way to draw all the 4 sides */
        s.setBounds(bounds.x - Off, bounds.y - Off, bounds.w + (Off * 2), thick);
        g.fillRect(s);
        s.setBounds(bounds.x - Off, bounds.y - Off, thick, bounds.h + (Off * 2));
        g.fillRect(s);
        s.setBounds(bounds.x + bounds.w + Off, bounds.y + bounds.h + Off, -bounds.w - (Off * 2), -thick);
        g.fillRect(s);
        s.setBounds(bounds.x + bounds.w + Off, bounds.y + bounds.h + Off, -thick, -bounds.h - (Off * 2));
        g.fillRect(s);
    }

    /* soon to be obsolete method to calculate plane size multiplier */
    public static int GetSizeMultiplier() {
        int mul = 1;

        if(v.PlaneMode != 0){
            mul = v.PlaneMode * 2;
        }
        return mul;
    }

    /* soon to be obsolete method to set boundaries for plane editing windows */
    public static void setBounds() {
        setPlaneBounds();
        setTileListBounds();
        SetPalListBounds();
        SetPalChgBounds();
        SetTilEdBounds();
    }
}
