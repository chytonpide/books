# Chapter 8. Transaction
트랜잭션은 수십년간 어플리케이션에서 데이터를 처리하는데 수반되는 여러 문제를 단순화 시키는 메카니즘으로 선택되어 왔다.
트랜잭션은 어플리케이션이 몇가지 읽기들과 쓰기들을 함께 논리적 단위로 묶는 방법이다.
개념적으로 한 트랜잭션 내의 읽기들과 쓰기들은 하나의 오퍼레이션으로 실행되며 커밋으로 성공하거나 중지로 함께 실패한다.

데이터베이스에 접근하는 어플리케이션의 프로그래밍 모델을 단순화 하기 위해서 만들어졌다.
트랜잭션을 사용하면 동시성 문제나 특정한 잠재적 오류를 데이터베이스가 대신 처리함으로 어플리케이션은 이것들을 무시할 수 있다.

언제 트랜잭션이 필요한지 판단하기 위해서는 트랜잭션이 제공할 수 있는 정확한 안전성 보장 사항과 그에 수반되는 비용을 이해해야 한다.

# 목차
- What Exactly Is a Transaction?
  - The Meaning of ACID
  - Single-Object and Multi-Object Operations
- Weak Isolation Levels
  - Read Committed
  - Snapshot Isolation and Repeatable Read
  - Preventing Lost Updates
  - Write Skew and Phantoms
- Serializability
  - Actual Serial Execution
  - Two-Phase Locking
  - Serializable Snapshot Isolation
- Distributed Transactions
  - Two-Phase Commit
  - Distributed Transactions Across Different Systems
  - Database-Internal Distributed Transactions
  - Exactly-Once Message Processing Revisited

# What Exactly Is a Transaction?
- 2000 년대 초반에 객체에 적합한 문서모델을 사용하는 NoSQL 이 유행하면서, 대규모 시스템에서 성능과 가용성을 확보하기 위해서 트랜잭션 을 배제하려는 움직임이 있었지만, 
  Spanner 같은 DB 를 통해, 컨센서스 프로토콜과 샤딩을 조합한 방법으로, 대규모 시스템에서 트랜잭션을 지원하면서 확장성을 가질 수 있게 되었다.
## The Meaning of ACID
- 트랜젝션이 제공하는 safety guarantees 는 ACID(에시드) 라는 머리글자로 표현된다.
  - atomicity, consistency, isolation, durability
### Atomicity (원자성) 
- 몇개의 쓰기 작업을 트랜잭션으로 묵었을 때, 일부가 실패했을 때, 트랜잭션의 모든 쓰기 작업이 수행되지 않도록 하는 능력
- 다른 말로 abortablity 라고 말할 수 있다.
### Consistency (일관성)
- ACID 에서의 consistency 는 데이터 베이스가 좋은 상태에 있다는 어플리케이션에 특정한 개념이다.
- ACID 에서의 consistency 는 데이터가 불변식을 만족하는 특정한 상태에 있어야 한다는걸 의미한다.
  - 데이터 베이스가 불변식을 만족 하도록 하려면 스키마에 제약사항을 정의해야한다. 
    복잡한 불변식은 데이터베이스를 통해서 모델링 할 수 없는데, 이러한 경우 일관성을 보존하기 위해서 트랜잭션을 정의하는 책임은 어플리케이션의 진다.
### Isolation
- 데이터베이스의 어떤 레코드에 클라이언트가 동시에 접근한다면 레이스 컨디션이라는 동시성 문제가 발생할 수 있다.
- ACID의 isolation은 동시에 실행되는 트랜잭션들이 서로 간섭하지 않는다는 보장이다. 이상적으로는 트랜잭션을 하나씩 순서대로 실행한 것과 같은 결과를 낸다(serializability).
- serializability 는 성능 비용이 크기 때문에, 실제론, 많은 데이터베이스가 serializability 보다 약한 고립 형태를 사용한다. 이러한 형태에서 동시 실행되는 트랜잭션은 제한된 방법으로 서로에게 영향을 준다.
### Durability
- ACID 의 Durability 는 트랜젝션 commit 된 후에, 하드웨어 결함이나 데이터베이스의 비정상적인 종료에도 쓰여진 데이터가 소실 되지 않는 다는 보장이다.
  - 단일 노드 데이터베이스에서 내구성이란 일반적으로 데이터가 하드 드라이브나 SSD와 같은 비휘발성 저장 장치에 기록되는걸 의미한다.  
    - 이를 위해 fsync, WAL 을 사용한다.
  - 복제된 데이터베이스에서 내구성이란, 데이터가 특정한 수 의 노드에 복사 되었다는걸 의미한다. 
    - 내구성을 보장하기 위해서는 데이터베이스가 이러한 쓰기 작업이나 복제 작업이 완료될 때까지 기다린 후에야 트랜잭션이 성공적으로 커밋되었다고 보고해야 한다.
## Single-Object and Multi-Object Operations
- atomicity: 트랜잭션에서 에러가 발생했을 때, 도중까지 이뤄진 쓰기 작업을 파기하고 이전 상태로 돌아가는 '모두 아니면 전무'를 보증한다.
  - atomicity 가 없으면 쓰기 작업에서, 일부만 성공하는 것으로 일관성을 가져아 하는 레코드들이 그것을 잃어버릴 수 있다.
- isolation: 동시에 수행되는 트랜잭션은 서로 간섭해서는 안된다. 한 트랜잭션이 몇개의 쓰기작업을 가지고 있을 때, 다른 트랜잭션은 그 쓰기작업들의 일부가 아니라, 전부 아니면 전무를 봐야한다.
  - isolation 이 없으면, dirty read 문제가 발생한다.
- Multi-object transactions (다중 객체 트랜젝션) 에서는, 어떤 읽기 및 쓰기 작업이 같은 트랜잭션에 속하는지 판단할 방법이 필요하다. 
  이는 보통 `BEGIN TRANSACTION` and a `COMMIT` 사이에 이루어지는 요청은 한 트랜잭션에 속하는 것으로 본다.
- nonrelational databases 는 이렇게 오퍼레이션을 묶는 수단이 없다. 
### Single-object writes
- 거의 모든 스토리지 엔진은 객체 하나의 단위에 atomicity 랑 isolation 을 기본적으로 제공한다.(여러 객체를 하나의 단위로 묶는 일반적인 의미의 트랜잭션과는 구분된다.)
  - Atomicity can be implemented using a log for crash recovery
  - isolation can be implemented using a lock on each object (allowing only one thread to access an object at any one time).
- 동시성 프로그래밍의 atomic: 연산이 쪼개지지 않아서 다른 쓰레드가 중간에 끼어들 수 없다라는 의미의 atomic 도 쓰임으로 atomic increment 는 isolated increment 여야 한다.
### The need for multi-object transactions
- multi-object transactions 이 필요한 경우
  - 서로 참조하는 몇몇 레코드를 삽입할 때, 이러한 참조가 유효하게 유지되도록 한다.
  - 비정규화된 정보가 여러 오브젝트에 걸쳐 있을 때, 싱크가 깨지는 것을 막는다.
  - secondary index 가 존재 할때, 값을 변경할 때 이도 함께 변경되도록 한다.
- 트랜잭션 없이도 어플리케이션이 구현될 수 있지만, 원자성 없이는 에러헨들링이 힘들어지고, 격리성이 없이는 동시성 문제를 야기할 수 있다.
### Handling errors and aborts
- 트랜잭션의 핵심 특징 중 하나는 오류가 발생했을 때 트랜잭션을 중단하고 안전하게 재시도할 수 있다는 점이다.
- ACID 를 제공하는 데이터베이스는 다음과 같은 철학에 기반하고 있다. "데이터베이스가 원자성, 격리성 또는 내구성 보장을 위반할 위험이 있을 경우, 트랜잭션을 반쯤 완료된 상태로 남겨두기보다는 차라리 트랜잭션을 완전히 포기한다."
- 재시도를 지원하는데 트랜잭션이 실패하면 그냥 예외로 던저버리는것은 수치다.

----

# Weak Isolation Levels
- 동시성 문제는 해결하기 어려운 문제이다. 데이터베이스는 트랜잭션 격리성 (transaction isolation.) 을 제공하는 것으로 이 이슈를 숨기려 해왔다.  
  트랜잭션이 순차적으로 실행되는걸 보장하는 serializable isolation 이 있지만 성능 비용이 있음으로, 일반적으로 이보다 낮은 격리 수준(weaker levels of isolation)을 사용한다.

## Read Committed
- Read Committed 는 가장 기본적인 격리 수준으로 다음을 보장한다.
  - no dirty reads: 데이터베이스에서 데이터를 읽을 때, 커밋된 데이터만 볼 수 있다.
  - no dirty writes: 데이터베이스에서 데이터를 쓸 때, 커밋된 데이터만 쓸 수 있다.
### No dirty reads
- dirty reads: 한 트랜잭션이 아직 커밋하거나 중단하지 않은 쓰기를, 다른 트랜잭션이 읽는 것.
  - 쓰기를 하는 트랜잭션이 중단되면, 읽기 트랜잭션은 일관성을 잃은 값을 얻게 된다.
  - 커밋되더라도, 여러 객체를 바꾸는 트랜잭션의 일부만 반영된 중간 상태를 볼 수 있다.
### No dirty writes
- dirty writes: 두개의 트랜잭션이 동시에 같은 행을 업데이트 하려고 할 때, 먼저 수행된 한 트랜잭션의 쓰기가 커밋되지 않은 상태에서 나중에 수행된 트랜잭션의 쓰기가 그 값을 덮어 씌우는 것.
  - 트랜잭션이 여려행을 업데이트 하는 경우, 행마다 서로 다른 트랜잭션의 쓰기가 뒤섞여, 값들은 일관성을 잃게 된다.
- 두개의 트랜잭션이 동시에 데이터 베이스의 같은 행을 업데이트 하려고 할 때, Read Committed 에서는 첫 번째 쓰기 트랜잭션이 커밋되거나 중단될 때까지 두 번째 쓰기를 지연시켜 더티 라이트를 방지한다.
- read-modify-write 경합은 방지할 수 없다. 
  - select for update 를 사용하면 방지할 수 있다. read-modify-write를 하는 쪽이 모두 FOR UPDATE로 읽으면, 두 번째 트랜잭션은 읽는 단계에서 기다리게 된다.
### Implementing read-committed
- 많은 데이터베이스에서 read-committed 는 기본 격리수준이다. 쓰기에 대해서는 row 수준의 lock 을 사용한다. 읽기에 대해서는 row 를 lock 하지는 않고, 가장 마지막에 커밋된 값(snapshot)을 돌려준다.
  - 몇몇 데이터베이스는 읽기에 대해서도 row 를 lock 할 수 있다. `read_committed_snapshot=off`
  - PostgreSQL, Oracle default: read-commited
  - Mysql default: repeatable read
  - SQLite: serializable

## Snapshot Isolation and Repeatable Read
- 같은 시점에 읽어야 일관성이 있는 행들이 있는데, 읽기 사이에 이 행들의 변경이 있으면 모순된 값을 관찰 할 수 있다. 보통 다시 읽는걸로 해결이 되지만, backup 이나, 진실성 검사, 데이터 분석에서는 문제가 될 수 있다.
  - 이러한 문제를 read skew 라고 한다. (nonrepeatable read 의 한 종류)
- 이런 문제를 해결하기 위한 격리 수준이 Snapshot Isolation 으로, 각 트랜잭션이 데이터베이스의 일관된 상태의 스냅샷으로 읽는것을 보장한다.
### Multiversion concurrency control
- snapshot Isolation 은 dirty read 를 방지하기 위해서 사용했던 메카니즘의 일반화된 버전인 Multiversion concurrency control 을 사용해서 구현한다.
  이는 다양한 트랜잭션이 서로 다른 시점의 데이터 베이스의 상태를 확일 할 수 있도록 한 행에 대해서 여려개의 커밋된 버전을 유지한다.
- 각각의 트랜잭션에 증분하는 tx-id 를 부여하고, 행의 변경을 기록해 두는 것으로 어떤 버전의 값을 사용할지 결정할 수 있다.
- GC 를 통해서 더이상 필요없어진 이력을 삭제한다.
### Visibility rules for observing a consistent snapshot
- 증분하는 tx-id 를 통해서 read 에서 어떤 것을 볼 수 있고 볼 수 없는지 결정할 수 있다.
- 값이 변경될 때 마다 버전을 삽입해서 이력 리스트를 유지하는 것으로 일관된 스냅샷을 제공할 수 있다. 
### Indexes and snapshot isolation
- 다중 버전 데이터베이스에서 인덱스를 구현하는 일반적인 방식은, 인덱스가 여러 버전의 행중 하나를 가르키고 그 행의 버전을 순회하며 값을 찾게 하는 것이다.
### Snapshot isolation, repeatable read, and naming confusion
- 용어가 의미하는게 통일되어 있지 않다. Snapshot isolation 은 postgreSQL 의 "repeatable read" 라고 부르고, Oracle 에서는 "serializable" 이라고 부른다.
- postgreSQL 의 repeatable read 는 제대로된 Snapshot isolation 이지만, Mysql 의 repeatable read 는 Snapshot isolation 의 보장보다 약간 약하다.
- repeatable read: 같은 트랜잭션에서 여러번 값을 여러번 읽어도 된다. 즉 다른 트랜잭션이 그 사이에 어떤 값을 변경하고 커밋해도, 같은 트랜잭션 내에서는 변경된 값을 보지 못하고 트랜잭션이 시작했을 때의 값으로 읽는다.  


## Preventing Lost Updates
- lost update: 트랜잭션이 서로 간섭하는 dirty write 가 없다고 해도. 읽기-수정-쓰기 사이클에서 동시의 두개의 트랜잭션이 실행되면, 첫번째 트랜잭션의 업데이트가 나중의 업데이트에 의해서 덮어쓰여진다(사라진다). 
  예를들어 증분 업데이트를 해야하는 상황에서는 이것이 문제가 된다. 
### Atomic write operations
- 어플리케이션에서 read-modify-write cycles 로 구현하는 것 대신에 DB 의 원자 업데이트 오퍼레이션을 (atomic update operation) 사용하는 것으로 이 문제를 해결할 수 있다. 많은 DB 가 atomic operation 을 지원하고 일반적으로 이것을 사용하는 것이 가장 좋은 선택이다. 
- locking 이나 single thread 를 사용하는 것으로 구현된다.
- ORM 을 사용하는 경우, DB 의 atomic update operation 를 사용하지 않고. read-modify-write cycles 의 코드를 쓰기 쉬움으로 주의해야 한다.
### Explicit locking (명시적 라킹)
- select for update : 쿼리 결과로 돌아오는 모든 row 를 lock 하고, 다른 트랜잭션은 lock 이 풀릴 때까지 기다린다.
- 여러 개체에 잠금을 설정하면 두 개 이상의 트랜잭션이 서로의 잠금 해제를 기다리는 교착 상태(dead lock)가 발생할 위험이 있다.
  - ※　select for update, update, delete 문은 lock 된 row 를 기다린다. 그냥 select 는 커밋된 값을 바로 읽는다.  
- 많은 DB 는 교착을 감지하면 둘중 하나의 트랜잭션을 중단 시킨다. 어플리케이션에서 재시도를 설정함으로 이런 상황을 처리할 수 있다.
### Automatically detecting lost updates
- 트랜잭션 매니저가 lost update 를 감지하도록 할 수 있다. lost update 가 감지되면 해당 트랜잭션을 중단하고 read-modify-write cycle 을 다시 수행하도록 할 수 있다.
- 어플리케이션 레벨에서 atomic operation 을 사용하거나, 명시적인 lock 을 사용하는걸 신경쓰지 않아도 되지만, 재시도는 어플리케이션 레벨에서 설정해야한다.
- snapshot isolation 과 연계하면 lost update 검사를 효율적으로 할 수 있지만, 모든 DB 에서 지원하는건 아니다. (Mysql 은 안된다.)
### Conditional writes(compare-and-set)
- transaction 을 제공하지 않는 DB 에서, lost update 를 방지하기 위한 수단으로 conditional write operation 이 사용되는걸 볼 수 있다.
- 이는 CPU 의 CAS(compare and swap) operation 의 DB 버전으로 수정하려는 값이 내가 직전에 읽은 값과 같을 때만 수정하도록 하는 것이다.
- 이는 종종 optimistic locking 이라고 불린다.
```
UPDATE wiki_pages SET content = 'new content' WHERE id = 1234 AND content = 'old content';
```
- MVCC 의 가시성 규칙에 의하면, 다른 트랜잭션이 나중에 쓴 값은 읽을 수 없지만, 많은 MVCC 의 구현이 UPDATE, DELETE 문의 WHERE 절은 최신값을 읽도록 예외를 둔다.
### Conflict resolution and replication
- multi-leader 나 leader-less replication 의 경우, lock 이나 atomic operation 을 사용할 수 없다.
- 대신 동시 쓰기 작업을 허용해서 여러버전의 값을 만든 후에 나중에 어플리케이션 레벨에서 충동을 해결하는게 일반적인 접근 방법이다.
  - 머지 충돌 해결은, 업데이트가 교환적(commutative)인 경우, 단순히 값들을 합치는 것으로 lost update 를 막을 수 있다.
    - CRDT(conflict-free replicated datatypes) 이 교환법칙을 따르도록 설계되어 있다. 
  - LWW 충돌 해결은, 당연히 lost update 가 발생하기 쉽다. 

## Write Skew and Phantoms
- 최소한 1명이 당직을 서야 한다는 제약사항이 있을 때, 당직 후보인 두 의사가 동시에 비번으로 변경하는 경우, 각각의 레코드를 수정하면서 둘다 비번이 된다.
  이는 제약을 위반한다. 
※ race condition(경합 상태 a.k.a race hazard) : 타이밍에 따라 예상치 못한 결과나, 일관성 없는 결과를 초래하는 결함.
### Characterizing write skew
- write skew: 두 트랜잭션이 같은 오브젝트를 읽고, 서로 다른 두 오브젝트를 업데이트 할때 예상치 못한 결과를 야기하는 문제. dirty write 나 lost update 와는 다르다.
- lost update 를 일반화 한 문제라고 볼수 있으나, 서로 다른 두 오브젝트를 업데이트 한다는게 다르다. 
- 해결방법
  - DB 가 지원한다면 true serializable isolation 사용하기
  - DB 에서 복잡한 제약사항을 설정하기는 힘듬으로 materialized views 같은 별도의 장치를 사용하기
  - DB 가 serializable isolation 를 지원하지 않는다면 select for update 로 제약사항을 포함한 row 까지 locking 하기
    - 제약사항이 부재의 확인이면 select for update 는 locking 할 row 가 없기 때문에 사용할 수 없다. 
### More examples of write skew

### Phantoms causing write skew
- write skew 는 다음과 같은 비슷한 패턴을 가진다.
  - select 쿼리는 검색 조건에 부합하는 행을 찾아서, 제약사항이 충족되는지 확인한다.
  - 첫번째 쿼리의 결과에 따라서 어플리케이션 코드는 진행할지 말지를 결정한다.
  - 어플리케이션이 진행하기를 결정했다면, DB 에 쓰기를 하고 커밋한다. 
    쓰기의 효과로 두번째 단계의 결정의 전제조건이 변경된다.
- phantom: 한 트랜잭션의 쓰기가 또 다른 트랜잭션의 검색 쿼리의 결과를 바꾸는 현상 → write skew 를 일으키는 메카니즘
  - snapshot isolation 은 read-only 쿼리들에 대해서 phantom 을 방지한다. 하지만 read/write 트랜잭션에서는 phantom 은 write skew 를 발생시킨다.
### Materializing conflicts
- materializing conflicts: phantom 형상에서 lock 을 붙일 object 가 없다면, 인공적으로 db 에 lock 을 위한 object 을 도입해서, select for update 를 통해서 locking 이 가능하도록 하는 방식 
- 충돌을 구체화 하는 방법을 파악하는게 어렵고, 동시성 제어 메카니즘이 어플리케이션 데이터 모델로 누수되는건 바람직 하지 않음으로, 대부분의 경우 serializable isolation 격리 수준이 바람직 하다.


----

# Serializability
- write skew 문제에 맞딱드렸을 때의 문제점
  - Isolation level 은 이해하기 어렵고, DB에 따라 다르게, 일관성 없이 구현되어있다.
  - 특정한 Isolation level 에서 어플리케이션 코드가 안전한지 알기 어렵다.
  - race condition 을 탐지할 수 있는 좋은 툴이 없다. 
- 결론은 serializable isolation 을 사용하는 것이다. DB 는 모든 트랙잰션이 순서대로 실행되는것 처럼 처리해서 모든 race condition 을 방지한다.

## Actual Serial Execution
- single thread 를 통해서 serializable isolation 보장을 구현하는 방법은 의외로 최근에야 실현가능 해 졌다.  
### Encapsulating transactions in stored procedures (저장된 프로시저로의 트랜잭션 캡슐화)
- single-threaded serial transaction processing 시스템은 대화형의 다중 문장 트랜잭션을 허용하지 않고, 어플리케이션이 트랜잭션이 단일 문장만 포함되도록 하거나, 전체 트랜잭션 코드를 stored procedures 의 형태로 사전에 DB 에 제출하도록 했다.
### Pros and cons of stored procedures
- 예전에도 stored procedures 는 RDS 에서 꾀 오랬동안 사용되었지만 다음과 같은 단점이 있었다.
  - 벤더에 종속적이고 완성도가 낮은 언어
  - DB 에서 돌아가는 코드는, 관리하기(디버그, 버전관리, 배포)가 힘들었다. 그리고 모티터링을 위한 시스템과의 통합도 힘들었다.
  - DB 하나에 여러 어플리케이션 서버가 붙는 구조이기 때문에, 잘못 쓰여진 procedure 가 심각한 성능 저하를 초래할 수 도 있었다.
- VoltDB 나 Redis 에서 최근 구현된 stored procedures 들은 이러한 단점들을 극복하고 범용 프로그래밍 언어를 사용할 수 있다.
  이러한 stored procedure 와 메모리 상의 데이터로 인해서 모든 transaction 을 single thread 에서 실행 시키는것이 가능해 졌다.
  네크워크 I/O 가 없고, 동시성 제어 메카니즘을 피하는 것으로 상당한 좋은 처리량을 달성할 수 있다.
### Sharding
- sharding 을 하는 경우, 트랜잭션에서 사용하는 데이터가 여러 shard 에 걸쳐 있다면, 이것들을 조정하기 위한 추가적인 오버헤드가 필요하다.  
### Summary of serial execution
- 트랜잭션의 순차적 실행 (Serial execution of transaction) 은 다음과 같은 제약하에 serializable isolation 보장하는데 사용될 수 있다.
  - 하나의 트랜잭션이 모든 길을 막기 때문에 트랜잭션은 작고 빨라야 한다. 
  - 활성 데이터가 메모리에 올라가 있어야 한다.
  - 요구되는 쓰기처리량이 낮아야 한다. 만약 그렇지 않다면  cross-shard coordination 이 필요없도록 sharding 되어야 한다.
  - cross-shard coordination 은 가능하지만 확장하기 어렵다. 

## Two-Phase Locking
- 지난 30년간 데이터베이스에서 serializability 를 구현하기 위해 사용된 것은 2PL 이었다.
- snapshot isolation 에서는 MVCC 를 통해서, read skew 를 방지 할 수 있었다.
  여러 버전의 snapshot 을 사용하는것으로 reader 들을 writer 들을 블록하지 않고, writer 들을 reader 들을 블락하지 않았다.
- 2PL 은 아래와 같은 lock 정책으로 serializability 를 제공한다.
  - writer는 writer를 막는다: (기존 isolation level 에서도 하던 것)
  - writer는 reader를 막는다: 한쪽이 먼저 쓰기작업으로 배타 락(exclusive) 만들었다. 동시에 다른 한쪽이 읽기를 하는경우 베타 락이 있음으로 트랜잭션이 커밋되기를 기다렸다가 읽는다.        
  - reader가 writer를 막는다: 둘이 동시에 조회를 한 경우 공유(shared) 락이 만들어진다. 어느 한쪽이 쓰기 작업을 시도하는 경우 배타 락(exclusive)이 필요한데, 공유락을 트랜잭션이 공유하고 있음으로 실패한다.  
### Implementing of 2PL
- 블로킹은 각각의 오브젝트가 락을 가지는것으로 구현된다. 락은 공유 모드나 배타 모드가 될 수 있으며 아래와 같이 동작한다.
  - 트랜잭션이 오브젝트를 읽을 때 공유 모드의 락을 먼저 얻어야 한다. 몇몇 DB 는 동시에 공유 모드의 락을 가지는걸 허용하지만 다른 트랜잭션이 배타 락을 가지고 있으면 다른 트랜잭션들은 기다려야 한다.
  - 트랜잭션이 오브젝트에 쓰기를 할때, 배타 락을 먼저 얻어야 한다. 배타 락은 동시에 다른 트랜잭션이 가질 수 없음으로, 다른 트랜잭션이 락을 가지고 있으면 기다려야 한다.
  - 트랜잭션이 먼저 읽고, 쓰기를 할때, 공유락을 배타 락으로 업그레이드 해야한다. 
  - 트랜잭션이 락을 얻었다면 트랜잭션이 끝날 때 까지 락을 유지해야한다. 첫번째 페이즈에서는 락을 얻고, 두번째 페이즈에서는 트랜잭션에 종료될 때, 모든 락을 해제해야 함으로 2PL 이라고 불린다.
- 많은 락이 사용됨으로, 트랜잭션이 서로의 락이 풀리길 기다리는 dead lock 상황이 발생한다. DB 는 자동으로 이것들을 찾아서 어느 하나를 중지(abort) 시키는 것으로 진행되도록 한다.
  중지된 트랜잭션은 어플리케이션에 의해서 재시도 되어야 한다.
### Performance of 2PL
- 2PL 이 기본 설정이 아닌 이유는 그것의 성능 때문이며, 이것은 동시성을 지원할 때, 락이 발생하면 다른 한쪽을 기다리는 설계 때문이다.
- 2PL 을 사용할 때 한 긴 작업이 락을 가저가 버리면 나머지 작업들은 그것이 완료될 때 까지 기다리므로, 지연시간은 불안정하며 높은 백분위에서 매우 느려질 수 있다.
- 2PL 에서 데드락 상황이 발생해서 어느 한 트랜잭션이 중단되면 그것을 다시 실행 해야하는 부수적인 성능 문제도 있다.
### Predicate locks
- 하나의 트랜잭션이 다른 트랜잭션의 검색 결과를 바꾸는 팬텀 현상이, 쓰기 왜곡을 만듬으로 serializable isolation 에서는 이 문제를 해결해야 한다.
- 미팅룸을 예약한다고 할 때, 예약이 있는지 조회하고 예약이 존재하지 않으면 예약을 하는 오퍼레이션을 사용할 수 있다. 
  이때 부재에 대해서는 락을 걸수 없음으로 "예약이 있는지 조회" 자체에 락을 거는 것을 predicate lock 이라고 한다.
  - 쓰기를 하려는 다른 트랜잭션은 predicate lock 이 해제되기를 기다려야 한다.
  - 조회 결과에 포함되는 객체에 대한 배타 락을 다른 트랜잭션이 가지고 있따면, predicate lock 을 만들수 없고 기다려야 한다.
### Index-range locks
- 성능 문제로 predicate lock 의 단순화된 근사치인 Index-range locks 을 사용한다. 이는 predicate 가 오브젝트의 더큰 세트에 매치되도록 단순화 함으로 안전하다.
- room_id 에 index 를 설정했다면, room 123 을 찾을 때 이 index 에 접근을 한다. 이 index entry 에 잠금을 설정하는 것으로 트랜잭션이 room 123 을 조회 했다는걸 나타낼 수 있다.
  이는 범위에 대해서도 효율적으로 lock 을 만들 수 있다.
- predicate lock 보다 정확도는 떨어지지만 오버헤드가 훨씬 적음으로 좋은 타협점이 된다. 물론 적절한 인덱스가 설정되어있지 않으면 사용할 수 없다.

## Serializable Snapshot Isolation
serializable snapshot isolation: full serializability 를 제공하하는 새로운 알고리즘으로, snapshot isolation 격리 수준보다 성능저하가 미미하면서도 serializability 를 제공한다.
### Pessimistic versus optimistic concurrency control
- pessimistic concurrency control 은 문제 상황 자체가 발생하지 않도록 기다렸다가 실행하고, optimistic concurrency control 은 일단 실행하고 문제가 발생하면 그때 중단하고 다시 시도한다.
  - 경합이 낮을 때는 optimistic concurrency control 이 유리하다.
  - 경합이 높을 때는 pessimistic concurrency control 이 유리하다.
    - optimistic 의 경우 abort 가 증가하고, 재시도가 쌓일 수 있다.
- serial execution 은 pessimistic concurrency control 의 극단적 버전이다. 이는 모든 트랜잭션이 서로 배타적 락을 가지고 있는것 과 같다.
- serializable snapshot isolation 은 optimistic concurrency control 이다.
- serializable snapshot isolation 은 snapshot isolation 에 기초해서 만들어졌다. snapshot isolation 에 더해서, 읽기와 쓰기들에 걸쳐서 serialization conflict 가 발생하는걸 중단시키는 알고리즘이 추가되었다.
### Decision based on outdated premise
- serializability 가 필요한 write skew 는, 전제가 되는 쿼리가 다른 트랜잭션에 의해 변경되어서 더이상 최신이 아닐때 문제가 발생하는 패턴을 가지고 있다.
- 이를 사용해서 더이상 최신이 아닌 전제를 찾아내고 이를 바탕으로 트랜잭션의 행동을 결정할 수 있다.
  - 최신이 아닌 MVCC object version 의 읽기를 감지 (읽기 전에 이미 미확정 쓰기 작업이 발생한 경우) 
  - 이전 읽기에 영향을 미치는 쓰기를 감지 (읽기 이후에 쓰기가 발생한 경우)
### Detection of stale MVCC reads
- snapshot isolation 에서 트랙잭션은 consistent snapshot 읽기를 한다. 이는 다른 트랜잭션이 쓴 커밋되지 않은 값을 무시한다.
- 트랜잭션B가 트랜잭션A가 변경중인 오브젝트의 옛 값을 읽고 있는다는 것을 MVCC 를 통해서 알 수 있다. 트랜잭션A가 커밋된 후, 트랜잭션B가 쓰기를 한다면 전제가 변경되었음으로 트랜잭션B를 중지한다.
  - 다른 트랜잭션이 오브젝트를 변경 했어도, 해당 트랜잭션이 읽기라면 중지될 이유없이 snapshot isolation 의 메카니즘에 따라 동작한다.
### Detection of writes that affect prior reads
- 2PL 에서는 조회 쿼리에 대해서 index-range locks 을 만들었다. serializable snapshot isolation 도 비슷하게 index-range locks 를 사용하지만 다른 트랜잭션을 block 하지 않는다.
- 두 트랜잭션 A,B 에서 읽기를 할때 index-range locks 를 만든다. 트랜잭션A가 쓰기를 할때 index-range locks 을 보고, 내가한 쓰기가 있다고 마킹해 둔다. 트랜잭션A 가 커밋되고, 트랜잭션B가 커밋하려고 하면 이미 전제가 되는 쿼리는 변경되었음으로 트랜잭션B를 중지한다.
### Performance of serializable snapshot isolation
- 2PL 과 비교하면, query latency 의 분산은 적고 예측가능하다.
- serial execution 과 비교하면, 분산 시스템에도 적용할 수 있어서 쉽게 확장 가능하다.
- SSI 의 성능에 영향을 미치는 가장큰 요소는 중지 비율이다.


 

# Summary
## What Exactly Is a Transaction? (트랜잭션은 무엇인가?)
트랜잭션은 어플리케이션에 의한 DB로의 몇가지 읽기 및 쓰기 작업의 논리적은 묶음으로 데이터 베이스의 접근하는 어플리케이션의 프로그래밍 모델을 단순화 하기 위해서 만들어 졌다.
다르게 설명하면, 트랜잭션은 추상화 레이어로, 동시성 문제나 특정한 잠재적 오류를 데이터베이스가 대신 처리하는 것으로 어플리케이션으로 하여금, 이런 문제들이 없는것 처럼 숨길 수 있다.
DB 가 트랜잭션을 통해서 보장하는 안전성은 ACID 이다.
- Atomicity: 같은 트랜잭션내의 일부가 실패 했을 때, 트랜잭션의 모든 쓰기 작업이 수행되지 않도록 하는 것.
- Consistency: 데이터가 불변식을 만족하는 특정한 상태에 있도록 스키마를 통해서 강제하는 것.
  - 불변식은 어플리케이션에 특정하며, 어플리케이션이 데이터 베이스를 어떻게 사용하는지에 달려있어 데이터 베이스만으로 보장할 수 있는 것이 아니다.  
- Isolation: 트랜잰션이 서로 간섭하지 않도록 하는 것. 다른 트랜잭션의 쓰기작업의 일부를 보지 않고 전부 아니면 아무것도 볼 수 없어야 한다.
  - 이상적으로는 트랜잭션을 하나씩 순서대로 실행한 것 과 같은 결과를 내는 serializability 를 의미하지만, 실제로는 성능때문에 이보다 더 약간 고립 수준을 사용한다. 
- Durability: 트랜잭션이 커밋된 후에, 데이터베이스의 비정상적인 종료에도 쓰여진 데이터가 소실되지 않는 것.
  - 단일 노드에서 Durability 는 데이터가 비휘바성 저장장치에 기록 되는걸로 달성된다.
  - 복제된 데이터베이스에서 Durability 는, 데이터가 특정한 수의 노드에 복사되었다는걸 의미한다.

Atomicity 와 Isolation 는 Single-Object 에도 필요하고 적용된다. 예를들어서 20kb json 문서를 쓴다고 할때, 10kb 의 일부 조각만 쓰여지지 않는다.
또 10kb 정도 썼을 때, 다른 클라이언트가 그것을 읽는 일도 발생하지 않는다. Atomicity 는 로그로 Isolation 은 락으로 구현된다.
Multi-object transaction 이 필요하다. 예를들어 서로 참조하는 몇몇 오브젵그을 삽입할 때 참조가 유효해야 한다. 
또 비 정규화된 정보가 여러 오브젝트에 걸쳐 있을 때 싱크를 유지해야 한다. 또는 secondary index 가 존재해서 이것을 함께 변경해야할 때가 그렇다.
트랜잭션의 햄심 특징중 하나는 오류가 발생했을 때 안전하게 중단하고 재시도 할 수 있다는 점이다. 중지하고 재시도하는게 간단하고 효과적인 에러를 처리하는 방법이지만,
재시도에 수반하는 여러 문제들도 있다.

## Weak Isolation Levels (약한 격리 단계)
트랜잭션은 격리를 제공하는 것으로 동시성 문제를 트랜잭션 뒤로 숨기려 해왔다.
트랜잭션이 순차적으로 실행되는걸 보장하는 serializable isolation 이 있지만, 더 나은 성능을 위해서 일반적으로 더 이보다 저 낮은 격리 수준을 사용한다.
- Read Committed
  - 문제: 
    - dirty read: commit 될 수도 abort 될 수도 있는 값을 읽는 것으로 일관성이 깨질 수 있다.
    - dirty writes:
      - 같이 변경되어야 하는 여러 레코드를 업데이트 할 때, 서로 다른 트랜잭션의 쓰기가 뒤섞여 일관성이 깨질 수 있다.
      - 롤백이 다른 트랜잭션의 커밋된 쓰기를 지우거나, 제대로 되돌릴 수 없다.
  - 해결: 가장 기본적인 격리 수준으로 읽기를 할 때, 커밋된 데이터만 볼 수 있고(no dirty read), 쓰기를 할 때에도 커밋된 데이터에만 할 수 있다(no dirty writes). 트랜잭션A가 어떤 레코드를 쓰는 도중인 경우, 트랜잭션B는 그 레코드를 읽거나 쓸 수 없다.
  - 구현: 쓰기에 대해서 row 에 lock 을 설정하는 것으로 다른 트랜잭션의 쓰기를 블록한다. 읽기에 대해서는 블록하지 않고, 가장 마지막에 커밋된 값인 snapshot 을 돌려준다.
- Snapshot Isolation (Repeatable read):
  - 문제:
    - fuzzy read: 트랜잭션A 에서 레코드A를 읽는 도중, 트랜잭션B 에서 레코드A를 업데이트하고 커밋하면, 트랜잭션A에서 레코드A를 다시 읽으면 다른 값을 읽는다. 
    - read skew: 트랜잭션A 에서 레코드A,B를 읽는 도중, 트랜잭션B 에서 레코드A,B를 업데이트하고 커밋하면, 트랜잭션A 는 일관성이 깨진 레코드를 읽을 수 있다. 보통 다시 읽는걸로 해결이 되지만, backup 만들기나, 데이터 분석을 위한 대규모 쿼리에서는 문제가 될 수 있다.
  - 해결: 각 트랜잭션이 데이터베이스의 일관된 상태의 스냅샷을 읽는것을 보장한다.
  - 구현: multiversion concurrency control, dirty read 를 방지하기 위해서 사용했던 메카니즘의 일반화된 버전, 다양한 트랜잭션이 서로다른 시점의 데이터 베이스의 상태를 읽을 수 있도록 레코드에 여러개의 커밋된 버전을 유지한다. 
         트랜잭션에 증분하는 tx-id 를 부여하고 변경을 기록해두는 것으로 어떤 버전의 레코드를 읽을지 결정할 수 있다.  

약한 격리 단계에서는 dirty writes 말고도 다음과 같은 쓰기 충돌이 발생할 수 있다.
- Lost Update
  - 정의: read-modify-write cycles 에서 첫번째 트랜잭션의 업데이트가 나중의 업데이트에 의해서 덮어쓰여지는(사라지는) 문제. 예를 들어 증분 카운터 구현에서 문제가 발생할 수 있다.
  - 해결:
    - Atomic write operations: read-modify-write cycles 자체를 없에고 DB 의 원자 업데이트 오퍼레이션을 사용한다.
    - Explicit locking (명시적 라킹): 쿼리 결과로 돌아오는 모든 row 를 lock 하고, 다른 트랜잭션은 lock 이 풀릴 때까지 기다리도록 한다. 교착 상태는 DB에 의해 탐지되어서, 교착을 유발하는 트랜잭션을 중단 시키는 것으로 해결된다.
    - Automatically detecting lost updates: 트랜잭션 매니저가 lost update 의 발생을 감지하고 중단한다. 재시도는 어플리케이션 레이어에서 설정해야한다.
    - Conditional writes(compare-and-set): 값이 내가 직전에 읽은 값과 같을 때만 수정을 허용하는 것, 예를들어 JPA 에서 @Version 의 사용이다.
  - 보충: multi-leader 나 leader-less replication 의 경우, lock 이나 atomic operation 을 사용할 수 없다. 쓰기 작업을 허용해서 어러 버전의 값을 만둔 훈에 나중에 어플리케이션 레벨에서 충돌을 해결하는게 일반적인 접근 방법이다. 
         업데이트가 교환적(commutative)인 성격을 가지면 단순하게 병합하는것으로 충돌을 해결할 수 있다.
- Write Skew
  - 정의: 트랜잭션A,B가 같은 레코드를 읽고 판단한 뒤 서로 다른 레코드에 쓸 때, 트랙잭션A의 쓰기의 효과로, 트랜잭션B의 판단의 전제조건이 변경되어서 일관성이 깨지는 문제
  - 해결: 
    - DB 의 serializable isolation 사용
    - select for update 로 판단에 사용되는 레코드를 locking 하기 →　lock 할 레코드가 없으면 사용할 수 없다.
  - 보충:
    - materializing conflicts: lock 레코드가 없을 때, 인공적으로 lock 을 위한 오브젝트를 도입한다. 어플리케이션 레벨로 동시성 제어 메카니즘이 누수됨으로 serializable isolation 격리 수준을 사용하는게 더 적합하다.

Phantom: 한 트랜잭션의 쓰기가 또 다른 트랜잭션의 같은 조건의 검색 쿼리의 결과를 바꾸는 현상
 - 읽기의 경우: 같은 트랜잭션 내에서 두번째 읽기의 결과가 바뀐다. → snapshot isolation　으로 막을 수 있다. 
 - 쓰기의 경우: write skew 가 발생한다.

## Serializability (직렬 가능성)
serializable isolation 을 사용하면, DB 는 모든 트랙잰션이 순서대로 실행되는것 처럼 처리해서 모든 race condition 을 방지한다.

### Actual Serial Execution
동시성을 완전히 제거하고 한번에 하나씩 트랜잭션을 실행한다. 
실제로 한번에 하나의 트랜잭션을 실행하려 한다면 대화형 트랜잭션에서는 어플리케이션과 DB 사이를 오가는 시간 때문에 성능이 크게 저하된다.
따라서 옛날에는 snapshot isolation 을 serial execution 으로 구현할 때, 트랜잭션이 하나의 문장만을 가지던가, 미리 프로시져를 정의하고 그것을 실행하도록 했다.
하지만 프로시저를 작성하는 언어는 벤더에 종속적이고 완성도가 낮아서 관리하기 힘들었다.
최근에는 범용 프로그래밍 언어로 프로시저를 쓸수 있게 되었지만, 여전히 snapshot isolation 을 serial execution 으로 구현할 때는, 성능을 위해서 각각의 트랜잭션은 짧아야 하고,
조정을 위한 처리 때문에 sharding 등을 통한 확장성이 좋지 못하다. 

### Two-Phase Locking
지난 30년간 serializability 를 구현하기 위해 사용된 알고리즘이다.
트랜잭션이 락을 얻었다면 트랜잭션이 끝날 때 까지 락을 유지해야한다. 첫번째 페이즈에서는 락을 얻고, 두번째 페이즈에서는 트랜잭션에 종료될 때, 모든 락을 해제해야 함으로 2PL 이라고 불리고,
s-lock 과 e-lock 을 다음과 같이 사용한다.
- writer는 writer를 막는다: (기존 isolation level 에서도 하던 것)
- writer는 reader를 막는다: 한쪽이 먼저 쓰기작업으로 배타 락(exclusive) 만들었다. 동시에 다른 한쪽이 읽기를 하는경우 베타 락이 있음으로 트랜잭션이 커밋되기를 기다렸다가 읽는다.
- reader가 writer를 막는다: 둘이 동시에 조회를 한 경우 공유(shared) 락이 만들어진다. 어느 한쪽이 쓰기 작업을 시도하는 경우 배타 락(exclusive)이 필요한데, 공유락을 트랜잭션이 공유하고 있음으로 실패한다.  
2PL 을 사용할 때, 한 긴 작업이 락을 가저가 버리면 나머지 작업들은 그것이 완료될 때 까지 기다리므로, 지연시간은 불안정하며 높은 백분위에서 매우 느려질 수 있다.
데드락이 발생하면 트랜잭션이 중단된다. 재실행 때문에 추가적인 오버헤드가 발생할 수 있다.

레코드가 없으면 락을 걸수 없음으로 따라서 2PL 은 이를 predicate locks 을 사용해서 방지한다. 이는 조회 자체에 락을 걸고 나중에 추가, 변경되는 레코드가 그 조회 자체에 해당하는지 평가하는 것으로 블락한다.
- 트랜잭션 A가 select 쿼리를 사용해서 특정 조건의 레코드를 읽으려면 s-predicate-lock 을 얻어야한다. 만얀 다른 트랜잭션이 b-lock 을 가지고 있으면 기다린다.
- 트랜잭션 A가 어떤 레코드를 변경하려고 한다면, 다른 트랜잭션이 s-predicate-lock 이 있는지 확인해야 한다. 그렇다면 락이 풀릴때까지 기다려야 한다.

predicate locks 은 성능이 좋지 못함으로 실제로는 더큰 집합에 매치되도록 단순화한 index-range locks 을 사용한다.
조회 자체에 해당하는지 평가하지 않고, 인덱스상의 범위에 락을 설정해서 평가하는 것으로 평가 비용이 훨씬 작다. 하지만 적절한 인덱스가 설정되어있지 않다면(어떤 범위에 락을 걸지 결정할 수 없다면),
primary index 전체에 락을 걸게 된다.

### Serializable Snapshot Isolation
MVCC 를 사용해 pessimistic concurrency control 을 제공하는 알고리즘으로 snapshot isolation 격리 수준보다 성능저하가 미미하다.
snapshot isolation 에 기초해서 만들어졌다. snapshot isolation 에 더해서, 읽기와 쓰기들에 걸쳐서 serialization conflict 가 발생하는걸 중단시키는 알고리즘이 추가되었다.
write skew 는 쓰기 트랜잭션 안에서, 전제조건이 다른 트랜잭션에 의해서 변경되었을 때 발생하는데 버전 관리를 통해서 다른 트랜잭션이 변경한 전제를 감지하고 트랜잭션을 중단시킨다.
트랜잭션에 쓰기가 없다면, snapshot isolation 알고리즘에 기반해 동작합으로 블라킹은 없다.
존재하지 않는 레코드에 대한 locking 위해서 2PL 처럼 index-range locks 을 사용한다.

pessimistic concurrency control 은 문제 상황 자체가 발생하지 않도록 기다렸다가 실행하고, optimistic concurrency control 은 일단 실행하고 문제가 발생하면 그때 중단하고 다시 시도한다.
serial execution 은 pessimistic concurrency control 의 극단적 버전으로 이는 모든 트랜잭션이 서로 배타적 락을 가지고 있는것 과 같다.
Serializable Snapshot Isolation 은 optimistic concurrency control 이고, 문재가 발생하면 바로 트랜잭션을 중단한다.
경합 적을 때는 optimistic concurrency control 이 유리하고, 경합이 많을 때는 pessimistic concurrency control 이 유리하다.    
2PL 비교하면, 읽기에 블라킹이 적기 때문에 query latency 의 분산은 적고 예측가능하다.

## Distributed Transactions
복수의 노드가 관여하는 트랙잭션으로 Database-internal distributed transactions, Heterogeneous distributed transactions 으로 다시 구분할 수 있다.
분산 트랜잭션에서, 동시성의 제어는 단일노드 제어 트랜잭션과 비슷하게 2PL, SSI 을 사용하지만, 원자성을 보장하는 것은 다른다.

분산 트랜잭션에서는 Two-Phase Commit 알고리즘을 통해서 원자성을 보장한다.
prepare 와 commit 두단계를 가지고 coordinator 라는 컴퍼넌트가 모든 참여 노드에게 prepare 요청을 보내고 모든 응답이 yes 인 경우 commit 요청을 보내고 응답이 되돌아 왔을때만 커밋한다.
이 알고리즘이 동작할 수 있는 이유는 참여자가 더이상 결정을 되돌릴 수 없는 지점과, coordinator 가 더이상 결정을 되돌릴 수 없는 지점을 정하고, 결정이 관철될 수 있도록 약속했기 때문에다.
따라서 coordinator 에 장애가 있을 때, 참여노드는 doubt 라는 커밋할지 중지할지 결정할 수 없는 상태에 놓일 수 있고, coordinator 가 복구 되기를 계속 기다려야 한다.
따라서 coordinator 은 단일장애 지점으로 고가용성이 요구된다.

Heterogeneous distributed transactions 에서 2PC 를 구현하기 위한 표준으로 XA transaction 이 있다. 이는 프로토콜이라기 보다는 API 스팩에 가깝다.
XA 는 여러 데이터베이스와 호환하기 위해서, 공통 분모일 필요가 있고, SSI 같은 DB에 특정한 설정은 사용할 수 없다.
싱글노드 coordinator 는 전체 시스템의 단일 장애 지점이지만, XA 에서 coordinator 의 리플리케이션을 만들기 힘들다.

Database-Internal Distributed Transactions 에서도 복수의 shard 쓰는 트랜잭션에서 원자성을 보장하기 위해서 2PC 를 사용하지만, XA 와 같이 공통분모여야 한다는 제약이 없다.
coordinator 의 리플리케이션과 failover 가 가능하고 (- Consensus algorithms 이 coordinator 를 복제하는데 사용된다), 어플리케이션 코드를 거치지 않고 coordinator 가 shard 와 직접 통신한다. 
원자성 커밋 프로토콜과 동시성 제거 프로토콜을 결합하여 효율적으로 동작하도록 할수 있다.

exactly-once 시멘틱은, 메세지 확인과 데이터베이스 쓰기 작업을 원자적인 단일 트랜잭션으로 묶어서, 메세지 전달이나 데이터베이스 트랜잭션중 하나라도 실패하면, 두 작업이 중단됨으로 메세지 브로커는 안전하게 재시도 할수 있게 함으로써 메세지가 정확히 한번만 처리되도록 보장하도록 하는 것이다.
heterogeneous distributed transactions 으로 이것을 구현하는 방법도 있지만, message broker 를 트랜잭션으로 묶을 필요 없이, 메세지 상태를 DB를 통해 관리하고, 같은 트랜잭션 내에서 그것들을 다루는 걸로 "Exactly-Once" 시멘틱을 구현할 수도 있다.