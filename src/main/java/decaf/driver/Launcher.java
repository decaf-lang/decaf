package decaf.driver;

public class Launcher {
    public static void withArgs(String[] args) {
        var parser = new OptParser();
        var config = parser.parse(args);
        withConfig(config);
    }

    public static void withConfig(Config config) {
        var tasks = new TaskFactory(config);
        var task = switch (config.target) {
            case PA1 -> tasks.parse();
            case PA2 -> tasks.typeCheck();
            case PA3 -> tasks.tacGen();
        };
        assert task != null;
        task.apply(config.source);
    }
}
