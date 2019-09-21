package decaf.driver;

import decaf.lowlevel.log.Log;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.nio.file.Path;
import java.util.logging.Level;

/**
 * Compiler configuration.
 */
public class Config {

    /**
     * Target/task. Options: PA1, PA1_LL, PA2, PA3, PA4, PA5.
     */
    public enum Target {
        PA1, PA1_LL, PA2, PA3, PA4, PA5
    }

    /**
     * Input Decaf source.
     */
    public final FileInputStream source;

    /**
     * Input Decaf source file path.
     */
    public final Path sourcePath;

    /**
     * Output (PA1, PA2, PA3).
     * <p>
     * Warning: NEVER close this because it could be stdout.
     */
    public final OutputStream output;

    /**
     * Output folder for TAC and assembly code (PA3, PA4, PA5).
     */
    public final Path dstPath;

    /**
     * Target/task.
     */
    public final Target target;

    private Config(FileInputStream source, Path sourcePath, OutputStream output, Path dstPath, Target target) {
        this.source = source;
        this.sourcePath = sourcePath;
        this.output = output;
        this.dstPath = dstPath;
        this.target = target;
    }

    /**
     * Path of present working directory.
     */
    public static Path PWD = new File(System.getProperty("user.dir")).toPath();

    /**
     * Stdout.
     */
    public static OutputStream STDOUT = System.out;

    /**
     * Parse configuration from command line.
     *
     * @param cli command line
     * @return configuration
     * @throws ParseException        if parse or validation fails
     * @throws FileNotFoundException if cannot locate a file
     */
    public static Config fromCLI(CommandLine cli) throws ParseException, FileNotFoundException {
        if (cli.getArgList().isEmpty()) {
            throw new ParseException("No input files");
        }

        var sourceFile = new File(cli.getArgList().get(0));
        var source = new FileInputStream(sourceFile);
        var sourcePath = sourceFile.toPath();

        var target = Target.PA5;
        if (cli.hasOption(OptParser.TARGET)) {
            target = parseTarget(cli.getOptionValue(OptParser.TARGET));
        }

        var output = STDOUT;
        if (cli.hasOption(OptParser.OUTPUT)) {
            output = new FileOutputStream(cli.getOptionValue(OptParser.OUTPUT));
        }

        var dstPath = PWD;
        if (cli.hasOption(OptParser.DST)) {
            var dir = new File(cli.getOptionValue(OptParser.DST));
            if (!dir.isDirectory()) {
                throw new FileNotFoundException(dir.getPath() + " (Not an existed directory)");
            }
            dstPath = dir.toPath();
        }

        if (cli.hasOption(OptParser.LOG_LEVEL)) {
            var showColor = cli.hasOption(OptParser.LOG_COLORFUL);
            var l = cli.getOptionValue(OptParser.LOG_LEVEL);
            try {
                var level = Level.parse(l.toUpperCase());
                if (cli.hasOption(OptParser.LOG_FILE)) {
                    Log.setup(level, showColor, cli.getOptionValue(OptParser.LOG_FILE));
                } else {
                    Log.setup(level, showColor);
                }
            } catch (IllegalArgumentException e) {
                throw new ParseException(String.format("Invalid log level: '%s'", l));
            } catch (IOException e) {
                throw new FileNotFoundException(e.toString());
            }
        }

        return new Config(source, sourcePath, output, dstPath, target);
    }

    /**
     * Get just the base name, without extension, of the input Decaf source.
     * <p>
     * Example: the base name of {@code myFolder/blackjack.decaf} is {@code blackjack}.
     *
     * @return base name
     */
    public String getSourceBaseName() {
        return FilenameUtils.getBaseName(sourcePath.getFileName().toString());
    }

    /**
     * Parse target from string.
     *
     * @param target string representation of the target
     * @return target
     * @throws ParseException if input is invalid
     */
    private static Target parseTarget(String target) throws ParseException {
        return switch (target) {
            case "PA1" -> Target.PA1;
            case "PA1-LL" -> Target.PA1_LL;
            case "PA2" -> Target.PA2;
            case "PA3" -> Target.PA3;
            case "PA4" -> Target.PA4;
            case "PA5" -> Target.PA5;
            default -> throw new ParseException(String.format("Invalid target: '%s'", target));
        };
    }
}
