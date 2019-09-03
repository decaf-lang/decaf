package decaf.driver;

import org.apache.commons.cli.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintStream;

public class OptParser {
    final Option output = Option
            .builder("o")
            .longOpt("output")
            .argName("file")
            .desc("output file (default stdout)")
            .build();

    final Option dir = Option
            .builder("d")
            .longOpt("dir")
            .argName("directory")
            .desc("output directory (enabled when target=jvm, default .)")
            .build();

    final Option target = Option
            .builder("t")
            .longOpt("target")
            .argName("target")
            .desc("compilation target: jvm (default), PA1, PA2, or PA3")
            .build();

    final Option help = Option
            .builder("h")
            .longOpt("help")
            .hasArg(false)
            .desc("prints this usage text")
            .build();

    Options options;

    public OptParser() {
        options = new Options();
        options.addOption(output);
        options.addOption(dir);
        options.addOption(target);
        options.addOption(help);
    }

    public Config parse(String[] args) {
        var parser = new DefaultParser();
        CommandLine cli;
        try {
            cli = parser.parse(options, args);
            // Create config
            if (cli.getArgList().isEmpty()) {
                throw new IllegalArgumentException("no input files");
            }

            var source = new FileInputStream(cli.getArgList().get(0));
            var outputStream = cli.hasOption('o') ? new PrintStream(new File(cli.getOptionValue('o'))) : Config.STDOUT;
            var outputDir = cli.hasOption('d') ? new File(cli.getOptionValue('d')).toPath() : Config.PWD;
            var target = cli.hasOption('t') ? Config.parseTarget(cli.getOptionValue('t')) : Config.Target.JVM;
            return new Config(source, outputStream, outputDir, target);
        } catch (ParseException | FileNotFoundException e) {
            System.err.println("Parsing failed.  Reason: " + e.getMessage());
        }

        return null;
    }
}
