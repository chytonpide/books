# Load Balancing in the Datacenter
이 장에서는 데이터 센터에서의 로드밸런싱 특히 쿼리 스트림을 분산하는 알고리즘을 다룬다.
데이터 센터에서는 쿼리들을 다루는 서비스들이 있고 이러한 서비스들은 보통 100~1000 개의 테스크를 가진다.

```
metric 측정기준
overload 과부하
overhead 간접비
```

## Ideal case
이상적인 경우 특정 서비스의 부하가 모든 백엔드 작업에 완벽하게 분산되고 특정 시점에서 가장 부하가 높은 테스크와 가장 낮은 테스크의 cpu 사용량 동일할 것이다.
현실은 그렇지 않으며, 가장 부하가 높은 테스크와 가장 부하가 낮은 테스크의 cpu 사용량의 차이는, 준비된 리소스를 충분히 사용하지 못하는 낭비가 발생한다.


## Identifying Bas Tasks: Flow Control and Lame Duck
### A Simple Approch to Unhealty Tasks: Flow Control
클라이언트 테스크가 백엔드 테스크에 보내진 활성 요청수를 추적해서, 100 개를 초과할 때 다음 다른 백엔드 테스크에 요청을 보내도록 하는 방법이 있다.

### A Robust Approch to Unhealthy Tasks: Lame Duck State
task가 shutdown 상태에 들어갈때 Lame Duck State 에서 클라이언트의 요청을 받지 않음으로,
shutdown 된 task 에 요청을 보내 error 가 발생 하는것을 방지하면 이 명백함이 유지보수 활동에 도움이 된다.

## Limiting	the Connections Pool with Subsetting
커넥션을 매번 새로 생성하는건 오버헤드는 얼핏 작아보이지만 큰 규모의 시스템에서는 문제가 될 수 있다.
따라서 클라이언트가 요청을 보낼 수 있는 벡엔드의 하위집합을 설정해 커넥션을 유지하고,
idle한 커넥션이 발생하면 inactive 모드로 변경되도록 함으로써 이러한 오버헤드를 감소 시킬 수 있다.

### Picking the Right Subset
클라이언트와 백엔드테스크의 비율, 클라이언트의 작업에 성격에 따라서 서브셋 사이즈가 결졍되어야한다.
예를들어 클라이언트수가 적고 백엔드의 수가 훨씬 크다면 서브셋이 크지 않을때 그냥 놀고있는 백엔드가 많아질 것이다.
이러한 서브셋 크기는 알고리즘에 의해서 결정되어야 한다.

### A Subset Selection Algorithm: Random Subsetting
랜덤하게 가용한 백엔드중 하위집합을 선택하도록 했더니 부하가 잘 분산되지 않았다.
이 알고리즘을 사용해서 부하를 균등하게 분산하려면 전체 풀의 75%를 하위집합으로 선택해야 했다.

### A Subset Selection Algorithm: Deterministic Subsetting
결정적 서브셋팅 알고리즘은 동일한 입력에 대해서 항상 균등하게 subset을 만들어내는 만들어낸다.

```
def Subset(backends, client_id, subset_size):
  subset_count = len(backends) / subset_size

  # Group clients into rounds; each round uses the same shuffled list:
  round = client_id / subset_count
  random.seed(round)
  random.shuffle(backends)

  # The subset id corresponding to the current client:
  subset_id = client_id % subset_count

  start = subset_id * subset_size
  return backends[start:start + subset_size]
```

## Load Balancing Policies
백엔드 서브셋을 유지하는 방법에 이어서 정해진 서브셋에서 요청을 처리할 백엔드를 고르는 알고리즘을 살펴봐야 한다. 
부하 분산은 백엔드의 정보를 전혀 사용하지 않는 방법도, 사용하는 방법도 있다.
### Simple Round Robin
단순하지만 아직도 많이 사용한다. 랜덤보다 성능이 좋다. cpu 사용량의 분산이 2배가 될 수도 있다. (큰 낭비가 발생한다.)
작은 하위집합, 다양한쿼리비용, 머신 다양성, 예상못한 성능 요소로 인해 이러한 분산이 야기될 수 있다.
#### Small subsetting: 특정 클라이언트가 요청을 많이하고 서브셋이 충분히 크지 않다면, 그 클라이언트의 하위집합에 속하는 백엔드의 부하가 커진다.
#### Varying query costs: 요청마다 비용이 다르다. 요청당 작업량을 제한하기 위해서 인터페이스를 조절해야 할 수도 있다.(예를들어 페이징) 
#### Machine diversity: 머신 성능이 다르기 때문에 GCU 라는 단위를 도입했다.
#### Unpredictable performance factors:
	- 리소스를 어떤 요청들은 경쟁적으로 사용할 수 있다.
	- 만약 빈번한 푸쉬로 테스크가 빈번하게 재시작된다면, 재시작이 많은 리소스를 사용하기 때문에 재시작 프로세스를 기다려야하고 이것이 문제가 될 수 있다. 
### Least Loaded Round Robin
백엔드가 처리하고 있는 활성 요청의 수를 추적하고 가장 부하가 적은 백엔드에 요청을 보내는 알고리즘
아래와 같은 문제가 발생 할 수 있다.
	- 싱크홀, 에러를 리턴하면서 처리중인 요청이 0개인 테스크로 계속 요청을 보냄
	- I/O 를 포함한 레이턴시가 클라이언트가 보는 레이턴시임으로 더 빠른 백엔드가 있어도 같은 부하를 처리한다고 본다. (I/O가 느리고 처리가 빠른 백엔드가 더 많은 작업을 처리 해야한다.)
	- 자신이 보내는 리퀘스트만 봐서 실제로 다른 클라이언트가 백엔드가 보낸 요청을 보지 않는 한계가 있는 뷰를 가진다.
### Weighted Round Robin
백엔드와 통신해서 부하에 대한 정보를 얻고 이를 다시 어느 백엔드에 리퀘스트를 보낼지의 결정에 사용하는 알고리즘
