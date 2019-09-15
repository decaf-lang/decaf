package decaf.backend.asm;

import decaf.backend.reg.RegAlloc;
import decaf.dataflow.CFGBuilder;
import decaf.dataflow.LivenessAnalyzer;
import decaf.driver.Config;
import decaf.driver.Phase;
import decaf.instr.PseudoInstr;
import decaf.instr.tac.TAC;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class Asm extends Phase<TAC.Prog, String> {

    protected final AsmEmitter emitter;

    protected final RegAlloc regAlloc;

    public Asm(AsmEmitter emitter, RegAlloc regAlloc, Config config) {
        super("asm: " + emitter.toString(), config);
        this.regAlloc = regAlloc;
        this.emitter = emitter;
    }

    @Override
    public String transform(TAC.Prog prog) {
        var analyzer = new LivenessAnalyzer<PseudoInstr>();

        for (var vtbl : prog.vtables) {
            emitter.emitVTable(vtbl);
        }

        var pw = new PrintWriter(System.out);

        emitter.emitSubroutineBegin();
        for (var func : prog.funcs) {
            pw.println("In function " + func.entry);
            var pair = emitter.selectInstr(func);
            var builder = new CFGBuilder<PseudoInstr>();
            var cfg = builder.buildFrom(pair.getLeft());
            analyzer.accept(cfg);
            cfg.printLivenessTo(pw);
            pw.flush();
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
