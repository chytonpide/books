# Chapter 7. Shading

shading 은 데이터베이스를 shard 라는 조각으로 나누는 방법으로, 일반적으로 shard 로 분리된 데이터들은 서로 중복되지 않는다.
하나의 노드가 다루기에는 데이터의 양이 너무 많거나 큰 쓰기 처리량이 요구되는 경우, 데이터를 더 작은 샤드나 파티션으로 분리한 뒤 각 샤드를 서로 다른 노드에 저장할 수 있다.

# 목차
- Pros and Cons of Sharding
- Sharding for Multitenancy
- Sharding of Key-Value Data
  - Sharding by Key Range
  - Sharding by Hash of Key
  - Skewed Workloads and Relieving Hot Spots
  - Operations: Automatic Versus Manual Rebalancing
- Request Routing
- Sharding and Secondary Indexes
  - Local Secondary Indexes
  - Global Secondary Indexes
- Summary

# Pros and Cons of Sharding
- sharding 을 하는 주요 이유는 쓰기 작업에 대한 확장성 확보 이다. 단일 노드에서 다루기에 데이터의 볼륨이 너무 크거나 쓰기 산출량이 너무 클때, 샤딩을 통해서 여러개의 노드로 데이터 및 쓰기를 분산 시킬 수 있다.
- replication 은 장애 내성을 증가와 운영작업을 용이하게 하기 때문에 작은 스케일과 큰 스케일에서 (큰 스케일에서 read 산출량을 증가시킬 때도 사용된다.) sharding 은 많은 복잡성을 야기 하기 때문에 큰 스케일이 필요할 때만 사용하는게 좋다.
  - 어느 샤드에 데이터를 저장할지 partition key 를 선택해야 한다.
  - 관계형 데이터 베이스에서 2차 인덱싱도 분산된 샤드에 걸쳐서 수행되어야 한다.
  - 마찬가지로 여러 샤드에 걸친 분산 트랜잭션이 필요하다.

# Sharding for Multitenancy
- multitenant 시스템으 구현하는데 샤딩이 사용되기도 한다. 장점은 다음과 같다.
  - Resource isolation
  - Permission isolation: 물리적으로 분리되어 있기 때문에 인가 로직의 오류로 인한 사고가 미연에 방지된다.
  - Cell-based architecture: 테넌트 별로 자기 충족적인 서비스와 스토리지로 자기 충적적인 셀로 구성이 가능해지므로, 장애가 격리된다.
  - Per-tenant backup and restore
  - Regulatory compliance
  - Data residence: data residency 법에 따르는게 가능해진다.
  - Gradual schema rollout: 테넌트별로 점진적으로 schema 변경을 롤아웃 하는 것으로 리스크를 감소시킬 수 있다.

# Sharding of Key-Value Data
- 샤딩을 통해서 각 노드에 균등하게 쿼리 부하를 나누었을 때, 노드가 추가되면 rebalancing 이 필요하다.
- 특정 데이터에 대한 수요가 커서 부하가 균등하지 않은 경우가 생길 수 있다. 그러한 데이터의 키를 hot key 라고 부록 노드를 hot spot 이라고 부른다.  
- 데이터 셋을 샤드로 나눌 때, 레코드의 partition key 를 입력했을 때, 레코드가 어떤 샤드에 위치하는지 말해줄 수 있는 알고리즘이 필요하다. 이 알고리즘은 한 스팟을 완화하는 리벨런싱을 처리할 수 있어야 한다.
  - key-value store 에서는 key 자체 또는 key 의 첫 부분이 partition key 이다.
  - RDS 에서는 table 의 한 column 이 partition key 일 수 있다. 

## Sharding by Key Range
- key-range sharding: partition key 연속된 범위를 할당하는 방법 
- 데이터를 균등하게 배분하기 위해서 샤드의 경계는 데이터를 반영해야한다. A-Z 의 키에 대해서 sharding 을 할때, A~B, .., T~Z 로 샤딩 될 수 있다.
- key 는 정렬되어 있기 때문에, 범위 쿼리를 할때는 매우 효율적이다. 단점은 쓰기에 대해서 특정 shard 에 부하가 집중될 수 있다.
  예를 들어 데이터가 생성된 timestamp 로 달별로 sharding 을 하면, 특정 범위의 데이터를 찾는건 간편하지만, 쓰기 요청은 항상 최신의 달을 키 범위로 가진 샤드가 처리하게 된다.
### Rebalancing key-range sharded data
- 데이터의 양이나, 쓰기 산출량이 임계를 넘었을 때 자동으로 진행될 수 있다. 
- Rebalancing 은 log-structured storage engine 의 간결화 compaction 처럼 비용이 큰 작업이다.

## Sharding by Hash of Key
- 근접한 파티션 키가 같은 샤드에 그룹지어지도록 저장할 때는 key-range 샤딩이 적합하다. 그렇지 않은 경우 파티션 키의 해시값을 구하고, 노드 수로 modulo 연산을 하는 방법이 일반적이다.
### Hash modulo number of nodes
- 노드의 수가 바뀌면 modulo 연산의 divisor 도 바뀌어야 하고, 레코드가 어떤 노드에 속하는지를 나타내는 결과값도 바뀐다. 리벨런싱이 있을때 레코드를 이동시켜야 한다.
### Fixed number of shard
- 미리 실제 노드보다 큰 divisor 를 설정하고 각 노드가 여러개의 shard 를 가지도록 하면, 노드가 추가 삭제 될 때, 어떤 샤드를 어떤 노드로 이동시킬지 관리할 수 있다.   
### Sharding by hash range
- key-range shading 과 hash function 을 조합한 방식: 파티션 키 계산에 사용되는 값의 hash 값을 구하고, hash 값의 범위로 shading 을 한다.
- mod 연산은 전체 샤드에 절처서 레코드의 위치가 바뀐다. 구간을 명시적으로 나누어두면, 키 경계만 변경시키는 국소적 결정이 가능해 진다.
- 샤딩에 사용되는 첫번째 키는 해시함수에 대해서 분산되지만, 첫번째 키가 같다는 가정하에 레코드들은 같은 샤드에 위치함으로, 두번째 세번째 키를 사용한 범위 쿼리는 효과적일 수 있다.
### Consistent hashing
- hash range 샤딩의 한 구현, 구간 경계를 테이블에 저장해 두지않고, 해시값으로 구한다. 마찬가지로 리밸런싱이 있을 때, 레코드 이동이 mod 로 분배하는 방식보다 적다.

## Skewed Workloads and Relieving Hot Spots
- 특정 샤드가 레코드를 많이 가지고 있거나, 인플루언서의 포스팅의 키 처럼, 특정 키에 대한 요청 수가 많은 경우가 있다.
- Key-Range 샤딩 처럼, 경계를 관리할 수 있는 샤딩 방법을 사용한다면 특정 레코드를 포함하는 샤딩을 조정할 수 있다. 
- 쓰기가 폭주하는 상황에서 특정 키에, 값을 더해서 여러 샤드로 쓰기 요청을 분산시킬 수도 있다. 물론 읽기를 할 때 모든 샤드의 레코드를 읽어야 한다.
  - 이러한 메카니즘을 사용할 때는 핫키 관리가 필수적이다.
- 어떤 시스템은 hot shards 를 관라하는 자동화된 접근 방법이 있다. 예를들면 Amazon 에서는 이런 메카니즘을 heat management 라고 부른다.

## Operations: Automatic Versus Manual Rebalancing
- 자동으로도 수동으로도 rebalancing 을 수행할 수 있다. 
- rebalancing 은 비용이 큰 작업이다. 요청의 길 설정을 변경하고 많은 양의 데이터를 다른 노드로 전송하고, 노드나 네트워크에 과부하를 유발할 수 있다. 이것이 자동 실패 감지 시스템과 조합되면 연쇄 실패를 낳는다.(cascading failure)
- 이러한 사태를 미연에 방지하기 위해서 리벨런싱 루프에 사람이 있으면 좋다. 이 루프에 사람이 있으면 어떤 이베트에 대비한 선제적인 rebalancing 도 가능하다.

# Request Routing
- request routing: key 를 포함하고 있는 노드로 요청을 보내는 것
- request routing 의 여려가지 방법
  - 일단 임의의 node 로 요청을 보내고, 그 노드가 키를 가지고 있지 않으면 키를 가지고 있는 다른 노드로 전달하는 것
  - 어떤 노드가 key 가지고 있는지 알고 있는 라우팅 단계를 두는 것
  - 클라이언트가 샤딩과 어떤 노드에 샤드가 있는지 알게 하는 것
- 어느 방식이든 노드에 shard 할당이 바뀌면 최신 매핑을 모두가 공유해야 한다. 이를 해결하기 위해서 ZooKeeper 와 같은 분산 조정시스템을 사용한다.

# Sharding and Secondary Indexes
- primary key(또는 그 일부)가 partition key 역할을 하므로, key 기반 쿼리는 담당 shard로 바로 라우팅된다.
- secondary index는 레코드를 유일하게 식별하지 않는다. shard는 primary key 기준으로 나뉘므로, 같은 secondary index key로 조회되는 레코드 들이 여러 shard에 흩어져 있을 수 있다.
## Local Secondary Indexes
- local index: 각각의 샤드가 다른 샤드는 신경쓰지 않고 독립적으로 자신의 Secondary Index 를 관리하는 방식.
- 모든 샤드에 쿼리를 보내서 결과를 합쳐야 하기 때문에, 꼬리 지연시간 확장 때문에 지연시간이 늘어날 수 있고, shard 를 추가해도 각 샤드가 쿼리를 처리함으로(시스템 전체의 QPS는 같다.), 쿼리 산출량을 증가시키지 않음으로 확장성도 없다.
## Global Secondary Indexes
- global index: 모든 샤드의 데이터를 다루도록 하는 방식. 한 노드에서 전부 관리하면 그 노드가 병목이 됨으로, primary-key index 와 다른 방식으로 여러 샤드로 나눠야 한다.
  - secondary index 레코드를 다른 shard 에 저장한다 예를들어 color 를 secondary index 로 했을때 red 를 shard1 에, silver 를 shard2 에 저장한다.
    red, silver 와 같은 term 이 partition key 로 사용된다.
    때문에 full-text-search 에서 term 별로 index 를 만드는 방식과 유사해서, term-partitioned 라고도 한다.
- 여러 조건을 가진 쿼리를 처리할 때, 각 term 들은 다른 shard 에 있을 확률이 높고, 이 레코드들이 긴 postings list 를 가지면 다시 이것들을 종합하는데 시간이 걸린다.
- 쓰기 작업에 대해서 여러 샤드의 index 가 업데이트 되어야 해서 sync 되게 하기 힘들 수 있다.
- global index 방식은 읽기 처리량이 쓰기 처리량 보다 높고, postings list 가 많이 길지 않을 때 유용하다.

# Summary

sharding은 데이터셋이 너무 크거나 쓰기 처리량이 높아 단일 노드로 감당할 수 없을 때,
데이터셋을 더 작은 단위로 나누어 여러 노드에 분산 저장하는 것이다. 각 레코드는 정확히 하나의 shard에 속한다.
나누어진 각 단위를 shard라고 하고, 레코드가 어느 shard에 속할지 결정하는 값을 partition key라고 한다.

sharding은 데이터 크기와 쓰기 처리량을 확장하기 위한 방식으로, 읽기 확장과 내결함성을 위한 replication과는 역할이 다르며 보통 함께 사용된다.
데이터셋을 여러 shard에 분산시키면 partition key 선택, rebalancing, secondary index 처리, 분산 트랜잭션 등의 복잡성이 생기므로 필요할 때만 사용해야 한다.
multitenant 시스템을 구현할 때, 클라이언트 별로 데이터를 나누는 방법으로 sharding이 사용되기도 한다.

key-value store 에서 shard 를 나누는 방법에는 크게,
파티션 키의 연속된 볌위를 shard 에 할당 하는 방법. 파티션 키의 hash 값을 사용하는 방법이 있다.
리밸런싱은 shard 가 추가되거나 감소할 때 데이터를 재 배치하는 작업이다.
key range 를 정해서 나누는 방법은 범위를 테이블에 저장해서 관리하기 때문에 리벨런싱이 수월하다.
범위 쿼리가 효율적이고 특정 범위의 샤드가 쓰기 수요가 몰리는 hot spot 문제가 발생할 수 있다.
hash 값과 나머지 연산을 사용해서 나누는 방법은 hash function 에 의해 값들이 분산 되기 때문에 key range 와 같은 문제는 없다.
hash 값을 다시 shard 로 매핑할때 나머지 연산을 사용할 수 있다. 이때 shard 수가 변하면 배정이 거의 다 변경됨으로 shard 수 변경에 따라 그때 그때 리벨런싱을 하는것이 매우 어렵다.
이때 shard 수를 고정하는 방법을 사용할 수 있다. node 와 shard 를 분리한 다음, node 보다 훨씬 큰 수의 shard 수를 제수로 사용해서 나머지 연산을한 결과로 node 에 여러 shard 를 할당 하는 것으로 이 문제를 해결할 수 있다.
노드가 추가되면 shard 단위로 옮기면 되고 key-shard 매핑은 바뀌지 않는다.
분산된 hash 값으로 다시 key range 방식으로 샤딩을 하는 방법이 있다. 키와 샤드의 매핑을 명시적으로 관리하는 것으로 리벨런싱을 훨씬 수월하게 진행할 수 있다.
리벨런싱은 자동으로도 수행할 수 있지만, 비용이 큰 작업으로, 실패 감지 시스템과 조합되어서 실패를 오판하면 연쇄 장애가 수반될 수 있다. 때문에 사람이 루프에 있는게 더 낫다.

특정 키와 관련된 요청을 해당 shard 를 가진 node 로 보내는 것을 라우팅 이라고 한다. 
크게 샤드들이 매핑 정보를 가지고, 자신이 담당하지 않는 키에 대한 요청이 올 경우 다른 노드로 리다이렉트 하는 방법.
하나의 라우팅 단계(routing tier)를 를 추가해서 그 티어를 통해서 라우팅이 이루어지게 하는 방법
클라이언트가 샤딩과 샤드 와 노드의 매핑 정보를 가지고 있도록 하는 방법이 있다.
어느 방식이든 노드에 shard 할당이 바뀌면 최신 매핑을 모두가 공유해야 한다. 이를 해결하기 위해서 ZooKeeper 와 같은 분산 조정시스템을 사용한다.

primary key(또는 그 일부)가 partition key 역할을 하므로, key 기반 쿼리는 담당 shard로 바로 라우팅된다.
secondary index는 레코드를 유일하게 식별하지 않는다. shard는 primary key 기준으로 나뉘므로, 같은 secondary index key로 조회되는 레코드 들이 여러 shard에 흩어져 있을 수 있다.
secondary index 를 구성하는 방법으로 local index, global index 두가지 방법이 있다.
local index 는 각각의 shard 가 다른 샤드와 독립적으로 자신의 secondary index 를 관리하는 방법으로, 쿼리 결과를 얻기 위해서 모든 샤드에 쿼리를 보내 결과를 취합한다.
쓰기 작업에 대해서 자신의 shard 만 업데이트 해도 된다.
꼬리 지연시간 확장 문제가 있고 node 를 추가해도 시스템 전체의 쿼리 산출량은 늘지 않는다.
global index 는 전체 레코드에 대해서 secondary index 를 만들고 다시 여러 shard 가 나누어 가지는 방식이다(term-partitioned). secondary index 의 key 가 다시 partition key 로 사용된다.
쓰기 작업에 대해서 여러 샤드의 secondary index 가 업데이트 해야되는 문제가 있다.
읽기 처리에 대해서 local index 처럼 모든 shard 에 쿼리를 할 필요가 없어서 비교적 효과적이다. 따라서 읽기 처리량이 많이 요구될 때 사용한다.