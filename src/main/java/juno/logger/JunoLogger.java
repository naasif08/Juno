package juno.logger;
public class JunoLogger {
    private static final String PREFIX = "[JUNO] ";

    public static void info(String message) {
        System.out.println(PREFIX + message);
    }

    public static void warn(String message) {
        System.out.println(PREFIX + message);
    }

    public static void error(String message) {
        System.out.println(PREFIX + message);
    }

    public static void success(String message) {
        System.out.println(PREFIX + message);
    }
}
