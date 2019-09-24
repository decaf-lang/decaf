package decaf.lowlevel.log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.*;

/**
 * A very simple and easy-to-use logging library, with only one internal logger instance. Support color and file dump.
 */
public class Log {

    private Log() {
    }

    private static Logger L;

    private static Formatter simpleFormatter = new Formatter() {
        @Override
        public String format(LogRecord record) {
            return String.format("[%s] %s\n", record.getLevel(), record.getMessage());
        }
    };

    private static List<OutputStream> outs = new ArrayList<>();

    static {
        L = Logger.getGlobal();
        L.setLevel(Level.OFF);
        LogManager.getLogManager().reset();
    }

    /**
     * Setup logger. Must call this to enable logging.
     *
     * @param level     log level
     * @param showColor show color?
     * @param files     a list of file paths for output
     */
    public static void setup(Level level, boolean showColor, String... files) throws IOException {
        var ch = showColor ? new ColorConsoleHandler() : new ConsoleHandler();
        ch.setLevel(level);
        ch.setFormatter(simpleFormatter);
        L.addHandler(ch);
        outs.add(System.err);

        for (var path : files) {
            var fh = new FileHandler(path, true);
            fh.setLevel(level);
            fh.setFormatter(simpleFormatter);
            L.addHandler(fh);
            outs.add(new FileOutputStream(path, true));
        }

        L.setLevel(level);
    }

    /**
     * Get the underlying logger, which is the {@link Logger#getGlobal()}.
     * Warning: strange things may happen if you play with this logger improperly.
     *
     * @return logger
     */
    public Logger getLogger() {
        return L;
    }

    /**
     * Formatted log.
     *
     * @param level log level
     * @param fmt   format
     * @param args  argument
     */
    public static void format(Level level, String fmt, Object... args) {
        if (L.isLoggable(level)) {
            L.log(level, String.format(fmt, args));
        }
    }

    /**
     * Wrapper of {@link #format} when log level is {@link Level#SEVERE}.
     */
    public static void severe(String fmt, Object... args) {
        format(Level.SEVERE, fmt, args);
    }

    /**
     * Wrapper of {@link #format} when log level is {@link Level#WARNING}.
     */
    public static void warn(String fmt, Object... args) {
        format(Level.WARNING, fmt, args);
    }

    /**
     * Wrapper of {@link #format} when log level is {@link Level#INFO}.
     */
    public static void info(String fmt, Object... args) {
        format(Level.INFO, fmt, args);
    }

    /**
     * Wrapper of {@link #format} when log level is {@link Level#CONFIG}.
     */
    public static void config(String fmt, Object... args) {
        format(Level.CONFIG, fmt, args);
    }

    /**
     * Wrapper of {@link #format} when log level is {@link Level#FINE}.
     */
    public static void fine(String fmt, Object... args) {
        format(Level.FINE, fmt, args);
    }

    /**
     * Wrapper of {@link #format} when log level is {@link Level#FINER}.
     */
    public static void finer(String fmt, Object... args) {
        format(Level.FINER, fmt, args);
    }

    /**
     * Wrapper of {@link #format} when log level is {@link Level#FINEST}.
     */
    public static void finest(String fmt, Object... args) {
        format(Level.FINEST, fmt, args);
    }

    /**
     * Pretty log (via {@link IndentPrinter}), if loggable, i.e. the given log level would actually be logged by this
     * logger, right now.
     *
     * @param level  log level
     * @param action input an indent printer, do the actual logging
     */
    public static void ifLoggable(Level level, Consumer<IndentPrinter> action) {
        if (L.isLoggable(level)) {
            var printer = new IndentPrinter(outs, true);
            action.accept(printer);
            printer.flush();
        }
    }
}
