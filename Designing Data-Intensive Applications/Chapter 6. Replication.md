# Chapter 6. Replication
- replication 은 네트워크로 연결된 복수의 머신에 같은 데이터의 사본을 보존하는 것이다.
- replication 의 어려움은 복사된 데이터의 변경을 다루는 것에 있고 이것을 해결하기 위한 세가지 유형의 알고리즘에 대해 배운다.
  - single-leader
  - multi-leader
  - leaderless replication
- replica 와 backup 의 차이
  - 목적이 다르다. replica 는 실제 운영에 사용되는 사본으로 신뢰성을 높이거나, 레이턴시를 개선하기 위한 목적이 있다. 따라서 어떤 노드에 쓰기가 이루어지면 다른 노드에도 바로 반영한다.
  - 백업은 특정 시간으로 거슬러 올라갈 수 있도록 스냅샷을 저장한다.

# 목차
- Single-Leader Replication
  - Synchronous Versus Asynchronous
  - Setting Up New Followers
  - Handling Node Outages
  - Implementation of Replication Logs
  - Problems with Replication Log
  - Solutions for Replication Log
- Multi-Leader Replication
  - Geographical Distributed Operation
  - Sync Engines and Local-First Software
  - Dealing with Conflicting Writes
- Leaderless Replication
  - Writing to the Database When a Node is down
  - Single-Leader Versus Leaderless Replication Performance
  - Multi-Region Operation
  - Detecting Concurrent Writes
- Summary

# Single-Leader Replication
- replica: 데이터베이스의 사본을 보존하고 있는 node.
- single leader replication solution
  - 하나의 leader node 가 쓰기 요청을 담당한다.
  - follower node 들이 replication log 를 넘겨받아 자신의 db 를 업데이트 한다.
  - 읽기는 모든 node 에서 수행될 수 있다.

## Synchronous Versus Asynchronous Replication
- semisynchronous: 적어도 하나의 follower 는 동기로 쓰기 작업을 하는것.
  - 동기 follower 가 정지되면 leader 의 write 가 완료될 수 없다는 단점을 가진다.
- 몇몇 시스템에서는 반수 이상을 동기로 두고, 나머지는 비동기로 두기도 한다.
- 모든 followers 를 비동기로 두기도 한다. 
  - 단점은 leader 가 실패해서 복구 불가능한 경우, 아직 follower 에 복제되지 않은 쓰기는 모두 유실된다.
  - 장점은 followers 를 기다리지 않고 계속해서 쓰기 처리를 할 수 있다. 

## Setting Up New Followers
- 데이터가 계속 흐르고 있기 때문에 단순히 스냅샷을 새로운 follower 설정하는 것으로 부족하다.
- downtime 없이 새로운 follower 를 설정하는 것은 다음과 같은 프로세스로 완수 할 수 있다.
  - leader 의 스냅샷을 찍는다.
  - 새로운 follower 에 스냅샷을 설정한다.
  - follower 가 leader 의 스냅샷 시점 이후의 변경에 대해 요청한다.
    - snapshot에 대응하는 로그 위치 이후의 replication log를 (계속) 받는다
    - replication log: 리더에서 일어난 쓰기 순서를 기록한 스트림
  - follower 가 데이터 변경을 모두 반영하면  caught up(catch up) 되었다고 말할 수 있다.
- replication log 를 db 의 스냅샷과 함께 object storage 에 저정하는 방법도 있다.
- 많은 데이터 베이스가 Amazon S3, Google Cloud Storage 와 같은 object storage 에 데이터를 저장하고 실시간 쿼리에 필요한 데이터를 제공한다.
  - object storage 를 DB 의 storage 계층으로 쓴다.
  - 여러 트레이드 오프들이 있다. 싸고, 여러 지역에 설정하고 통합하기 쉽다는 장점이 있지만, 네트워크를 통하기 때문에 기본적으로 느리다.
  - zero-disk architecture (ZDA): 모든 데이터를 object storage 에 저장하고, disks 와 memory 는 캐싱에만 사용하는 아키텍쳐
  - OLAP 에서, storage 는 S3 으로 쓰고, 쿼리 엔진으로 ClickHouse, DuckDB 같은걸 쓸 수 있다.
    - Mysql 의 InnoDB는 행 지향 + B-tree 이기 때문에 사용할 수 없다.

## Handling Node Outages
### Follower failure: Catch-up recovery
- catch-up recovery: follower 에 문제가 생기거나, 재시작하거나 네트워크의 문제가 생기는 경우, leader 에게 그동안의 변경 사항을 요청하고 그것을 반영하는 것으로 복구하는 것.
- 쓰기 산출량이 큰 시스템이거나, follower 가 오랜시간 오프라인인 경우 성능 문제가 발생할 수 있다.
- leader 는 log 를 정기적으로 삭제하는데, follower 가 오랜시간 동안 오프라인인 경우, log 의 보유기간을 늘리거나, follower 에 상관없이 삭제하거나 해야한다.
### Leader failure: Failover
- failover : leader 에 장애가 발생해서 새로운 leader 를 선출 하는 것.
- failover 단계
  - leader 에 장에가 발생한 것을 알아낸다. 보통 타임아웃을 사용함.
  - 새로운 leader 를 뽑는다.
    - election process 를 거쳐서 선출된다. 보통 최신 데이터를 가지고 있는 follower 가 된다.
  - 새로운 leader 를 사용하도록 시스템을 재 구성한다.
- failover 에는 문제가 발생할 수 있는 요소가 많다.
  - 비동기 follower 만 있는 경우, 데이터 유실이 발생할 수 있다.
  - 데이터베이스 외부의 다른 저장 시스템과 데이터베이스 내용을 동기화해야 하는 경우, 쓰기 작업을 무시하는 것은 특히 위험하다. 외부 시스템이 새로운 리더는 가지고 있지 않은 최신 데이터를 가지고 있는경우 문제가 발생할 수 있다.
  - 두 노드가 자신이 leader 라고 믿는 경우 데이터가 부패할 수 있다.
  - leader 에 장애가 발생했다고 결정하기 위한 기준 타임아웃을 정하는건 까다롭다.
    - 너무 짧으면 불필요한 failover 가 수행된다.
    - 길면 복구에 많은 시간이 들고 유실되는 데이터도 많을 수 있다.
- 유실되는 데이터를 최소화 하기 위해서 가장 최신의 데이터를 가지고 있는 follower 를 leader 로 선출하는 것이 가장 중요하다.

## Implementation of Replication Logs

### Statement-based replication
- statement-based replication: leader 는 자신이 실행하는 모든 요청(statement)을 기록하고 statement-log 를 follower 에게 보낸다.
- 비결정적 문장, 순서 보장이 요구되지만, 비결정적 문장은 leader 가 실행한 결과 값으로 대체해서 전송하고, 결정적 문장은 순서를 보장하는 것으로 우회한다.(state machine replication - 이벤트 소싱도 같은 접근 방식이다.)
- mysql 5.1 이전 버전에서는 이러한 방식이 사용되었지만 요즘은 다른 방식이 선호된다.

### Write-ahead log shipping
- B-tree storage engine 을 견고하게 만드는데 사용되는 write ahead log 는, replica 를 만드는데 사용될 수 있다. leader 는 WAL 을 디스크에 저장하는 것 뿐만 아니라, followers 에게 전송한다.
- WAL 의 원래 목적은 크래시 복구이다. DB 버전에 종속되어 버려서, follower 의 버전을 먼저 업그레이드한 후 리더로 변경하는 failover 를 수행할 수 없다는게 단점이다.
  - 예) $PGDATA/base/16384/24576 파일의 처음부터 137 × 8192 바이트 지점

### Logical(row-based) log replication
- 데이터 베이스의 버전에 종속되는 WAL 을 전송하지 않고 record 의 변경 로그로 복제하는 방법.
- follower 만 먼저 버전업을 할 수 있다. → 후방호환성을 지원한다.

## Problems with Replication Lag
- 장애를 견딜 수 있다는건 복제를 하는 하나의 이유일 뿐이고, 확장성, 지연시간 감소등의 이유도 있다. 웹서비스에는 읽기 요청이 훨씬 많기 때문에 follower 를 추가하는 것 많으로 처리량을 늘릴 수 있다.
- eventual consistency: follower 가 catch up 을 해서 결국 일관성이 달성되는 것.
- 비동기로 follower 가 leader 를 catchup 할 때 lag 가 길어지면 여러가지 문제가 발생할 수 있다.

### Reading your own writes
- read-after-write consistency(read-your-writes consistency): 유저가 어떤 데이터를 업데이트를 한 후에 다시 그 유저에 한해서, 그 데이터를 요청했을 때 업데이트가 반영되어 있는 것을 보장하는 것.
- 구현방법
  - 유저가 변경했을 수도 있는 것을 읽을 때, 리더, 동기 팔로워, 업데이트된 팔로워에서만 읽는다. 해당 항목을 조회하지 않고도 수정되었을 가능성이 있는지 파악할 수 있어야 한다. 
    예를 들어 유저가 자신의 프로파일을 변경하는 경우, 유저 자신만 프로파일을 변경할 수 있음으로 유저자신이 프로파일을 요청할 땐 리더에서 읽고, 다른 유저는 팔로워에서 읽는다.
  - 리더에서 읽을지 말지 정하는 다른 기준을 세운다. 
    예를 들어 마지막으로 변경된 후에 1분 이내에 읽기 요청이 있는 경우엔 무조건 리더에서 읽는다. 복제 지연을 측정해서 팔로워가 1분 이상 뒤쳐지지 않도록 방지할 수 있다.
  - 클라이언트가 가장 최근의 timestamp 를 기억하고, 시스템이 사용자의 읽기 요청을 처리할 때, 해당 timestamp 의 시점까지 업데이트가 완료된 replica 가 처리하도록 한다.
- replica 가 여러 리전에 분산되어 있으면, 문제는 훨씬 복잡해진다. 쓰기는 라우터를통해 leader 에 도달해야 한다.
- cross-device read-after-write consistency: 다른 디바이스에서도 read-after-write consistency 를 제공하는 것.

### Monotonic reads
- Monotonic reads: 데이터가 업데이트 된 팔로워에 쿼리를 했다가, 같은 데이터를 아직 업데이트 되지 않은 팔로워에 쿼리를 하는 경우, 데이터가 사라진것 처럼 보이는데 이러한 현상이 발생하지 않도록 보장하는 것.
  가장 최근에 받았던 데이터보다 더 오래된 데이터를 받지 않도록 하는 것.
- 구현방법
  - 같은 replica 를 사용하도록 한다.

### Consistent prefix reads
- Consistent prefix reads: 팔로워의 lag 의 차이로, 인과관계가 성립하지 않는 문제가 발생하지 않도록 보장하는 것.
  일련의 쓰기 작업이 특정 순서로 발생하면, 읽는 경우에도 그것이 동일한 순서로 나타나도록 하는 것. 

## Solutions for Replication Lag
- 어플리케이션 수준에서 Replication Lag 를 해결하는 방법도 있지만, 이는 복잡하고 잘못되기 쉽다.
- replica 에 대해서 일관성을 보장하는 데이터베이스를 사용하는게 하나의 선택이 될 수 있다.
- 이러한 데이터베이스가 사용가능 하더라도, 네트워크 중다시의 복원력, 더 적은 오버헤드등을 이유로, 일관성 보장이 낮은 복제 방법을 사용할 수도 있다.

----

# Multi-Leader Replication
- multi-leader configuration: 각각의 리더는 동시에 서로의 팔로워로 행동한다.
- multi-leader 에서도 동기식과 비동기식이 있다. 동기식은 동기 팔로워와 같은 문제들이 발생한다. (서로의 지연이나 실패에 종속된다.)

## Geographically Distributed Operation
- multi-region 배포에서의 single-leader 와 multi-leader configuration 의 비교
  - Performance
    - single-leader 면 leader 리전까지 리퀘스트가 도달해야 함으로 지연이 더 크다.
  - Tolerance of regional outages
    - single-leader 면 다른 리전의 follower 를 승격하는 등의 failover 가 필요하다. 
    - multi-leader 에서는 각 리전은 독립적으로 동작하고, 중단에서 돌아왔을때 캐치업하게 된다.
  - Tolerance of network problems
    - single-leader 에서 쓰기를 처리하기 위에서 리전간 통신은 안정성이 더 떨어진다.
    - multi-leader 에서는 각 리전이 독립적이고 네트워크 장애 또한 독립적이다.
  - Consistency
    - single-leader 에서는 강력한 일관성을 달성 할 수 있다.
    - multi-leader 에서는 대부분의 어플리케이션에서 유용한 정도의 일관성을 달성 할 수 있다.
#### Multi-leader replication topologies
- 다양한 위상배치가 사용될 수 있다. 복제루프가 무한히 반복되지 않도록 하는 장치가 있다.
#### Problems with different topologies
- circular topology, star topology 의 경우 하나의 노드가 실패하면, 다른 노드의 메세지 복제의 흐름도 멈춘다.
- all to all topology 의 경우에, 각각의 노드에 반영되는 속도가 다를 수 있음으로, 나중의 메세지가 이전의 메세지를 추월하는 문제(overtake)가 발생할 수 있다. 
  - 각 노드의 시계가 충분히 동기화 되었을지 알 수 없기 때문에 timestamp 로는 해결할 수 없고, version vectors 같은 테크닉을 사용해서 해겨할 수 있다. 

## Sync Engines and Local-First Software
- 인터넷이 없이도 동작해야 하는 어플리케이션에도 다중 리더 복제가 적합하다. 이때 로컬 환경의 replica 가 하나의 리더가 되고, 여러 디바이스를 region 으로 볼 수 있다.

## Dealing with Conflicting Writes
- 쓰기 충돌: 특정 레코드 대해서 각각의 리더에서 동시에 쓰기 작업이 발생했을 때, 리더끼리 동기화를 시도하려고 할때, 동기화 하려고 하는 서로의 레코드 자신이 이전에 알고 있던 레코드 덮어쓰기가 되어있는 상태.
### Conflict avoidance (충돌 자체를 피하기)
- 특정 레코드에 대해서 leader 를 정해놓는다. 예를들어 user1 이 region1 에 가까운 지역에서 엑세스 한다고 하면, user1 이 소유하고 있는 레코드 들의 리더를 region1 로 설정할 수 있다.
- 생성에 한해서, 각각의 리더가 한쪽은 짝수의 id 를 만들고, 다른 한쪽은 홀수의 id 만 만들도록 해서 id 의 충돌을 피할 수 있다.
### Last write wins (discarding concurrent writes)
- Automatic conflict resolution 의 한가지 방법
### Manual conflict resolution
- 각각의 리더가 변경을 쌓아놓고, 다음 쿼리에 각각의 리더의 변경을 포함한 레코드를 돌려주고, 유저가 원하는 방식으로 충돌을 해결한다.
  - 어플리케이션의 업데이트로 타입이 바뀐 데이터들의 호환성문제
  - 사람의 작업 부담 
  - 아마존 쇼핑카트 처럼 변경된 결과를 단순히 머지하는 경우, 예상치 못한 결과가 된다.
### Automatic conflict resolution
- 자동 충돌 해결은 모든 replica 가 같은 상태로 수렴하도록 보장한다. 이러한 수렴 보장과 결과적 일관성을 합친 것을 strong eventual consistency 라고 한다. 
  - 데이터 별로 업데이트에 대해 의도된 효과를 최대한 보존하도록, 병합 알고리즘이 개발되어 왔다.
    - text 의 경우, 삽입과 삭제를 모두 기록해서 병합한다.
    - list 의 경우, 마찬가지로 삽입과 삭제를 모두 기록한다음, 그 기록을 통해서 병합 할 수 있다. (아마존 쇼핑 카트와 같은 현상을 방지 할 수 있다.)
    - integer 의 경우, 마찬가지로 모든 증감을 기록한 다음, 그 기록을 병합하면, 올바른 결과를 얻을 수 있다.
    - key-value 의 경우, key 중보에 대해서만 다른 충돌 해결 알고리즘을 사용할 수 있다.
### Conflict-free replicated datatypes and operational transformation
- operational transformation (OT)
  - 글자와 인덱스를 매핑해서 변경을 추적함
  - google doc 에서 사용됨.
- conflict-free replicated datatypes (CRDTs)
  - 글자마다 유니크한 id 를 부여해서 변경을 추적함
### Types of conflict
- 두개의 쓰기 작업이 하나의 레코드를 동시에 수정하는 것은 명백한 충돌이지만, 하나의 레코드만 허용된다는 응용레벨의 제약사항이 있을 때, 동시에 새로운 레코드를 작성하는 것은 조금 더 미묘하다. 

----

# Leaderless Replication
- 리더가 없고 client 가 모든 replica 에 쓰기 요청을 보내는 구성
## Writing to the Database When a Node Is Down
- client 모든 replica 에 쓰기 요청을 보낸다. 하나의 노드가 outage 여도, 2개의 replica 에 쓰기가 성공하면, outage 상태의 노드에 대한 요청이 실패한건 무시한다.
- client 는 마찬가지로 읽기 요청을 할 때, 모든 replica 에 요청을 하고 돌아온 결과 최신 버전의 값을 취한다. outage 상태였던 노드가 돌아와 있다면, outdated 된 값을 돌려주는데, client 는 이를 알 수 있고, 최신 값으로 쓰기 요청을 한다. (Read repair) 
### Catching up on missed writes
- Read repair: 읽기 요청을한 클리아언트가 최신값으로 쓰기 요청을 한다.
- Hinted handoff: 쓰기 요청이 들어왔을 때, 하나의 replica 죽어있으면, 살아 있는 replica 가 힌트를 저장해두었다가, 죽어있던 replica 가 복구되면 handoff 프로세스를 통해 최신상태로 업데이트하고 힌트를 삭제한다.
- Anti-entropy: 백그라운드 프로세스가 데이터 차이를 확인하고, 누락된 데이터가 있는 경우 이를 동기화 한다. 리더 기반 복제 로그와 달리 특정 순서로 복제하지 않는다.
### Using quorums for reading and writing
- leaderless replication 에서 3개중의 2개의 replica 가 쓰기 요청을 처리하면 정상적인것으로 봤다. 그렇다면 정상 처리로 보기위한 쓰기와 읽기에서의 replica 수는 뭘까?
- w + r > n 이면, 읽기에서 항상 최신의 데이터를 읽을 수(알아낼 수) 있다고 예상할 수 있다 - 적어도 하나의 노드는 stale 하지 않은 값을 돌려준다. 이를 정족수 quorum 이라고 한다.
  - w + r > n 은 n / 2 (rounded down) 의 노드에 장애를 용인한다.  
- 어떤 노드에 장애가 발생하더라도 정족수가 유지되면 시스템은 정상 동작한다.
### Understanding the limitations of quorum consistency
- edge case 들이 많다.
### Monitoring staleness
- leader-based replication 에서는 리더를 통해서 쓰기가 이루어지고 follower 은 replication log 를 통해서 같은 순서로 catch up 이 이루어 진다. 
  운영 관점에서 얼마나 lag 발생했는지 쉽게 확인할 수 있다.
- leaderless replication 에서는 순서가 보장되지 않기때문에 이러한 것들을 측정하기 어렵다.

## Single-Leader Versus Leaderless Replication Performance
- Single-Leader 아키텍쳐에서 up-to-date 한 응답을 얻는데에는 다음과 같은 성능문제가 발생한다. 
  - leader 의 처리량에 종속된다.
  - 리더가 실패하면 결함이 감지되고 failover 가 이루어질 때까지 기다려야 한다.
  - leader 의 성능문제에 종속된다. 
- Leaderless 시스템의 회복력은 일반적인 캐이스와, 실패 케이스를 구분하지 않는 다는 것에 있다. 
  - 어떤 노드가 완전히 실패하지 않은 상태인 gray failure 를 다루는데 좋다.
  - leader-based 시스템 에서는 failover 를 수행해야 하는지 판단 해야하지만, leaderless 시스템에서는 이러한 질문 자체가 떠오르지 않는다.
- Leaderless 아키첵쳐에서 up-to-date 한 응답을 얻는데에는 다음과 같은 성능문제가 발생한다.
  - failover 를 수행하지 않는다고 해도, 각 replica 는 handoff 프로세스를 위한 힌트를 저장해야한다.
  - replica 의 수가 늘어나면 정족수도 늘어나고 여러 replica 에서 response 를 받아야 함으로 지연이 커질 가능성이 높아진다.
  - 큰 규모의 네트워크 장애로 정족수를 유지하지 못할 수 있다. 정족수를 유지하지 못한 상황에서도 동작할 수 있게 하는 설정이 있지만, 최신 값을 읽는다는 보장은 사라진다.
- Quorum reads and writes provide a compromise: good fault(결함) tolerance and a high likelihood of reading up-to-date data.  

## Multi-Region Operation
- Cassandra 에서는, 다중 리전 쓰기를 수행하는 클리어언트가 자신의 리전의 coordinator node 라는 곳에 쓰기를 하는 것으로 시작한다.
  coordinator node 는 다른 리전의 모든 replica 들에 쓰기작업을 전송한다. 이렇게 하는 것으로 리전간에 여러번 요청이 오가는것을 피할 수 있다.
  - 리전에 걸친 일관성 수준을 설정 할수 있다. 예를들어 전체 리전에 걸친 quorum 을 사용하거나 리전별 quorum 을 사용할 수 있다.

## Detecting Concurrent Writes
- 동시성으로 인한 충돌을 해결하기 위해서는, 동시성을 발견하기 위한 알고리즘이 필요하다.
- LWW 과 같은 알고리즘은 조용히 데이터를 유실시킨다.
### The happens-before relation and concurrency (선행 발생 관계와 동시성)
-　두 오퍼레이션이 동시적인지 어떻게 결정할까? 인과성을 통해서 판단한다.
 - 오퍼레이션 B 가 인과적으로 오퍼레이션 A 에 의존하는 경우 
 - 같은 키에 대해서 서로 다른 클라이언트가 오퍼레이션이 수행되는 지 모르는 경우, 인과성이 없다.
- A도 B보다 먼저가 아니고, B도 A보다 먼저가 아니면 → concurrent
  - 동시성은 실체가 있는 게 아니라 관계의 부재 
  - 물리적 시간과 무관하다.
### Capturing the happens-before relationship (선행 발생 관계를 캡쳐하기)
- 버전 메카니즘: 발생한 동시성을 탐지하고 보존 → 클라이언트가 머지하는것으로 발생한 동시성을 해소
  - 서버는 키마다 버전 번호를 유지, 쓰기마다 증가
  - 클라이언트는 쓰기 전에 읽어야 함 → 그때 받은 버전을 기억
  - 쓰기 시 그 버전을 함께 보냄
  - 서버는 그 버전 이하의 값은 덮어쓰고 (= 클라가 본 값이니 안전),
  - 그보다 높은 버전의 값은 sibling () 으로 남김(= 클라가 못 본 값) 
### Version vectors
- 하나의 replica 를 통해서 보인 버전을 통한 충돌 해결 알고리즘은 여러 replica 에서도 마찬가지로 적용될 수 있다.
  - 각 replica 의 버전 번호의 집합을 version vector (a.k.a vector clock)라고 한다.

# Summary
replica 는 database 의 사본을 보존하고 있는 node 이다. 가용성, 성능, 확장성 향상을 목적으로 replica 를 두는 시스템을 구성할 수 있다.
Single-Leader Replication 은 쓰기 처리를 담당하는 하나의 leader와 follower 들을 구성하는 아키텍쳐이다.
leader 의 최신 데이터를 follower 로 복제하는 방법은 동기 방식과 비동기 방식이 있는데, 동기 방식은 상대적으로 낮은 가용성과 성능을 가지기 때문에 비동기 방식을 사용한다.

leader 가 실패했을 때 복구하는 것을 failover 라고 한다. 새로운 leader 선출과 이에 수반하는 오버헤드가 발생한다.
follower 는 leader 에게 replication log 를 전달 받아서 데이터를 동기화 한다.
replication log 를 전달하는 방식은, statement-based, write-ahead log 전달, row-based 방식이 있다.
읽기를 처리하는 follower 가 복수개 있고 동기화되는 시점이, 즉 replication lag 때문에 때문에 여러 문제가 발생할 수 있다.
어플리케이션은 다음과 같이 동작해야 한다.
- Read-after-write consistency 를 제공해야한다.
- Monotonic reads 를 해야한다.
- Consistent prefix reads 를 해야한다.

Multi-Leader Replication 은 쓰기 처리를 담방하는 복수의 leader 가 서로의 follower 가 되도록 구성하는 아키텍쳐이다.
지역적으로 분산된 운영을 하는 경우, Single Leader 방식은 위치가 먼 지역에서의 요청은 느려질 수 밖에 없는데, 각 리전별로 leader를 설정하면 지연지간을 감소시킬 수 있다.
로컬 우선 소프트웨어(Local-First Software) 의 경우도 로컬환경의 replica 를 하나의 리전으로 볼 수 있다. 
복수의 leader 가 존재함으로 발생하는 같은 레코드에 대해 쓰기처리를 하고 동기화하려고 할때 서로가 알고 있던 값이 다른 경우 즉, 쓰기 출돌 문제를 해결해야 한다.
LWW 같은 단순한 알고리즘, 특정 데이터 타입에 대해서 병합할 수 있는 알고리즘이 사용된다.
leader 간 복제를 처리하는 순서와 방향, 즉 topology 를 고려해야 한다.

Leader-Less Replication 은 리더가 없이 모든 replica 에 쓰기와 읽기 요청을 하는 아키텍쳐이다.
일부 노드의 실패를 용인 할수 있는 아키텍처로 읽기 쓰기에서 정족수(quorum), w + r > n 를 만족하면 쓰기를 할 수 있고 읽기에서도 항상 최신 데이터를 돌려 받을 수 있는 가능성이 높다.
일부 노드에서 쓰기가 실패했을 때 이를 복구하는 방법으로, 클라이언트가 replica 를 최신데이터로 동기화 하는방법, hinted handoff, anti-entropy 방법이 있다.
일부 노드의 실패가 용인 되기 때문에 Single-Leader 에서의 failover 난 catchup 같은 개념 자체가 존재하지 않지만,
클라이언트가 모든 replica 와 통신을 시도 해야 한다는 점에서 지연시간이 길어질 수 있다.
지역적으로 분산된 운영을 하는 경우, 각각에 리전을 클러스터로 나누고, 특정한 replica 를 통해서만 리전간 동기 처리를 하는 방법이 사용될 수 있다.

LWW 처럼 단순한 처리로는 값이 유실되고 만다. Leader-Less Replication 에서 동시성으로 인한 충돌을 해결하기 위해서는 동시성을 발견하기 위한 알고리즘이 필요하다.
어떤 작업이 동시성을 가지고 있다는 것은 두 작업관의 인과관계가 없다는 것을 의미한다. 이는 물리적 시간과는 무관하다.
동시성을 탐지하면 이것을 보존해 두고, 클라이언트가 머지하는 것으로 동시성을 해소하는 것을 버전 메카니즘 이라고 한다.

쓰기 처리 및 복제본 간 데이터 불일치 해소 방법
- 단일 리더(비동기로 follower 와 동기화되는 경우): 쓰기를 한 리더에서 처리
  - 장: 쓰기 충돌이 없고 단순함. 
  - 단: 리더 장애 시 failover, 원거리 쓰기 지연, 비동기 복제에 따른 오래된 읽기·데이터 유실 가능성을 고려해야 함.
- 다중 리더(비동기로 follower 와 동기화되는 경우): 각 리더가 독립적으로 쓰기를 처리하고 이를 사후에 전파·병합.
  - 장: 지역별 저지연과 오프라인 내성 확보.
  - 단: 리더간 쓰기 충돌 해결 필요.
- 리더리스: 고정 리더 없이 여러 복제본이 쓰기를 받아들임. Read repair 과 같은 방식으로 불일치 해소
  - 장: 쿼럼으로 일부 노드장애 용인, 최신 데이터를 읽을 가능성이 구조적으로 어느정도 보장됨.
  - 단: 클라이언트 부담 증가. (버전 관리 및 클라이언트를 통한 머지로 동시성 문제 해결)
