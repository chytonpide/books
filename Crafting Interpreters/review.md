- 언어에 대한 메타지식을 소개
- 프로그래밍 언어의 계열 소개
- stack 과 heap 의 용도에 대해서 알게된다. stack 프로그램이 기동되면서 추적해야 하는 값의 행동은 stack 자료구조의 행동과 정확히 일치한다는 것도 알게된다.
- 컴파일타임과 런타임에서 공유되는 데이터들, 하나의 모듈 안에서 이루어지는 복잡한 상태의변화. 메서드를 쪼개거나 하는것으로 이해가 쉬어지거나 하지 않다. 어떤 메카니즘을 간결한 코드로 구현하는걸 목적으로 하는 것 같다.


※ variable(): IDENTIFIER를 읽는 파싱용 래퍼, 토큰을 읽어서 namedVariable 로 넘김
※ namedVariable(): 이 이름이 어디(로컬/글로벌/업밸류)에 바인딩되는지 결정하고 GET/SET 바이트코드를 생성
※ identifierConstant(): 식별자 이름(토큰)을 “상수 풀(constants)”에 문자열로 등록하고, 그 상수 인덱스(constant index)를 얻는 함수
    - 
※ 이 이름이 어디에 바인딩 되는지 결정한다: 소스코드에 나온 식별자가 실행중에 어떤 저장소 (slot) 을 가르키는지 컴파일 타임에 정한다.
    - 저장소는 VM이 런타임에 값을 보관하는 실제 자리를 의미, 컴파일러는 globals, stack 의 index 를 옵코드오 함께 전달한다.  


※ Compiler.locals 와 CallFrame.slots 의 인덱스는 연동된다.
※ Compiler.upvalues 와 ObjClosure.upvalues 는 연동된다.
※ OP_CODE 와 같이 전달되는 인수는 READ_BYTE(), READ_STRING() 로 바로 읽는다. pop(), peek() 등은 이전 instruction 이 실행된 결과 stack 에 값이 올라가 있을 때 사용한다.
