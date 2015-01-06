package gs.soni.plane;

import gs.app.lib.gfx.Graphics;
import gs.app.lib.gfx.Sprite;
import gs.app.lib.gfx.gfx;
import gs.app.lib.math.bounds;
import gs.soni.plane.util.Colors;

import java.awt.*;
import java.awt.image.BufferedImage;

public class v {
    /* flag to switch to testing mode or off. Used to not redirect output to run.txt when testing */
    public static boolean test = false;
    /* describes the location the application was launched from */
    public static String LaunchAdr;
    /* is the direct address for preferences file.
     * Used to convenience and future possibility of making preferences read from universal address */
    public static String prefs;
    /* version number constant */
    public static final String version =     "1.1.1";
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
    /* check if high plane will be drawn (soon to be obsolete) */
    public static boolean DrawHighPlane =  true;
    /* check if low plane will be drawn (soon to be obsolete) */
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

    /* palette line offset */
    public static int LineOff =              0;
    /* minimum and maximum priority for rendering */
    public static final int RENDERPR_MIN =   0;
    public static final int RENDERPR_MAX =   8;
    /* program mode */
    public static int mode =                 0;

    /* mappings size */
    public static bounds mapSize;

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

    /* few images used in Windows */
    public static BufferedImage[] corner;
    public static BufferedImage[] slope;

    /* method to render special images used in the program */
    public static void renderImages(){
        corner = new BufferedImage[]{
                imgReplace(gfx.getImage(v.LaunchAdr +"/res/window-corner.png"), Color.WHITE, Colors.GetColor("window-normal")),
                imgReplace(gfx.getImage(v.LaunchAdr +"/res/window-corner.png"), Color.WHITE, Colors.GetColor("window-focus")),
                imgReplace(gfx.getImage(v.LaunchAdr +"/res/window-corner.png"), Color.WHITE, Colors.GetColor("window-normal2")),
                imgReplace(gfx.getImage(v.LaunchAdr +"/res/window-corner.png"), Color.WHITE, Colors.GetColor("window-focus2")),};

        slope = new BufferedImage[]{
                imgReplace(gfx.getImage(v.LaunchAdr +"/res/window-end.png"), Color.WHITE, Colors.GetColor("window-normal")),
                imgReplace(gfx.getImage(v.LaunchAdr +"/res/window-end.png"), Color.WHITE, Colors.GetColor("window-focus")),};
    }

    /* method used by above to replace each instance of color to another color */
    private static BufferedImage imgReplace(BufferedImage image, Color from, Color to) {
        /* loop for each pixel of the image */
        for(int y = 0;y < image.getHeight();y ++){
            for(int x = 0;x < image.getWidth();x ++){
                /* if RGB values match, replace with new RGB value */
                if(image.getRGB(x, y) == from.getRGB()){
                    image.setRGB(x, y, to.getRGB());
                }
            }
        }

        return image;
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
}
