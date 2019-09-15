package decaf.lowlevel.tac;

public interface Opcodes {

    // 0-address
    int NOP = 0;
    int RETURN_VOID = 1;

    // 1-address
    int BRANCH = 2;
    int RETURN = 3;
    int PARM = 4;
    int MARK = 5;
    int MEMO = 6;
    int INDIRECT_CALL_VOID = 7;
    int DIRECT_CALL_VOID = 8;

    // 2-address
    int ASSIGN = 9;
    int LOAD_IMM4 = 10;
    int LOAD_STR_CONST = 11;
    int LOAD_VTBL = 12;
    int NEG = 13;
    int LNOT = 14;
    int INDIRECT_CALL = 15;
    int DIRECT_CALL = 16;

    // 3-address
    int ADD = 17;
    int SUB = 18;
    int MUL = 19;
    int DIV = 20;
    int MOD = 21;
    int EQU = 22;
    int NEQ = 23;
    int LES = 24;
    int LEQ = 25;
    int GTR = 26;
    int GEQ = 27;
    int LAND = 28;
    int LOR = 29;
}
