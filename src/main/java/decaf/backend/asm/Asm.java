package decaf.backend.asm;

import decaf.backend.dataflow.CFGBuilder;
import decaf.backend.dataflow.LivenessAnalyzer;
import decaf.backend.reg.RegAlloc;
import decaf.driver.Config;
import decaf.driver.Phase;
import decaf.lowlevel.tac.TAC;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 * The assembly code generation phase: translate a TAC program to assembly code.
 */
public class Asm extends Phase<TAC.Prog, String> {
    /**
     * Helper assembly code emitter.
     */
    protected final AsmEmitter emitter;

    /**
     * Register allocator.
     */
    protected final RegAlloc regAlloc;

    public Asm(AsmEmitter emitter, RegAlloc regAlloc, Config config) {
        super("asm: " + emitter.toString(), config);
        this.regAlloc = regAlloc;
        this.emitter = emitter;
    }

    @Override
    public String transform(TAC.Prog prog) {
        var analyzer = new LivenessAnalyzer<>();

        for (var vtbl : prog.vtables) {
            emitter.emitVTable(vtbl);
        }

        emitter.emitSubroutineBegin();
        for (var func : prog.funcs) {
            var pair = emitter.selectInstr(func);
            var builder = new CFGBuilder<>();
            var cfg = builder.buildFrom(pair.getLeft());
            analyzer.accept(cfg);
            regAlloc.accept(cfg, pair.getRight());
        }

        return emitter.emitEnd();
    }

    @Override
    public void onSucceed(String code) {
        if (config.target.equals(Config.Target.PA5)) {
            var path = config.dstPath.resolve(config.getSourceBaseName() + ".s");
            try {
                var printer = new PrintWriter(path.toFile());
                printer.print(code);
                printer.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
