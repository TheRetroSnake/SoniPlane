package gs.soni.plane;

import gs.app.lib.application.App;
import gs.app.lib.application.AppConfig;
import gs.app.lib.util.FileUtil;
import gs.soni.plane.project.project;
import gs.soni.plane.util.file;

import java.io.*;

public class Main {
    /* Direct addresses for threads to report bugs in (also release threads :3) */
    private static String[] bugReportThreads = new String[]{
            "http://forums.sonicretro.org/index.php?showtopic=33520",
    };

    /* to be executed first */
    public static void main(String[] args) throws IOException {
        /* get the launch folder and make sure SoniPlane.jar exists in it */
        String adr = FileUtil.getJarFolder();
        if(!file.IsRightFolder(adr)){
            /* does not? Create window with max size to display message and such */
            new App(new AppConfig("Cannot launch at directory "+ adr, 0, 0, 9999, 9999, false), new SP(args, true));
        }

        /* HOLY SHIT THIS IS WHY IT DIDNT WORK OMG I AM SO DUMB */
        adr = adr.replace("\\", "/");
        try {
            /* remove allow /r sequences from preferences files to make sure its fine */
            file.saveFile(adr +"/prefs.txt", new String(file.readFile(adr +"/prefs.txt")).replace("\r", "").getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        /* redidects output to run.txt file (if free) and prints OS infromation */
        if(!v.test) {
            RedidectOutput(adr);
            printOS(adr);
        }

        /* and here we make sure preferences arent supposed to reset and the actual file exists */
        if (FileUtil.exists(adr +"/prefs.txt") && isReset(adr)) {
            /* get preferences to String array */
            String[] d = FileUtil.readString(adr + "/prefs.txt").split("\n");

            /* create new application with data from preferences */
            new App(new AppConfig("Loading the program",
                    Integer.parseInt(project.GetField("windowX", d)),
                    Integer.parseInt(project.GetField("windowY", d)),
                    Integer.parseInt(project.GetField("windowWidth", d)),
                    Integer.parseInt(project.GetField("windowHeight", d)), true), new SP(args, false));

        } else {
            /* new application with default settings */
            new App(new AppConfig("Loading the program", 0, 0, 1280, 720, true), new SP(args, true));
        }
    }

    /* redidect output to run.txt if possible */
    private static void RedidectOutput(String dir) {
        try {
            /* make sure the file doesnt exist, or if does, make sure we can write on it */
            if(!new File(dir +"/run.txt").exists() || new File(dir +"/run.txt").canWrite()) {
                /* get the file as PrintStream */
                PrintStream out = new PrintStream(new File(dir +"/run.txt"));
                /* pass the PrintStream to System.out and System.err */
                System.setOut(out);
                System.setErr(out);
            } else {
                System.err.println("Failed to access file \""+ (dir +"/run.txt") +"\" for write! Directing output to console.");
            }

        } catch (FileNotFoundException e) {
            System.err.println("Failed to access file \""+ (dir + "/run.txt") +"\" for write:");
            e.printStackTrace();
            System.err.println("\nDirecting output to console.");
        }
    }

    /* print information about OS */
    private static void printOS(String adr) {
        String OS = System.getProperty("os.name");
        System.out.println(
                /* tell version number */
                "OS information dump; SoniPlane beta "+ v.version +"\n" +
                /* tell launch directory */
                "Launch directory:                   "+ adr +" (user.dir: "+ System.getProperty("user.dir") +")\n"+
                /* tell OS version, OS bits, OS version, OS architecture, and something */
                "OS version:                         "+ OS +" "+ System.getProperty("sun.arch.data.model")
                    +"-bit "+ System.getProperty("os.version") +"; "+ System.getProperty("os.arch")
                    +" "+ System.getProperty("sun.desktop") +"\n" +
                /* tell system language and country */
                "System language:                    "+System.getProperty("user.language") +" "+ System.getProperty("user.country") +"\n"+
                /* tell Java versions */
                "Java version:                       "+ System.getProperty("java.version") +" "+
                    System.getProperty("java.runtime.version") +"\n" +
                /* tell JVM version and name */
                "Java virtual machine version:       "+ System.getProperty("java.vm.version") +" "+
                    System.getProperty("java.vm.name") +"\n" +
                /* tell misc JVM info */
                "Java virtual machine information:   "+ System.getProperty("java.vm.info") +"\n"+
                /* tell Java memory */
                "Available Java memory:              "+ GetBytes(Runtime.getRuntime().maxMemory()) +"\n" +
                /* tell initial Java heap size */
                "Java heap size (may be inaccurate): "+ GetBytes(Runtime.getRuntime().freeMemory()) +"\n" +
                /* list cores */
                "Processors (cores):                 "+ Runtime.getRuntime().availableProcessors() +"\n"+
                /* list full filesize of database of SoniPlane */
                "Database size:                      "+ GetBytes(getDatabaseSize(new File(adr))) +"\n");

        /* list information of HardDrives */
        for (File root : File.listRoots()) {
            System.out.println("File system root:                   "+ root.getAbsolutePath());
            System.out.println("Total space:                        "+ GetBytes(root.getTotalSpace()));
            System.out.println("Free space:                         "+ GetBytes(root.getFreeSpace()));
            System.out.println("Used space:                         "+ GetBytes(root.getTotalSpace() - root.getFreeSpace()) +"\n");
        }

        /* list bug report addresses */
        System.out.println("Bug reports can be posted at:");
        for (String bug : bugReportThreads) {
            System.out.println("-> "+ bug);
        }
        /* start of output from System.out and System.err */
        System.out.println("\nStandard output starts here:");
    }

    /* get size of all files on directory */
    private static long getDatabaseSize(File f) {
        long ret = 0;
        /* if the file is a directory */
        if (f.isDirectory()) {
            /* recursively check all directories */
            for (File c : f.listFiles()) {
                /* add data size from file */
                ret += getDatabaseSize(c);
            }
        }

        /* return the final length */
        return ret + f.length();
    }

    /* just checks if preferences need resetting */
    private static boolean isReset(String adr) {
        return FileUtil.readString(adr +"/prefs.txt").contains("reset: false") &&
               FileUtil.readString(adr +"/prefs.txt").contains("version: "+ v.prefversion);
    }

    /* variables to store file sizes (needed for TeraByte) */
    public static final double SPACE_KB = 1024;
    public static final double SPACE_MB = 1024 * SPACE_KB;
    public static final double SPACE_GB = 1024 * SPACE_MB;
    public static final double SPACE_TB = 1024 * SPACE_GB;

    /* translate size to Bytes/kiloBytes/megaBytes/gigaBytes/teraBytes accordingly */
    private static String GetBytes(double size) {
        /* if more than 1 GigaByte */
        if(size >= SPACE_TB) {
            return String.format("%.02f", (size / SPACE_TB)) + " TB";

        /* if more than 1 GigaByte */
        } else if(size >= SPACE_GB){
            return String.format("%.02f", (size / SPACE_GB)) +" GB";

        } else if(size >= SPACE_MB){
            /* if more than 1 megaByte*/
            return String.format("%.02f", (size / SPACE_MB)) +" MB";

        } else if(size >= SPACE_KB){
            /* if more than 1 kiloByte */
            return String.format("%.02f", (size / SPACE_KB)) +" KB";
        }

        /* less than 1 kilobyte */
        return (long)size +" B";     // is casted to long to make sure no .0 is shown after
    }
}
