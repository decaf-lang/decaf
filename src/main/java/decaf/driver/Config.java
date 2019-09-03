package decaf.driver;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

public class Config {
    public enum Target {
        PA1, PA2, PA3, JVM
    }

    public final InputStream source;

    public final OutputStream outputStream;

    public final Path outputDir;

    public final Target target;

    public static Path PWD = new File(System.getProperty("user.dir")).toPath();
    public static OutputStream STDOUT = System.out;

    public Config(InputStream source, OutputStream outputStream, Path outputDir, Target target) {
        this.source = source;
        this.outputStream = outputStream;
        this.outputDir = outputDir;
        this.target = target;
    }

    public static Target parseTarget(String target) {
        return switch (target) {
            case "PA1" -> Target.PA1;
            case "PA2" -> Target.PA2;
            case "PA3" -> Target.PA3;
            case "jvm" -> Target.JVM;
            default -> throw new IllegalStateException("Unexpected value: " + target);
        };
    }
}
