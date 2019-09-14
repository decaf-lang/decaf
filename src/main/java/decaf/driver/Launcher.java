package decaf.driver;

public class Launcher {
    public static void withArgs(String[] args) {
        var parser = new OptParser();
        parser.parse(args).ifPresent(Launcher::withConfig);
    }

    public static void withConfig(Config config) {
        var tasks = new TaskFactory(config);
        var task = switch (config.target) {
            case PA1 -> tasks.parse();
            case PA2 -> tasks.typeCheck();
            case PA3 -> tasks.tacGen();
            case PA4 -> tasks.tacGen();
            case PA5 -> tasks.mips();
        };
        task.apply(config.source);
    }
}
