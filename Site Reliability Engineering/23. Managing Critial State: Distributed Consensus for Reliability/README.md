# Managing Critical State: Distributed Consensus for Reliability (중요한 상태 관리: 신뢰성을 위한 분산 합의)

Reliability 를 위한 전략으로 여러 장소에서 시스템을 동작 시킬 수 있고, 위치 시키는 것은 간단하더라고 시스템 상태에 대한 일관적인 뷰를 제공하는 것은 어렵다.
시스템 상태의 일관적인 뷰를 제공하는데 분산 합의가 효과적일 수 있다는 것을 발견했다.
리더 선출이나 중요한 공유된 상태, 분산 잠금을 볼때, 공식적으로 검증되고 철저하게 테스트된 분산합의 시스템을 사용하는 걸 추천한다.  
_※ 분산 합의: 분산 시스템에서 여러 노드가 일관된 결정을 내리는 과정_


## CAP Theorem (CAP 정리)
- Consistent views of the data at each node
- Availability of the data at each node
- Tolerance to network partitions
CAP 정리에 따르면 C,A,P 모두를 동시에 충족시킬 수 없다. 이중 둘만 충족시킬수 있고 시스템 요구사항에 따라 어떤 특성을 충족시킬지 결정해야 한다.
분산 데이터 베이스에서 사용되는 시멘틱은 ACID 가 아니라 BASE 이고, 둘은 서로 다른 강점을 가진다. 


## Motivating the Use of Consensus: Distributed Systems Coordination Failure (분산합의 사용의 동기부여: 분산 시스템 조정 실패)
분산 시스템에서 장애가 발생했을 때 시스템의 동작을 추론하는건 어렵다. 특히 네트워크 단절 문제가 특히 더 어렵다.
아래의 예는 실제 분산시스템에서 발생하는 문제들이고 분산 합의를 사용해서 이러한 문제들을 어떻게 방지 할 수 있는지에 대해서 논한다.

### Case Study 1: The Split-Brain Problem
리더와 팔로워로 서버를 구성한 경우, 서로의 헬스를 체크를 하고 헬스체크에 반응이 없는 경우 그 노드를 죽이고(STONITH 송신) 마스터쉽을 가진다.
만약 네트워크가 제대로 동작하지(느려지거나 패킷이 누락된 경우) 않는 경우 하나의 리소스에 대해서 두 노드가 모두 활성상태이거나,
STONITH 를 주고 받아서 둘다 다운된 상태가 될 수 있다.
리더 선출 문제를 푸는데 단순히 timeout 을 사용하는 것이 문제다.

### Case Study 2: Failover Requires Human Intervention (사람의 개입이 필요한 복구)
외부 시스템에 의해서 primary를 결정한다. 헬스체크에 실패할 경우 외부 시스템은 세턴더리 프라이머리로 승격시킨다.
이때 만약 프라이머리가 세턴더리의 헬스를 확인 할 수 없는 경우, 자신을 사용불능 상태로 만들고 cas1 의 문제가 발생하지 않도록 문제를 격상시켜 사람을 호출한다.
이는 운영부하를 증가시킨다. 이런 문제가 발생하는 경우 이미 인프라에 다른 문제도 발생해서 엔지니어가 과부하에 걸려있을 가능성이 높다.
이때 사람이 직접 마스터를 선출하는건 좋은 방법이 아니다.

### Case Study 3: Faulty Group-Membership Algorithms
각 노드가 클러스터에서 리더를 선출할 때, 네크워크 단절 문제가 발생하면 분리된 각 측에서 마스터 노드를 선출 할 수 있다.

위와 같은 분산 시스템의 문제는 증명된 분산합의 알고리즘을 사용해서 풀어야 한다.
위와 같은 종류의 문제를 풀기위한 임시수단은 현실적으로 항상 신뢰성 문제를 가진다.

## How Distributed Consensus Works
합의 문제에는 여려가지 변형이 있다.
- synchronous or asynchronous distributed consensus
- crash-fail or crash-recover (문제가 발생한 노드가 되돌아오는지 아닌지)
_※ crash_ : 장애, 사고
- Byzantine or non-Byzantine (비잔틴 또는 비-비잔틴: 비잔틴 장애는 악의적인 활동으로 프로세스가 잘못된 메세지를 전달할 때 발생)
최초의 해결책은 Paxos 프로토콜이다.

### Paxos Overview: An Example Protocol
분산 시스템에서 합의를 이루기 위한 프로토콜로, 제안자는 순서번호를 수락자에게 보내고
수락자는 자신의 노드 집합내에서 더 높은 순서번호를 가진 노드가 없을 때만 이를 수락을 한다.
노드가 제안을 승인하면, 제안자는 그 제안을 commit 한다.
다수의 승인을 받아야만 커밋이 가능하다는 요구사항은 동일한 제안에 대해서 두가지 다른 값이 커밋되는 것을 방지한다.
최소한 하나의 노드가 두개의 다수 집합에 겹치기 때문에 이것이 가능하다.


## System Architecture Patterns for Distributed Consensus (분산합의를 위한 시스템 아키텍쳐 패턴)
Distributed consensus algorithms 는 낮은 레벨이고 프리머티프이다.
많은 시스템은 Zookeeper 같은 서비스를 통해 분산 합의 알고리즘을 사용한다.
Google 의 Chubby 서비스 또한 라이브러리가 아닌 서비스로 제공되는데 이를 통해 어플리케이션에서
Distributed consensus 의 관심사를 덜어낼 수 있다.


### Reliable Replicated State Machines
Replicated State Machines 은 오퍼레이션의 집합을 같은 순서로 실행하는 시스템을 말한다.
결정론적인 오퍼레이션을 순서대로 실행한다면, 복제된 state machine은 최종적으로 같은 상태에 다다른다.
RSM은 합의 알고리즘의 논리적 계층 위에 구현된 시스템 이다.
여기서 합의 알고리즘은 작업 순서에 대한 합의를 도출한다. RSM 은 이에 따라 작업을 실행한다.

### Reliable Replicated Datastores and Configuration Stores
Reliable Replicated Datastores 는 RSM 의 응용이다.
복제본 간에 일관성을 유지하기 위해 합의 알고리즘을 사용하고, 분산 합의된 데이터스토어는 읽기에 대해서 다양한 일관성의 의미를 제공할 수 있다.
(느스한 일관성 수준, 엄격한 일관성 수준..)
분산합의를 사용하지 않는 시스템은 timestamp에 의존하기도 하는데 여러 머신에서 clock이 동기화 된다고 보장할 수 없기 때문에 문제가 될 수 있다.

### Highly Available Processing Using Leader Election (리더 선출을 사용한 고 가용성 처리)
분산 시스템에서의 리더 선출은 분산 합의와 동일한 문제이다.
서비스 리더의 작업이 하나의 프로세스로 수행되어야 할 때, 리더 선출을 통해 특정 시점에서 하나의 리더만 작업을 수행하도록 보장 함으로써 고 가용성을 달성할 수 있다.
(리스 시스템 사용)
※ _mutual exclusion: 상호 배타적인_
※ _coarse level: 대략적인 레벨_

### Distributed Coordination and Locking Services
베리어는 특정 조건이 충족될 때 까지 프로세스 그룹이 진행되지 않도록 하는 분산 시스템의 조정 도구이다.
배리어를 사용하면 분산 계산을 효과적으로 논리적 단계로 쪼갤 수 있다. 
MapReduce의 경우, 모든 프로세스가 Map된 이후에 Reduce 될 수 있도록 베리어를 설치할 수 있다.
배리어는 단일 조정 프로세스로 구현될 수 있지만 이 경우 단일 실패 지점이 된다.
이때 RSM 으로 구현하면 이 문제를 해결 할 수 있다.

Lock 또한 분산시스템의 조정 도구로 RSM 으로 구현될 수 있다.
분산 시스템에서 Locking Services 는 timeout이 있는 lock을 한다.

### Reliable Distributed Queuing and Messaging
워커 프로세스에게 테스크를 분배하는데 큐가 사용될 수 있다.
분산 시스템에서는 큐가 단일 실패지점이 되는 것을 방지하기 위해 RSM 개념이 적용된 복수의 큐를 제공한다.
(리스 시스템 사용)


_※ 로드밸런싱은 분산 시스템에서 과부하를 피해 리소스 사용을 극대화 하기위해 컴퓨팅 테스크를 나누는것. 로드 밸런싱으로 인해 분산합의가 필요한 상황이 될 수 있다._
_또는 로드밸런싱과 관계없이 시스템의 신뢰성을 높이기 위해 분산 시스템을 구축하고 이때 분산합의를 사용할 수 있다._


## Distributed Consensus Performance


## Deploying Distributed Consensus-Based Systems


## Monitoring Distributed Consensus Systems
합의 그룹 멤버 상태, 지연이 발생하는 복제본, 리더 존재 여부, 리더 변경 횟수, 제안과 합의된 제안 수, 지연시간등을 모니터링 해야한다.

## Conclusion
여러 장애상황에도 시스템을 계속 실행 할 수 있는 전략이 필요하다. 분산 시스템을 구성하면 고가용성을 달성 할 수 있지만
상태에 대한 일관된 뷰를 제공하기 위해서는 분산합의 알고리즘을 사용해야 한다.

분산합의는 어떻게 동작할까?
합의 문제에는 여러가지 양상이 존재하고, 신뢰할 수 없는 네트워크가 있다면 어떤 분산 합의 알고리즘도 정해진 시간내에 합의에 다다를 수 없다는게 증명되어있다.
현실에서는 충분히 잘 동작하는 네트워크와 복제본, 무작위 백오프로 분한 합의 문제에 접근한다.
분산 합의의 첫번째 해결책은 Paxos 프로토콜이었다.

분산합의를 사용하는 시스템 아키텍쳐 패턴에는 무엇이 있을까? 
많은 시스템이 분산합의 알고리즘 구현한 서비스(Zookeeper)를 사용한다.
RSM은 오퍼레이션의 셋을 같은 순서로 실행하는 시스템을 말한다. 결정론적인 오퍼레이션을 순서대로 실행한다면, 복제된 state machine은 최종적으로 같은 상태에 다다른다.
RSM은 합의 알고리즘 위의 논리적 계층에서 구현된 시스템으로 합의 알고리즘이 오퍼레이션의 실행 순서에 대한 합의를 처리한다.
RSM 개념은 안정적인 Datastores, Leader Election, Queuing and Messaging 에 사용된다.
예를 들어 Reliable Replicated Datastores 에서는 데이터 스토어는 여러 복제본을 가지며, RSM 이기 때문에 합의 알고리즘에 의해서 데이터가 동기화 되었음을 보장할 수 있다.
분산 시스템에서 리더선출, 중요한 상태의 공유나, 잠금이 필요할 때 분산합의를 사용하는것을 고려해야 한다. (임시 방편을 사용하지 말라)