package decaf.driver;

import org.apache.commons.cli.*;

import java.io.FileNotFoundException;
import java.util.Optional;

public class OptParser {
    static final String OUTPUT = "o";
    final Option output = Option
            .builder(OUTPUT)
            .longOpt("output")
            .hasArg()
            .argName("file")
            .desc("output file for result (default stdout)")
            .build();

    static final String DST = "d";
    final Option dst = Option
            .builder(DST)
            .longOpt("dir")
            .hasArg()
            .argName("directory")
            .desc("output directory for byte/native code (default .)")
            .build();

    static final String TARGET = "t";
    final Option target = Option
            .builder(TARGET)
            .longOpt("target")
            .hasArg()
            .argName("target")
            .desc("compilation target: PA1, PA2, or PA3 (default)")
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
