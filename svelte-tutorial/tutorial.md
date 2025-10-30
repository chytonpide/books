# What is Svelte
UI프레임워크로, 마크업, 스타일 및 동작을 결합한 컴포넌트로 앱을 선언적으로 구축할 수 있게 한다.

## Runes
룬은 svelte 컴파일러를 제어하기 위해 사용하는 심볼, 언어로 치자면 구문의 키워드(제어문의 일부가 되는) 이다. 
반응성의 구조를 명시적으로 표현하기 위해서 Svelte5 부터 도입
- $state: $state 룬은 반응형 상태를 생성할 수 있게 해주며, 이는 상태가 변경될 때 UI가 반응함을 의미한다.
- $derived: 파생된 상태로, state 값이 변경될 때 같이 변경되는 값, component 의 코드(script)는 생성될때 한번만 실행된다. 
- $effect: 이펙트는 상태가 업데이트될 때 실행되는 함수이다. 이들은 오직 브라우저에서 실행된다.
- $props: `export let varName` 의 룬 버전
- $bindable: 템플릿 구문에서 bind: 지시어를 사용할 수 있음을 명시적으로 지정
- $inspect: 개발시 사용, 값이 변할때만 출력된다.
  - {@debug ...} 문장은 개발자 도구가 열려있으면 break point 로 동작한다.
- $host: 부모에서 bind:this 지시어를 사용해서 변수에 할당한 다음, 컴포넌트에서 노출한 인터페이스를 호출 할 수 있게 한다.
  - 상태 변화에 의존하지 않고, 직접 명령으로 동작을 제어하는게 어울리는, imperative 제어가 필요한 컴포넌트에 사용.
    - ex)
```js
  modalRef.open();
  modalRef.close();
  player.play()
```
## Runtime
- Stores: 전역 스코프 값 저장소.
- Context: 부모 스코프에 접근할 수있는 값 저장소.
- Lifecycle hooks: onMount, onDestroy, tick: 반응성 업데이트가 DOM에 적용된 뒤에 실행되는 Promise -
  - tick ex)
```js
function showModal() {
  visible = true;
  const modal = document.querySelector('.modal'); // ❌ 아직 존재하지 않음(dom 업데이트 안됨)
}

async function showModal() {
  visible = true;
  await tick(); // ✅ DOM 반영 완료 대기
  const modal = document.querySelector('.modal');
  modal.focus();
}
```


# Note
- 스벨트 파일구조
  - script
  - html
  - style
- 상태변수를 그냥 선언만 해주면 된다.
- 스벨트는 객체 내부 변경에 의해서 dom 을 업데이트 하지 않는다. (반응성을 트리거하지 않는다.)
- 최상위 변수의 재 할당에 의해서만 반응성을 트리거한다.
- 최상위 변수의 인덱스 접근후 할당의 경우는 반응성을 트리거한다.
- lib 에는 여러 컴포넌트에서 사용하는 공통 코드를 저장한다.
- 컴포넌트: 재사용이 가능하도록 분리한 UI 요소, 컴포넌트는 입력값을 통해 구성되는 UI 요소, 재사용하지 않더라도 책임에 따라 분류
  - Props 는 왜 Props 인가
    - `<MyButton color="red" size="large" />` 에서 color 등을 HTML attribute 라고 불름, MyButton 넘기는 파라미터 아규먼트 쌍을 props 라고 부르게 되었다.
    - 부모에서 자식으로의 단방향, 읽기 전용 
    - 부모의 데이터를 변경할 때는 부모에게 요청을 해야함은 부모에게 받은 callback 을 실행 해야함. (svelte 에서는 bind 라는 지시어를 사용할 수 있음)
    - Props 를 전달할 때, 이름이 같다면 생략 할 수 있다.
- transition: DOM 에 element 나가거나 들어올때 트리거 된다. bidirectional 이다.
  - in, out 따로 지시할 수도 있다. 
- `$: document:title = title;`
  - 리엑티브 문장: 의존 하는 값이 변할 때, 스크립트의 코드가 실행되고, 마크업이 랜더링 되기전에 실행되고, 그 이후에는 의존하는 값이 변경될 때마다 실행된다.
  - useEffect: 명령적(imperative): deps 를 직접 지정
  - $: 선언적(declarative): 정적분석을 통해 자동으로 추론 후 동작
- `let data_temp = [...data]`
  - spread operator (펼침 연산자): 원본 데이터의 복사본을 만든다.