package utils;

import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public final class Logger {

    private final static java.util.logging.Logger Logger = java.util.logging.Logger.getLogger("Logger");

    static {
        Logger.setLevel(Level.INFO);

        Logger.setUseParentHandlers(false);
        ConsoleHandler handler = new ConsoleHandler();

        handler.setFormatter(new Formatter() {
            @Override
            public String format(LogRecord record) {
                return "[" + record.getLevel() + "] " + formatMessage(record) + System.lineSeparator();
            }
        });

        Logger.addHandler(handler);
    }

    public static void fine(String prefix, String msg) {
        Logger.fine(prefix + ": " + msg + ".");
    }

    public static void info(String prefix, String msg) {
        Logger.info(prefix + ": " + msg + ".");
    }

    public static void warning(String prefix, String msg) {
        Logger.warning(prefix  + ": " + msg + ".");
    }

    public static void severe(String prefix, String msg) {
        Logger.severe(prefix + ": " + msg + ".");
        System.exit(-1);
    }
}