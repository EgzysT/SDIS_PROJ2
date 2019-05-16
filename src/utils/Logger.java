package utils;

import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public final class Logger {

//    private final static java.util.logging.Logger Logger = java.util.logging.Logger.getLogger("Logger");

    private static java.util.logging.Logger logger; // = java.util.logging.Logger.getAnonymousLogger();

    static {
        final Level loggerLevel = Level.FINE;

        logger = java.util.logging.Logger.getAnonymousLogger();
        logger.setLevel(loggerLevel);
        logger.setUseParentHandlers(false);

        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(loggerLevel);
        handler.setFormatter(new Formatter() {
            @Override
            public String format(LogRecord record) {
                return "[" + record.getLevel() + "] " + formatMessage(record) + System.lineSeparator();
            }
        });

        logger.addHandler(handler);
    }

    public static void fine(String prefix, String msg) {
        logger.fine(prefix + ": " + msg + ".");
    }

    public static void info(String prefix, String msg) {
        logger.info(prefix + ": " + msg + ".");
    }

    public static void warning(String prefix, String msg) {
        logger.warning(prefix  + ": " + msg + ".");
    }

    public static void severe(String prefix, String msg) {
        logger.severe(prefix + ": " + msg + ".");
        System.exit(-1);
    }
}