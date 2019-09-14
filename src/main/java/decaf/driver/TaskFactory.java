package decaf.driver;

import decaf.backend.asm.Asm;
import decaf.backend.mips.MipsAsmEmitter;
import decaf.backend.reg.BruteRegAlloc;
import decaf.instr.tac.TAC;
import decaf.parsing.Parser;
import decaf.tacgen.TacGen;
import decaf.tree.Tree;
import decaf.typecheck.Namer;
import decaf.typecheck.Typer;

import java.io.InputStream;

public class TaskFactory {
    private final Config config;

    public TaskFactory(Config config) {
        this.config = config;
    }

    public Task<InputStream, Tree.TopLevel> parse() {
        return new Parser(config);
    }

    public Task<InputStream, Tree.TopLevel> typeCheck() {
        return parse().then(new Namer(config)).then(new Typer(config));
    }

    public Task<InputStream, TAC.Prog> tacGen() {
        return typeCheck().then(new TacGen(config));
    }

    public Task<InputStream, String> mips() {
        var emitter = new MipsAsmEmitter();
        return tacGen().then(new Asm(emitter, new BruteRegAlloc(emitter), config));
    }
}
