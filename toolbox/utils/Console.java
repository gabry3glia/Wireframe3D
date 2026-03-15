package toolbox.utils;

public class Console {
    
    public static void printf(String format, Object... args) {
        System.out.printf(format, args);
    }

    public static void println(String format, Object... args) {
        System.out.printf(format + "\n", args);
    }

    public static void print(Object... args) {
        out(" ", "\n", args);
    }

    public static void out(String sepratator, String ending, Object... args) {
        String format = "";
        for (int i = 0; i < args.length; i++) {
            format += "%s" + (i < args.length - 1 ? sepratator : "");
        }
        System.out.printf(format + ending, args);
    }

    public static void info(String format, Object... args) {
        System.out.printf("[INFO]: " + format + "\n", args);
    }

    public static void warning(String format, Object... args) {
        System.out.printf("[WARNING]: " + format + "\n", args);
    }

    public static void error(String format, Object... args) {
        System.err.printf("[ERROR]: " + format + "\n", args);
    }

    public static void debug(String format, Object... args) {
        System.out.printf("[DEBUG]: " + format + "\n", args);
    }
}
