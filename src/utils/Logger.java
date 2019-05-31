package utils;

import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Logger
 */
public abstract class Logger {

    /** Logger instance */
    private static java.util.logging.Logger logger;

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

    /**
     * Fine message
     * @param prefix Prefix
     * @param msg Message
     */
    public static void fine(String prefix, String msg) {
        logger.fine(prefix + ": " + msg + ".");
    }

    /**
     * Info message
     * @param prefix Prefix
     * @param msg Message
     */
    public static void info(String prefix, String msg) {
        logger.info(prefix + ": " + msg + ".");
    }

    /**
     * Warning message
     * @param prefix Prefix
     * @param msg Message
     */
    public static void warning(String prefix, String msg) {
        logger.warning(prefix  + ": " + msg + ".");
    }

    /**
     * Severe message
     * @param prefix Prefix
     * @param msg Message
     */
    public static void severe(String prefix, String msg) {
        logger.severe(prefix + ": " + msg + ".");
        System.exit(-1);
    }
}