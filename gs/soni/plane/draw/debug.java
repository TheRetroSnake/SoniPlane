package gs.soni.plane.draw;

import gs.app.lib.application.App;
import gs.app.lib.gfx.Graphics;
import gs.app.lib.gfx.Sprite;
import gs.app.lib.math.bounds;
import gs.app.lib.util.KeyUtil;
import gs.soni.plane.SP;
import gs.soni.plane.project.*;
import gs.soni.plane.util.Keys;
import gs.soni.plane.util.Style;
import gs.soni.plane.util.StyleItem;
import gs.soni.plane.util.file;
import gs.soni.plane.v;

import java.awt.*;
import java.io.FileNotFoundException;

public class debug implements Window, PanelListener {
    private int draw = 0;
    private long last = 0;

    @Override
    public bounds getBounds() {
        return new bounds(0, 1, App.GetBounds().w, App.GetBounds().h);
    }

    @Override
    public void draw(Graphics g, bounds b, float a) {
        SP.getWM().movePanel(this, 0);
        DrawDebug(g, Style.GetStyle("debug"), Color.WHITE);
    }

    @Override
    public void logic() {
        /* cycle debug mode */
        if(Keys.isPressed(KeyUtil.F3)){
            draw ++;
            SP.getWM().getPanelManager(this).repaint();
            SP.repaintLater();

        /* redraw */
        } else if(Keys.isPressed(KeyUtil.F4)){
            SP.getWM().getPanelManager(this).repaint();
            SP.repaintLater();
        }

        /* check if we need to redraw for autosave */
        if(draw == 1) {
            long time = -(System.currentTimeMillis() - v.LastSave - v.AutoSave);
            if (time > 60000) {
                if(last != (int)(time / 60000)){
                    last = time / 60000;
                    SP.getWM().getPanelManager(this).repaint();
                    SP.repaintLater();
                }

            } else if (time >= 0) {
                if(last != time){
                    last = time;
                    SP.getWM().getPanelManager(this).repaint();
                    SP.repaintLater();
                }
            }
        }
    }

    @Override
    public void create() {
        SP.getWM().getPanelManager(this).addListener(this);
        SP.getWM().getPanelManager(this).setBackground(new Color(0, 0, 0, 0));
    }

    @Override
    public boolean canUnFocus() {
        return false;
    }

    @Override
    public boolean drawBound() {
        return false;
    }

    @Override
    public boolean cursorOverride() {
        return false;
    }

    @Override
    public void defaultSize() {

    }

    private void DrawDebug(Graphics g, StyleItem s, Color color) {
        try {
            g.setColor(color);
            Graphics.setFont(s.GetFont());
            String[] p = new String(file.readFile(v.project)).split("\n");

            switch (draw % 5) {
                case 1:
                    DrawDebug(g, "Mappings width: "+ v.mapSize.x +" - Mappings height: "+ v.mapSize.y +
                            " Entries: "+ mappings.GetMapArray().length, 0, 0, 15);
                    DrawDebug(g, "Selected palette: "+ v.PalLine +":"+ v.PalSelcted, 0, 15, 15);
                    DrawDebug(g, "Selected tile: "+ v.TileSelected +"-"+ v.TileSelectedEnd +"/"+
                            tileLoader.GetTileArray().length, 0, 30, 15);
                    DrawDebug(g, "Draw plane: High: "+ v.DrawHighPlane +" Low: "+ v.DrawLowPlane, 0, 45, 15);

                    if(-(System.currentTimeMillis() - v.LastSave - v.AutoSave) > 60000){
                        DrawDebug(g, "Autosave in: "+ (-(System.currentTimeMillis() - v.LastSave - v.AutoSave) / 60000) +
                                " minutes", 0, 60, 15);

                    } else if(-(System.currentTimeMillis() - v.LastSave - v.AutoSave) >= 0){
                        DrawDebug(g, "Autosave in: "+ (-(System.currentTimeMillis() - v.LastSave - v.AutoSave) / 1000) +
                                " seconds", 0, 60, 15);

                    } else {
                        DrawDebug(g, "Autosave in: Never", 0, 60, 15);
                    }

                    if(v.SelBounds != null) {
                        DrawDebug(g, "Plane selection: x "+ (v.SelBounds.x / tileLoader.GetWidth()) +"-"+ ((v.SelBounds.x +
                                v.SelBounds.w) / tileLoader.GetWidth()) +" y "+ (v.SelBounds.y / tileLoader.GetHeight()) + "-"+
                                ((v.SelBounds.y + v.SelBounds.h) / tileLoader.GetHeight()), 0, 75, 15);

                        if((v.SelBounds.w / tileLoader.GetWidth()) <= 1 && (v.SelBounds.h / tileLoader.GetWidth()) <= 1){
                            int off = ((v.SelBounds.y / tileLoader.GetWidth()) * v.mapSize.x) + (v.SelBounds.x / tileLoader.GetWidth());
                            if (off >= mappings.GetMapArray().length) {
                                break;
                            }

                            map n = mappings.GetMapArray()[off];
                            DrawDebug(g, "Map info: Tile ID: "+ n.tileOff +" - Pal line: "+ n.palLine +" - Is high plane: "+
                                    n.HighPlane +" - Flip: x "+ n.XFlip +" - y "+ n.YFlip, 0, 90, 15);
                        } else {
                            GetSelData(g);
                        }
                    }

                    DrawDebugNormal(g);
                    break;

                case 2:
                    DrawDebug(g, "Project name: "+ project.GetField("name", p), 0, 0, 15);

                    DrawDebug(g, "Project file: "+ v.project, 0, 15, 15);
                    DrawDebug(g, "Project version: "+ p[0].replace("SoniPlaneProject: ", ""), 0, 30, 15);
                    DrawDebug(g, "Autosave delay: "+ v.AutoSave +"ms", 0, 45, 15);
                    DrawDebug(g, "Last autosave: "+ (v.LastSave / 1000) +" unix", 0, 60, 15);
                    DrawDebug(g, "TileDisplay tiles drawn: "+ tileDisp.drawn, 0, 75, 15);
                    break;

                case 3:
                    DrawDebug(g, "Palette file: "+ project.GetField("palette file", p), 0, 0, 15);
                    DrawDebug(g, "Palette type: "+ project.GetField("name", new String(file.readFile(v.LaunchAdr +"/modules/palette/" +
                            project.GetField("palette type", p) + ".txt")).split("\n")), 0, 15, 15);

                    DrawDebug(g, "Total lines: "+ gs.soni.plane.project.palette.getPalette().length +" - Entries per line: "+
                            gs.soni.plane.project.palette.getPalette()[0].length, 0, 30, 15);

                    DrawDebug(g, "Transparent: "+ project.GetField("trans line", p) +":" + project.GetField("trans off", p), 0, 45, 15);
                    break;

                case 4:
                    DrawDebug(g, "Art file: " + project.GetField("art file", p), 0, 0, 15);
                    DrawDebug(g, "Art type: "+ project.GetField("name", new String(file.readFile(v.LaunchAdr +"/modules/tile/"+
                            project.GetField("art type", p) + ".txt")).split("\n")), 0, 15, 15);

                    DrawDebug(g, "Art compression: " + project.GetField("name", new String(file.readFile( v.LaunchAdr +"/modules/comp/"+
                            v.OS +"/" + project.GetField("art compression", p) + ".txt")).split("\n")), 0, 30, 15);

                    DrawDebug(g, "Tile amount: " + tileLoader.GetTileArray().length, 0, 45, 15);
                    break;

                case 5:
                    DrawDebug(g, "Mappings file: " + project.GetField("map file", p), 0, 0, 15);
                    DrawDebug(g, "Mappings type: " + project.GetField("name", new String(file.readFile(v.LaunchAdr +"/modules/map/" +
                            project.GetField("map type", p) + ".txt")).split("\n")), 0, 15, 15);

                    DrawDebug(g, "Mappings compression: " + project.GetField("name", new String(file.readFile(v.LaunchAdr +
                            "/modules/comp/"+ v.OS +"/"+ project.GetField("map compression", p) + ".txt")).split("\n")), 0, 30, 15);

                    DrawDebug(g, "Mappings width: "+ v.mapSize.x +" - Mappings height: "+ v.mapSize.y, 0, 45, 15);
                    DrawDebug(g, "Mappings offset: " + project.GetField("map offset", p), 0, 60, 15);
                    break;

                case 0:
                    break;

                default:
                    draw = 0;
                    break;

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void GetSelData(Graphics g) {
        int w = tileLoader.GetWidth(), h = tileLoader.GetHeight(), tileStart = mappings.GetMapArray().length, tileEnd = 0,
                palLineStart = gs.soni.plane.project.palette.getPalette().length, palLineEnd = 0;
        boolean highPlaneTrue = false, highPlaneFalse = false,
                xFlipTrue = false, xFlipFalse = false, yFlipTrue = false, yFlipFalse = false;

        for (int o = 0; o < mappings.GetMapArray().length;o ++) {

            int x_ = o % v.mapSize.x, y_ = o / v.mapSize.x;
            if (mappings.isInside(new bounds((x_ * w), (y_ * h), 0, 0), v.SelBounds)) {
                map n = mappings.GetMapArray()[o];

                if(n.tileOff > tileEnd){
                    tileEnd = n.tileOff;
                }

                if(n.tileOff < tileStart){
                    tileStart = n.tileOff;
                }

                if(n.palLine > palLineEnd){
                    palLineEnd = n.palLine;
                }

                if(n.palLine < palLineStart){
                    palLineStart = n.palLine;
                }

                if(!highPlaneTrue)  highPlaneTrue = n.HighPlane;
                if(!highPlaneFalse) highPlaneFalse = !n.HighPlane;

                if(!xFlipTrue)  xFlipTrue = n.XFlip;
                if(!xFlipFalse) xFlipFalse = !n.XFlip;
                if(!yFlipTrue)  yFlipTrue = n.YFlip;
                if(!yFlipFalse) yFlipFalse = !n.YFlip;
            }
        }

        DrawDebug(g, "Map info: Tile ID: "+ tileStart +"-"+ tileEnd +" - Pal line: "+ palLineStart +"-"+ palLineEnd +
                " - Is high plane: "+ GetDual(highPlaneTrue, highPlaneFalse) +" - Flip: x "+ GetDual(xFlipTrue, xFlipFalse)
                +" - y "+ GetDual(yFlipTrue, yFlipFalse), 0, 90, 15);
    }

    private String GetDual(boolean t, boolean f) {
        return (t ? "T" : "") + (f ? "F" : "");
    }

    private void DrawDebug(Graphics g, String text, int X, int Y, int Height) {
        int Width = (int) Graphics.GetTextWidth(text);

        Sprite spr = new Sprite();
        spr.setBounds(X, Y, Width, Height);
        spr.setColor(Color.BLACK);
        g.fillRect(spr);

        g.drawText(text, X, Y);
    }

    private void DrawDebugNormal(Graphics g) throws FileNotFoundException {
        DrawDebug(g, "(c) Green Snake (Natsumi) 2014", 0, App.GetBounds().h - 16, 15);
    }

    @Override
    public void close() {

    }

    @Override
    public void draw() {

    }

    @Override
    public void logic(boolean focus) {
        logic();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void move(int x, int y) {

    }
}
