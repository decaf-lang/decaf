package decaf.driver;

import org.apache.commons.cli.*;

import java.io.FileNotFoundException;
import java.util.Optional;

/**
 * Command line option parser.
 */
public class OptParser {
    static final String OUTPUT = "o";
    final Option output = Option
            .builder(OUTPUT)
            .longOpt("output")
            .hasArg()
            .argName("file")
            .desc("output file for result, available except PA5 (default stdout)")
            .build();

    static final String DST = "d";
    final Option dst = Option
            .builder(DST)
            .longOpt("dir")
            .hasArg()
            .argName("directory")
            .desc("output directory for low-level code, available >= PA3 (default .)")
            .build();

    static final String TARGET = "t";
    final Option target = Option
            .builder(TARGET)
            .longOpt("target")
            .hasArg()
            .argName("target")
            .desc("target/task: PA1, PA1-LL, PA2, PA3, PA4, or PA5 (default)")
            .build();

    static final String LOG_COLORFUL = "log-color";
    final Option logColorful = Option
            .builder(null)
            .longOpt(LOG_COLORFUL)
            .hasArg(false)
            .desc("enable colorful log (default plain)")
            .build();

    static final String LOG_LEVEL = "log-level";
    final Option logLevel = Option
            .builder(null)
            .longOpt(LOG_LEVEL)
            .hasArg()
            .argName("level")
            .desc("log level: all, severe, warning, info, config, fine, finer, finest, off (default)")
            .build();

    static final String LOG_FILE = "log-file";
    final Option logFile = Option
            .builder(null)
            .longOpt(LOG_FILE)
            .hasArg()
            .argName("file")
            .desc("also dump log to a file")
            .build();

    static final String HELP = "h";
    final Option help = Option
            .builder(HELP)
            .longOpt("help")
            .hasArg(false)
            .desc("prints this usage text")
            .build();

    Options options;

    public OptParser() {
        options = new Options();
        options.addOption(output);
        options.addOption(dst);
        options.addOption(target);
        options.addOption(help);
        // log related
        options.addOption(logLevel);
        options.addOption(logFile);
        options.addOption(logColorful);
    }

    public void printHelp() {
        String header = "options:\n\n";
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("decaf [options] <input file>", header, options, "");
    }

    public Optional<Config> parse(String[] args) {
        var parser = new DefaultParser();
        try {
            var cli = parser.parse(options, args);

            if (cli.hasOption(HELP)) {
                printHelp();
                System.exit(0);
            }

            return Optional.of(Config.fromCLI(cli));
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            printHelp();
        } catch (FileNotFoundException e) {
            System.err.println("Invalid file: " + e.getMessage());
        }

        return Optional.empty();
    }
}
