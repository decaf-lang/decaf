package decaf.printing;

import decaf.lowlevel.log.IndentPrinter;

public abstract class PrettyPrinter<T> {
    protected final IndentPrinter printer;

    public PrettyPrinter(IndentPrinter printer) {
        this.printer = printer;
    }

    public abstract void pretty(T t);

    public void flush() {
        printer.flush();
    }
}
