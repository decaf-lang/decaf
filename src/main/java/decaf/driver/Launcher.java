package decaf.driver;

/**
 * Entry of the compiler.
 */
public class Launcher {

    /**
     * Launch the compiler with command line args.
     *
     * @param args command line args and options
     */
    public static void withArgs(String[] args) {
        var parser = new OptParser();
        parser.parse(args).ifPresent(Launcher::withConfig);
    }

    /**
     * Launch the compiler with configuration.
     *
     * @param config compiler configuration
     */
    public static void withConfig(Config config) {
        var tasks = new TaskFactory(config);
        var task = switch (config.target) {
            case PA1 -> tasks.parse();
            case PA1_LL -> tasks.parseLL();
            case PA2 -> tasks.typeCheck();
            case PA3 -> tasks.tacGen();
            case PA4 -> tasks.optimize();
            case PA5 -> tasks.mips();
        };
        task.apply(config.source);
    }
}
