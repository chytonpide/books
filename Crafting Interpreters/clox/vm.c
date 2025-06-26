#include <stdio.h>

#include "common.h"
#include "debug.h"
#include "vm.h"


// 매번 vm 포인터를 넘기느니 그냥 전역변수로 선언한다. 이 설계는 지면을 줄이기위한 것이다.
// 함수들이 vm 포인터를 받도록 하면 vm 포인터를 호스트 어플리케이션에게 노출함으로 더 많은 것들(vm 을 복수로 사용하기, 포인터위치 확인하기)이 가능해 진다.
VM vm;


void initVM() {
}

void freeVM() {
}

static InterpretResult run() {
#define READ_BYTE() (*vm.ip++)
#define READ_CONSTANT() (vm.chunk->constants.values[READ_BYTE()])

  for (;;) {
#ifdef DEBUG_TRACE_EXECUTION
  disassembleInstruction(vm.chunk, (int)(vm.ip - vm.chunk->code))
#endif
    uint8_t instruction;
    switch (instruction = READ_BYTE()) {
      case OP_CONSTANT: {
        Value constant = READ_CONSTANT();
        printValue(constant);
        printf("\n");
        break;
      }
      case OP_RETURN: {
        return INTERPRET_OK;
      }
    }
  }

#undef READ_BYTE
#undef READ_CONSTANT
}

InterpretResult interpret(Chunk* chunk) {
  vm.chunk = chunk;
  vm.ip = vm.chunk->code;
  return run();
}

