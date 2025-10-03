#include <stdio.h>
#include <string.h>

#include "memory.h"
#include "value.h"
#include "object.h"

void initValueArray(ValueArray* array) {
  array->values = NULL; // 포인터는 Null 로 초기화 되고 이후, writeValueArray->GROW_ARRAY->malloc 에 의해서 공간이 할당된다.
  array->capacity = 0;
  array->count = 0;
}

void writeValueArray(ValueArray* array, Value value) {
  if (array->capacity < array->count + 1) {
    int oldCapacity = array->capacity;
    array->capacity = GROW_CAPACITY(oldCapacity);
    array->values = GROW_ARRAY(Value, array->values, oldCapacity, array->capacity);
  }

  array->values[array->count] = value;
  array->count++;
}

void freeValueArray(ValueArray* array) {
  FREE_ARRAY(Value, array->values, array->capacity);
  initValueArray(array);
}

void printValue(Value value) {
  switch (value.type) {
    case VAL_BOOL:
      printf(AS_BOOL(value) ? "true" : "false");
      break;
    case VAL_NIL: printf("nil"); break;
    case VAL_NUMBER: printf("%g", AS_NUMBER(value)); break;
    case VAL_OBJ: printObject(value); break;
  }
}

bool valuesEqual(Value a, Value b) {
  if (a.type != b.type) return false;
  switch (a.type) {
    case VAL_BOOL:    return AS_BOOL(a) == AS_BOOL(b);
    case VAL_NIL:     return true;
    case VAL_NUMBER:  return AS_NUMBER(a) == AS_NUMBER(b);
    case VAL_OBJ:     return AS_OBJ(a) == AS_OBJ(b); // 현재 OBJ 는 string 밖에 없다. 모든 string object 는 이제 interned 됨으로 단순히 주소 비교로 valueEqual 을 계산 할 수 있다.
    default:          return false; // Unreachable
  }
}