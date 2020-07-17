// These are various functions called by decaf programs.
//
// For MIPS their instructions are hardcoded, too bad.
//
// For x86-32 (i386), we implement the functions as a language runtime in C,
// and everything work as below:
//
//     +--------------------+            +------------------+
//     | Your Decaf Program |            | language runtime |
//     +---------+----------+            +---------+--------+
//               |                                 |
//               | The decaf compiler              | gcc -m32 -S
//               |                                 |
//               |                                 |
//               v                                 v
//     +--------------------+            +------------------+
//     | i386 assembly      |            | i386 assembly    |
//     +---------------\----+            +---/--------------+
//                      \                   /
//                       \                 /
//                        \               /  gcc -m32
//                         \             /     (actually invoking the assembler `as`
//                          \           /                     and the linker `ld`)
//                           v         v
//                   +----------------------------+
//                   | i386 executable code       |
//                   +-------------+--------------+
//                                 |
//                                 | run by your PC
//                                 |
//                                 v

#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include <string.h>


void _PrintInt(int a) {
    printf("%d", a);
}

void _PrintString(const char *a) {
    printf("%s", a);
}

void _PrintBool(int x) {
    printf("%s", x ? "true" : "false");
}

void* _Alloc(int sz) {
    void* ret = malloc(sz);
    if (ret == NULL) assert(0);
    //printf("_Alloc(%d) -> %p\n", sz, ret);
    return ret;
}

int _ReadInteger() {
    int x;
    scanf("%d", &x);
    return x;
}

void _Halt() {
    exit(1);
}

int _StringEqual(const char *a, const char *b) {
    return (!strcmp(a, b));
}

#define READ_LINE_LEN 1024
const char* _ReadLine(void) {
    // delibrate memory leak
    char *a = malloc(READ_LINE_LEN);
    assert(a != NULL);
    return fgets(a, READ_LINE_LEN, stdin);
}
