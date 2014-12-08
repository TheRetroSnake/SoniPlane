package gs.soni.plane;

import gs.app.lib.application.App;
import gs.app.lib.application.AppConfig;
import gs.app.lib.util.FileUtil;
import gs.soni.plane.project.project;
import gs.soni.plane.util.file;

import java.io.*;

public class Main {

    private static String[] bugReportThreads = new String[]{
            "http://forums.sonicretro.org/index.php?showtopic=33520",
    };

    public static void main(String[] args) {
        String adr = FileUtil.getJarFolder();
        adr = adr.substring(0, adr.length() - 2).replace("\\", "/");
        try {
            file.saveFile(adr +"/prefs.txt", new String(file.readFile(adr +"/prefs.txt")).replace("\r", "").getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if(!v.test) {
            RedidectOutput(adr);
            printOS(adr);
        }

        if (FileUtil.exists(adr + "/prefs.txt") && isReset(adr)) {
            String[] d = FileUtil.readString(adr + "/prefs.txt").split("\n");

            new App(new AppConfig("Loading the program",
                    Integer.parseInt(project.GetField("windowX", d)),
                    Integer.parseInt(project.GetField("windowY", d)),
                    Integer.parseInt(project.GetField("windowWidth", d)),
                    Integer.parseInt(project.GetField("windowHeight", d)), true), new SP(args, false));

        } else {
            new App(new AppConfig("Loading the program", 0, 0, 1280, 720, true), new SP(args, true));
        }
    }

    private static void RedidectOutput(String dir) {
        try {
            if(!new File(dir +"/run.txt").exists() || new File(dir +"/run.txt").canWrite()) {
                PrintStream out = new PrintStream(new File(dir + "/run.txt"));
                System.setOut(out);
                System.setErr(out);
            } else {
                System.out.println("Failed to access file \""+ (dir + "/run.txt") + "\" for write! Direction output to console.");
            }
        } catch (FileNotFoundException e) {
            System.out.println("Failed to access file \""+ (dir + "/run.txt") + "\" for write:");
            e.printStackTrace();
            System.out.println("\nDirection output to console.");
        }
    }

    private static void printOS(String adr) {
        String OS = System.getProperty("os.name");
        System.out.println(
                "OS information dump; SoniPlane beta "+ v.version +"\n" +
                "Launch directory:                   "+ adr +" (user.dir: "+ System.getProperty("user.dir") +")\n"+
                "OS version:                         "+ OS +" "+ System.getProperty("sun.arch.data.model")
                    +"-bit "+ System.getProperty("os.version") +"; "+ System.getProperty("os.arch")
                    +" "+ System.getProperty("sun.desktop") +"\n" +
                "System language:                    "+System.getProperty("user.language") +" "+ System.getProperty("user.country") +"\n"+
                "Java version:                       "+ System.getProperty("java.version") +" "+
                    System.getProperty("java.runtime.version") +"\n" +
                "Java virtual machine version:       "+ System.getProperty("java.vm.version") +" "+
                    System.getProperty("java.vm.name") +"\n" +
                "Java virtual machine information:   "+ System.getProperty("java.vm.info") +"\n"+
                "Available Java memory:              "+ Runtime.getRuntime().maxMemory() +"\n" +
                "Java heap size (may be inaccurate): "+ Runtime.getRuntime().freeMemory() +"\n" +
                "Processors (cores):                 "+ Runtime.getRuntime().availableProcessors() +"\n"+
                "Database size(bytes):               "+ getDatabaseSize(new File(adr)) +"\n");

        for (File root : File.listRoots()) {
            System.out.println("File system root:                   "+ root.getAbsolutePath());
            System.out.println("Total space (bytes):                "+ root.getTotalSpace());
            System.out.println("Free space (bytes):                 "+ root.getFreeSpace());
            System.out.println("Usable space (bytes):               "+ root.getUsableSpace() +"\n");
        }

        System.out.println("Bug reports can be posted at:");
        for (String bug : bugReportThreads) {
            System.out.println("  "+ bug);
        }

        System.out.println(OS.startsWith("Windows 9") ? "\nGais Windows 9 waz neva a thing!" :
                        OS.startsWith("Windows 2000") ? "\nOMG NEW MILLENNIUM AHAHAHAHA SHUT UP ALREADY!" : "\n");
        System.out.println("\nStandard output starts here:");
    }

    private static long getDatabaseSize(File f) {
        long ret = 0;
        if (f.isDirectory()) {
            for (File c : f.listFiles()) {
                ret += getDatabaseSize(c);
            }
        }

        return ret + f.length();
    }

    private static boolean isReset(String adr) {
        return FileUtil.readString(adr +"/prefs.txt").contains("reset: false") &&
               FileUtil.readString(adr +"/prefs.txt").contains("version: "+ v.prefversion);
    }
}
