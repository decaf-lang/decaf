package decaf.driver;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.nio.file.Path;

public class Config {
    public enum Target {
        PA1, PA2, PA3
    }

    public final FileInputStream source;

    public final Path sourcePath;

    // Warning: never close this since it could be stdout.
    public final OutputStream output;

    public final Path dstPath;

    public final Target target;

    private Config(FileInputStream source, Path sourcePath, OutputStream output, Path dstPath, Target target) {
        this.source = source;
        this.sourcePath = sourcePath;
        this.output = output;
        this.dstPath = dstPath;
        this.target = target;
    }

    public static Path PWD = new File(System.getProperty("user.dir")).toPath();
    public static OutputStream STDOUT = System.out;

    public static Config fromCLI(CommandLine cli) throws ParseException, FileNotFoundException {
        if (cli.getArgList().isEmpty()) {
            throw new ParseException("No input files");
        }

        var sourceFile = new File(cli.getArgList().get(0));
        var source = new FileInputStream(sourceFile);
        var sourcePath = sourceFile.toPath();

        var target = Target.PA3;
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

        return new Config(source, sourcePath, output, dstPath, target);
    }

    public String getSourceBaseName() {
        return FilenameUtils.getBaseName(sourcePath.getFileName().toString());
    }

    private static Target parseTarget(String target) throws ParseException {
        return switch (target) {
            case "PA1" -> Target.PA1;
            case "PA2" -> Target.PA2;
            case "PA3" -> Target.PA3;
            default -> throw new ParseException(String.format("Invalid target: '%s'", target));
        };
    }
}
