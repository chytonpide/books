# Chapter 3. Data Models and Query Languages
- Data models are perhaps the most important part of developing software, because of the profound effect they have not only on how the software is written, but also on how we think about the problem that we are solving.
- Most applications are built by layering one data model on top of another. For each layer, the key question is how it is represented in terms of the next-lower layer.
- In a complex application there may be more intermediary levels, such as APIs built upon APIs, but the basic idea is still the same: each layer hides the complexity of the layers below it by providing a clean data model. 

- Declarative Query Language: 우리가 사용하는 SQL 은, 사용자가 원하는 데이터의 패턴 — 결과가 충족해야 할 조건과 데이터가 어떻게 변환되어야 하는지을 명시하지만, 그 목표를 달성하는 구체적인 방법은 명시하지 않는다.
  - 이 선언적 언어는 쿼리 엔진의 구현 디테일을 숨긴다. 쿼리를 수정할 필요 없이 데이터베이스 시스템이 성능을 개선할 수 있게 해준다.
  - 선언형 : 원하는 결과를 설명하는 형태, 의미가 보존 되려면!?