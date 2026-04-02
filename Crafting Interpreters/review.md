Lox 라는 고수준 스크립트 언어의 스펙을 정의하고 구현하는 것으로 프로그래밍 언어 전반에 대한 메타지식과 함께 내가 작성한 코드가 어떻게 동작하는지에 대한 통찰을 얻을 수 있다.
책을 통해서 구문(Syntax), 표현식, 문장, 파싱, 컴파일링, 인터프리팅과 같은 용어, 클로저, 해시테이블과 GC의 동작 메커니즘 등 프로그래밍을 하면서 계속해서 의문을 품었던 것들에 대해 정확하게 이해할 수 있다.
언어의 스펙을 정의하고 Java와 C를 사용해서 그 언어의 스펙을 따르는 두가지 버전의 인터프리터 JLox와 CLox를 구현한다.
언어의 각 스펙에 따라서 유저의 소스코드의 표현을 변경하고(파싱/컴파일) 이를 실행(인터프리팅)하는 구현을 반복적으로 진행하는 구조를 가진다.

재귀적 문법을 비교적 직관적으로 반영하고 있는 구문트리(AST)라는 중간 표현으로 파싱하는 JLox 를 구현한다. 
JLox의 인터프리터는 AST를 재귀적으로 순회하며 실행한다.
그 다음 성능을 위해서 바이트코드라는 중간 표현으로 파싱(컴파일) 하는 CLox를 구현한다. 
바이트코드는 가상머신(VM)에서 실행될 수 있는 플랫한 명령어의 시퀀스이다. CLox의 VM은 이 바이트코드 뭉치를 실행한다.
여기서 성능의 트레이드오프는 복잡성이다. 

CLox를 구현하는 부분이야말로 이 책의 화룡점정이다. JAVA와 같은 고수준 언어에서 제공하는 기능들이 없기 때문에 해시테이블부터 가비지 컬렉터까지 직접 구현해야 한다. 
컴파일러와 컴파일의 결과물인 바이트코드 뭉치를 실행하는 VM은 스택을 통해서 긴밀하게 결합한다.
응용 코드만 작성하다가 정말 어려운 메커니즘을 이해하고 코드로 작성한다는 희열이 있고, 얻기 어려운 통찰이 있다.
프로그램의 동작이 스택 자료구조의 LIFO 동작과 정확히 일치하는것을 보면서 스택이라는 자료구조의 유용함을 몸소 체험할 수 있다.
함수와 클로저 기능을 추가하면서 레이어를 추가하고 그곳에 VM 이 필요로 하는 정보를 담는 설계가 인상적이었다.
컴파일러와 VM에서는 성능 자체가 본질적인 복잡성을 만든다.
스택의 슬롯 배치를 컴파일러와 VM이 동시에 합의해야 하는 구조에서, 자연히 구현 요소가 강하게 결합되고,
응용 코드에서 통하던 모듈 분리같은 전략은 효과적이지 않고, 오직 코드를 짧고 간결하게 작성하는게 더 효과적이라는 작은 깨달음도 얻었다.

저자의 유머와 멋진 문장들로 프로그래밍의 가장 어려운 부분을 하나씩 격파해 갈 수 있게 해주는 매우 훌륭한 책이다.
언어를 직접 구현해 보는 것은, 매일매일의 코딩에서도 내 시야를 스택의 저 아래까지 넓혀준 값진 경험이었다.


# 인상 깊은 구절
- You can think of the compiler as a pipeline where each stage’s job is to organize the data representing the user’s code in a way that makes the next stage simpler to implement.
  - (컴파일링에 관한 통찰)
- If we added instance methods to the expression classes for every one of those operations, that would smush a bunch of different domains together. That violates separation of concerns and leads to hard-to-maintain code.
  - (data 클래스를 사용해야 하는 경우에 대한 통찰)
- Parameters are core to functions, especially the fact that a function encapsulates its parameters—no other code outside of the function can see them. This means each function gets its own environment where it stores those variables.
  - (함수에 관한 통찰) 
- In our interpreter, environments are the dynamic manifestation of static scopes.
- The main goal is to bundle data with the code that acts on it.
  - (OOP의 기본 교리)
- Inheriting from another class means that everything that’s true of the superclass should be true, more or less, of the subclass. In statically typed languages, that carries a lot of implications.
  - (Liskov 치환에 대한 다른 설명)
- Lox splits statements into two categories. “Declarations” are those statements that bind a new name to a value. The other kinds of statements—control flow, print, etc.—are just called “statements”.
- Every place in the compiler that was writing to the Chunk now needs to go through that function pointer. Fortunately, many chapters ago, we encapsulated access to the chunk in the currentChunk() function. We only need to fix that and the rest of the compiler is happy.
  - (캡슐화의 이로움)
- Closures capture variables. You can think of them as capturing the place the value lives.
  - (클로저가 포착 하는 것. JAVA 는 값을 포착한다.)
- We say Lox is a “high-level” language because it frees programmers from worrying about details irrelevant to the problem they’re solving.
  - (고수준 언어란 무엇인가)

- 데이터 표현의 중요함 -> 결국 어떤 구현을 원할히 하기위해서 다른 표현을 만드는것