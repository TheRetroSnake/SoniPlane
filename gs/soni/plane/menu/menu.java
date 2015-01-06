package gs.soni.plane.menu;

import gs.app.lib.gfx.Graphics;
import gs.app.lib.math.bounds;
import gs.soni.plane.SP;
import gs.soni.plane.draw.Drawable;
import gs.soni.plane.project.project;
import gs.soni.plane.util.*;
import gs.soni.plane.v;

import java.io.FileNotFoundException;
import java.util.Arrays;

// Menu-related code
public class menu implements Drawable, Logicable {

    public static final int AT_NONE = 0;
    public static final int AT_UD =   1;
    public static final int AT_UDLR = 2;
    public static final int AT_TEXT = 0x100;
    public static final int AT_NEG =  0x200;
    public static final int AT_BTN =  0x400;
    public static final int AT_TMIN = 0x800;

    private MenuEntry[] menus;

    private static final int C_STYLESIZE =  0x80000000;
    private static final int C_SCREENSIZE = 0x40000000;
    private static final int C_AFTER =      0x20000000;

    public static final int COMMANDBITS = 0xFF000000;
    public static final int NORMALBITS =  0xFFFFFF;

    public menu(){
        menus = new MenuEntry[0];
        SP.addToLogicList(this);
        SP.addToRenderList(this);
    }

    public menu AddMenu(String ID, String text, int x, int y, int width, int height, StyleItem style,
                        float alpha, EventHandler Event) {

        if(CheckIDUsed(ID)){
            System.out.println("Warning: Menu ID \""+ ID +"\" already used!");
            return this;
        }

        MenuEntry[] temp = menus;
        menus = new MenuEntry[temp.length + 1];

        System.arraycopy(temp, 0, menus, 0, temp.length);
        menus[temp.length] = new MenuEntry(ID, text, x, y, width, height, style, alpha, Event);
        return this;
    }

    public menu AddProjMenu(String ID, String text, int x, int y, int width, int height, StyleItem style,
                            float alpha, EventHandler Event[], int btns) {

        if(CheckIDUsed(ID)){
            System.out.println("Warning: Menu ID \""+ ID +"\" already used!");
            return this;
        }

        MenuEntry[] temp = menus;
        menus = new MenuEntry[temp.length + 1];

        System.arraycopy(temp, 0, menus, 0, temp.length);
        if(btns == 0) {
            menus[temp.length] = new MenuEntry(ID, text, x, y, width, height, style, alpha, Event[0]);
        } else {
            menus[temp.length] = new ProjEntry(ID, text, x, y, width, height, style, alpha, Event, btns);
        }
        return this;
    }

    public menu AddProjMenu(String ID, String text, String regex, int x, int y, int width, int height, StyleItem style,
                            float alpha, EventHandler Event[], int btns) {

        if(CheckIDUsed(ID)){
            System.out.println("Warning: Menu ID \""+ ID +"\" already used!");
            return this;
        }

        MenuEntry[] temp = menus;
        menus = new MenuEntry[temp.length + 1];

        System.arraycopy(temp, 0, menus, 0, temp.length);
        if(btns == 0) {
            menus[temp.length] = new MenuEntry(ID, text, x, y, width, height, style, alpha, Event[0]);
        } else {
            menus[temp.length] = new ProjEntry(ID, text, regex, x, y, width, height, style, alpha, Event, btns);
        }
        return this;
    }

    private boolean CheckIDUsed(String ID) {
        for(MenuEntry m : menus){
            if(m.GetID().equals(ID)){
                return true;
            }
        }

        return false;
    }

    public MenuEntry GetMenu(int i) {
        return menus[i];
    }

    @Override
    public void logic() {
        for (MenuEntry m : menus) {
            if(m != null) {
                m.logic(this);
            }
        }
    }

    @Override
    public void draw(Graphics g) {
        for(int i = menus.length - 1;i >= 0;i --){
            if(menus[i] != null) {
                menus[i].draw(g);
            }
        }
    }

    public void destroy() {
        SP.rmvFromLogicList(this);
        SP.rmvFromRenderList(this);

        for(MenuEntry m : menus){   // dispose any disposables
            m.dispose();
        }
    }

    public static int GetStyleSize() {
        return C_STYLESIZE;
    }

    public static int GetScreenSize() {
        return C_SCREENSIZE;
    }

    public static int After(int ID) {
        return C_AFTER | ID;
    }

    public static int After() {
        return C_AFTER;
    }

    public menu sort(SortType s) throws SortTypeException {

        if(s.getOrder().equals("Y")){
            if(s.getType().equals("ABC")){

                String[] t = GetMenuText(true, false);
                Arrays.sort(t);

                MenuEntry[] back = menus;
                menus = new MenuEntry[back.length];

                int minY = Integer.MAX_VALUE;
                for (MenuEntry b : back) {
                    if (b.GetY() < minY) {
                        minY = b.GetY();
                    }
                }

                for(int i = 0;i < back.length;i ++){
                    menus[i] = back[GetMenu(t[i], back)];

                    if(i != 0) {
                        menus[i].SetY(After(i - 1));

                    } else {
                        menus[i].SetY(minY);
                    }
                }


                return this;
            }
        }

        throw new SortTypeException(s.getType(), s.getOrder());
    }

    public int GetMenu(String in, MenuEntry[] m) {
        for(int i = 0;i < m.length;i ++){

            if(m[i].GetText().equalsIgnoreCase(in)){
                return i;
            }
        }

        return -1;
    }

    public int GetMenuID(String in, MenuEntry[] m) {
        for(int i = 0;i < m.length;i ++){

            if(m[i].GetID().equalsIgnoreCase(in)){
                return i;
            }
        }

        return -1;
    }

    public int GetMenuID(String in) {
        for(int i = 0;i < menus.length;i ++){

            if(menus[i].GetID().equalsIgnoreCase(in)){
                return i;
            }
        }

        return -1;
    }

    public String[] GetMenuText(boolean toUpper, boolean toLower) {
        String[] out = new String[menus.length];

        for(int i = 0;i < out.length;i ++){
            out[i] = menus[i].GetText();

            if(toUpper){
                out[i] = out[i].toUpperCase();

            } else if(toLower){
                out[i] = out[i].toLowerCase();
            }
        }

        return out;
    }

    public String GetMenuText(String ID) {
        return menus[GetMenuID(ID)].GetText();
    }

    public String GetBaseText(String ID) {
        return menus[GetMenuID(ID)].GetBase();
    }

    public boolean SetMenuText(String ID, String text) {
        int id = GetMenuID(ID);
        if(id != -1) {
            menus[id].SetText(text);
            return true;
        }

        return false;
    }

    public void SetBaseText(String ID, String text) {
        menus[GetMenuID(ID)].SetBase(text);
    }

    public menu AddDropdownMenu(String ID, int x, int y, StyleItem style, float alpha,
                                String[] elementTexts, EventHandler[] elementEvents) {
        if (CheckIDUsed(ID)) {
            System.out.println("Warning: Menu ID \"" + ID + "\" already used!");
            return this;
        }

        MenuEntry[] temp = menus;
        menus = new MenuEntry[temp.length + 1];

        System.arraycopy(temp, 0, menus, 0, temp.length);
        menus[temp.length] = new DropDownMenu(ID, x, y, style, alpha, elementTexts, elementEvents);
        return this;
    }

    public menu setOff(bounds off, String ID) {
        int i = GetMenuID(ID, menus);

        menus[i].SetXYOff(off);
        return this;
    }

    public menu CompDropDown(String ID, int x, int y, int wdith, int height,
                             StyleItem style, float alpha, EventHandler Event) {
        if(CheckIDUsed(ID)){
            System.out.println("Warning: Menu ID \""+ ID +"\" already used!");
            return this;
        }

        String[] texts = file.AddString(ArrFieldGet(file.GetFileList_(v.LaunchAdr +"/modules/comp/"+ v.OS +"/", "txt"), "name"), ID, 0);
        EventHandler[] Events = new EventHandler[texts.length];

        Arrays.fill(Events, Event);
        Events[0] = null;

        MenuEntry[] temp = menus;
        menus = new MenuEntry[temp.length + 1];

        System.arraycopy(temp, 0, menus, 0, temp.length);
        menus[temp.length] = new DropDownMenu(ID, x, y, wdith, height, style, alpha, texts, Events);
        return this;
    }

    private String[] ArrFieldGet(String[] arr, String field) {
        try {
            for (int i = 0;i < arr.length;i ++) {
                arr[i] = project.GetField(field, new String(file.readFile(arr[i])).split("\n"));
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return arr;
    }

    public menu ModuleList(String ID, int x, int y, int width, int height, StyleItem style, float alpha,
                           EventHandler Event, String dir, String extension) {
        if(CheckIDUsed(ID)){
            System.out.println("Warning: Menu ID \""+ ID +"\" already used!");
            return this;
        }

        String[] texts = file.AddString(ArrFieldGet(file.GetFileList_(dir, extension), "name"), ID, 0);
        EventHandler[] Events = new EventHandler[texts.length];
        Events[0] = null;

        Arrays.fill(Events, Event);

        MenuEntry[] temp = menus;
        menus = new MenuEntry[temp.length + 1];

        System.arraycopy(temp, 0, menus, 0, temp.length);
        menus[temp.length] = new DropDownMenu(ID, x, y, width, height, style, alpha, texts, Events);
        return this;
    }

    public menu AddCheckBox(String ID, int x, int y, int width, int height, boolean value,
                            StyleItem style, float alpha, EventHandler Event) {
        if(CheckIDUsed(ID)){
            System.out.println("Warning: Menu ID \""+ ID +"\" already used!");
            return this;
        }

        MenuEntry[] temp = menus;
        menus = new MenuEntry[temp.length + 1];

        System.arraycopy(temp, 0, menus, 0, temp.length);
        menus[temp.length] = new CheckBox(ID, x, y, width, height, value, style, alpha, Event);
        return this;
    }

    public class SortTypeException extends Exception {
        public SortTypeException(String type, String order) {
            super("type ("+ type +") and order ("+ order +") combination is illegal or does not exist!");
        }
    }

    @Override
    public int renderPriority() {
        return v.RENDERPR_MAX - 2;
    }
}
