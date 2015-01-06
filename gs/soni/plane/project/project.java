package gs.soni.plane.project;

import gs.app.lib.util.FileUtil;
import gs.soni.plane.util.file;
import gs.soni.plane.v;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

// these are utilities for project files
public class project {
    public static String GetField(String field, String[] file) {
        for(String s : file){

            if(s.startsWith(field  +": ")){
                return s.replace(field  +": ", "");
            }
        }

        throw new NoSuchFieldError("\""+ field +"\"");
    }

    public static boolean isProject(String[] file) {
        return file[0].startsWith("SoniPlaneProject: ");
    }

    public static String DoCMD(String in, String Compressor, String field, String out)
            throws IOException, InterruptedException {

        String comp_path = v.LaunchAdr +"/modules/comp/"+ v.OS +"/";
        String[] Descriptor = new String(file.readFile(comp_path +"OS_SPECIFIC.asm")).split("\n");

        if(project.GetField("os.name", Descriptor).equals(System.getProperty("os.name").split(" ")[0])) {
            String separator = project.GetField("folder separator", Descriptor).
                    replace("%system%", System.getProperty("file.separator"));

            String main_com = (project.GetField("command", Descriptor).
                    replace("%cmd%", project.GetField("cmd", new String(file.readFile(comp_path + Compressor)).split("\n"))).
                    replace("%cmd%", (project.GetField(field, new String(file.readFile(comp_path + Compressor)).split("\n")).
                            replace("%%I", in).replace("%%O", out).
                    replace("%CD%", v.LaunchAdr + "/modules/comp/")).replace("\\", separator).replace("/", separator)));

            if(main_com.equals("%copyfile%")){
                FileUtil.writeBytes(out, FileUtil.readBytes(out), false);

            } else {
                new ProcessBuilder(GetArguments(main_com)).start().waitFor();
            }
            return out;
        }

        throw new NullPointerException("Could not validate Operating System \""+ project.GetField("os.name", Descriptor) +"\"!");
    }

    private static String[] GetArguments(String com) {
        ArrayList<String> ret = new ArrayList<String>();

        while(com.length() != 0){
            boolean isNew = false;

            int i = 0;
            for(;i < com.length();i ++){
                if(com.charAt(i) == ' '){
                    ret.add(ret.size(), com.substring(0, i));

                    if(com.length() - (i + 1) > 0) {
                        com = com.substring(i + 1, com.length());
                    } else {
                        com = "";
                    }

                    isNew = true;
                    break;

                } else if(com.charAt(i) == '"'){
                    for(int a = i + 1;a < com.length();a ++){

                        if(com.charAt(a) == '"'){
                            ret.add(ret.size(), com.substring(1, a));
                            if(com.length() - (a + 2) > 0) {
                                com = com.substring(a + 2, com.length());
                            } else {
                                com = "";
                            }
                            break;
                        }
                    }
                    isNew = true;
                    break;
                }
            }

            if(!isNew){
                ret.add(ret.size(), com);
                com = "";
            }
        }

        return ret.toArray(new String[ret.size()]);
    }

    public static String[] SetField(String field, String value, String[] file) {
        for(int i = 0;i < file.length;i ++){

            if(file[i].startsWith(field  +": ")){
                file[i] = file[i].replace(file[i].replace(field  +": ", ""), value);
                return file;
            }
        }

        String[] ret = new String[file.length + 1];
        System.arraycopy(file, 0, ret, 0, file.length);
        ret[ret.length - 1] = field  +": "+ value;
        return ret;
    }

    public static String GetField(String field, String[] file, String def) {
        for(String s : file){

            if(s.startsWith(field  +": ")){
                return s.replace(field  +": ", "");
            }
        }

        return def;
    }

    public static void RemoveRN() throws FileNotFoundException {    // used to fix manual edit errors
        for(String f : file.GetFileList_(v.LaunchAdr +"/projects/", "SPP")){
            if(FileUtil.exists(f)) {
                String[] d = new String(file.readFile(f)).replace("\r", "").split("\n");

                if(d[0].startsWith("SoniPlaneProject v")){
                    d[0] = "SoniPlaneProject: "+ v.projversion;
                    project.SetField("line offset", "0", d);
                }

                file.saveFile(f, d, "\n");
            }
        }

        for(String f : file.GetFileList_(v.LaunchAdr +"/autosave/", "SPP")){
            if(FileUtil.exists(f)) {
                String[] d = new String(file.readFile(f)).replace("\r", "").split("\n");

                if(d[0].startsWith("SoniPlaneProject v")){
                    d[0] = "SoniPlaneProject: "+ v.projversion;
                    project.SetField("line offset", "0", d);
                }

                file.saveFile(f, d, "\n");
            }
        }

        for(String f : file.GetFileList_(v.LaunchAdr +"/modules/map/", "txt")){
            file.saveFile(f, new String(file.readFile(f)).replace("\r", "").getBytes());
        }

        for(String f : file.GetFileList_(v.LaunchAdr +"/modules/tile/", "txt")){
            file.saveFile(f, new String(file.readFile(f)).replace("\r", "").getBytes());
        }

        for(String f : file.GetFileList_(v.LaunchAdr +"/modules/palette/", "txt")){
            file.saveFile(f, new String(file.readFile(f)).replace("\r", "").getBytes());
        }

        for(String f : file.GetFileList_(v.LaunchAdr +"/modules/comp/"+ v.OS +"/", "txt")){
            file.saveFile(f, new String(file.readFile(f)).replace("\r", "").getBytes());
        }

        file.saveFile(v.LaunchAdr +"/modules/comp/"+ v.OS +"/OS_SPECIFIC.asm",
                new String(file.readFile(v.LaunchAdr +"/modules/comp/"+ v.OS +"/OS_SPECIFIC.asm")).replace("\r", "").getBytes());
    }

    public static int getStateAmount() {
        int ret = 0;
        for (String f : file.GetFileList(v.LaunchAdr +"/autosave/", "SPP")) {
            String n = f.replace(v.LaunchAdr +"/autosave/", "");

            if (n.startsWith("st") && n.contains(project.GetField("name", getFields(v.project)))) {
                ret ++;
            }
        }

        return ret;
    }

    public static String[] getFields(String f){
        try {
            return new String(file.readFile(f)).split("\n");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return new String[0];
    }
}
