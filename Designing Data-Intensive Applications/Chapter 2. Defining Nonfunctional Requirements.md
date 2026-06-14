# Chapter 2. Defining Nonfunctional Requirements
- 데이터 집중적인 어플리케이션에서 품질 속성과 관련된 비 기능적 요구사항을 정의하는 방법을 배운다. 

## Representing Users, Posts, and Follows
# Case Study: Social Network Home Timelines
- 5초안에 새로운 포스트를 보여주기 위한 요구사항을 위해서 폴링을 사용하면, 유저가 많아지면 조인 쿼리의 비용은 기하급수적으로 증가한다.
  - lookups 하는 row 의 수가 기하급수적으로 증하한다.
## Materializing and Updating Timelines (타임라인 생성 및 업데이트)
- 폴링이 아니라 푸쉬를 한다. 타임라인 요청을 캐시에서 제공할 수 있도록 미리 계산한다.
- 사용자마다 타임라인의 자료구조를 가지고, 포스트가 발행되면 팔로워의 타임라인에 게시물을 삽입한다. 유저 클라이언트는 스트림을 구독고 있는걸로 포스트의 도착을 바로 알 수 있다.
  그 결과 타임라인은 derived (파생) 데이터가 된다.
- This process of precomputing and updating the results of a query is called materialization, and the timeline cache is an example of a materialized view

# Describing Performance
소프트웨어 성능을 이야기 할때 두가지의 주요한 타입의 메트릭을 고려한다.  
- response time: 응답까지 걸리는 시간
- throughput: 시간당 처리량, 예를들어 초당 리퀘스트 처리
- maximum throughput 을 쉽개 늘릴수 있다면 시스템은 scalable 하다고 볼 수 있다.

## Latency and Response Time
이 책에서는 Latency 와 Response Time 을 구분한다.
- response time: 유저 관점에서 요청을 보내서 응답이 올때까지 전체 시간
- latency: 적극적으로 처리되는 시간되에 걸리는 잡다한 시간들 (특히 네트워크 레이턴시)
- Queuing delay
- Service time

## Average, Median, and Percentiles
- This makes the median a good metric if you want to know how long users typically have to wait. (50th percentile - p50)
- To figure out how bad your outliers are, you can look at higher percentiles: the 95th, 99th, and 99.9th percentiles are common (tail latencies)
  - directly affect users’ experience of the service.

## Use of Response Time Metrics
- histogram: 분포 요약도. 평균은 의미가 없을 수 있다.

----

# Reliability and Fault Tolerance
- reliable 한 소프트웨어에 대한 기대
  - The application performs the function that the user expected.
  - The application can tolerate the user making mistakes or using the software in unexpected ways.
  - Its performance is good enough for the required use case, under the expected load and data volume.
  - The system prevents any unauthorized access and abuse.
- Reliability: 만약 이 모든 요소가 합쳐져 “제대로 작동한다”는 의미를 갖는다면, 신뢰성을 대략 “문제가 발생하더라도 계속해서 제대로 작동하는 것”으로 이해할 수 있다.
  - Fault: A fault occurs when a particular part of a system stops working correctly
  - Failure: A failure occurs when the system as a whole stops providing the required service to the user
- The distinction between faults and failures can be confusing because they are the same thing, just at different levels.
- if the system consists of multiple hard drives, the failure of a single hard drive is only a fault from the point of view of the bigger system, and the bigger system might be able to tolerate that fault by having a copy of the data on another hard drive.

## Fault Tolerance
- We call a system fault-tolerant if it continues providing the required service to users in spite of certain faults occurring.
- If a system cannot tolerate a certain part becoming faulty, we call that part a single point of failure (SPOF), because a fault in that part escalates to cause the failure of the whole system.
- Fault tolerance is always limited to a certain number of certain types of faults. For example, a system might be able to tolerate a maximum of two hard drives failing at the same time, or a maximum of one out of three nodes crashing. It would not make sense to tolerate any number of faults; if all nodes crash, nothing can be done.
- Although we generally prefer tolerating faults over preventing faults, in some cases where prevention is better than cure →　security

## Hardware and Software Fault
- Hardware fault: These events are rare enough that you often don’t need to worry about them when working on a small system, as long as you can easily replace hardware that becomes faulty. However, in a large-scale system, hardware faults happen often enough that they become part of normal system operation.

### Tolerating hardware faults through redundancy
- Redundancy is most effective when component faults are independent—that is, when the occurrence of one fault does not change the likelihood that another fault will occur.
- Hardware redundancy increases the uptime of a single machine; however, as discussed in “Distributed Versus Single-Node Systems”, using a distributed system has advantages, such as being able to tolerate a complete outage of one datacenter. For this reason, cloud systems tend to focus less on the reliability of individual machines and instead aim to make services highly available by tolerating faulty nodes at the software level.
- operational advantages: planned downtime vs rolling upgrade
### Software Fault
- software faults are often very highly correlated, because it is common for many nodes to run the same software and thus have the same bugs. 
  Such faults are harder to anticipate, and they tend to cause many more system failures than uncorrelated hardware faults.
- The problem of systematic faults in software has no quick solution. Lots of small things can help: 
  - carefully thinking about assumptions and interactions in the system; 
  - thorough testing; ensuring process isolation; 
  - allowing processes to crash and restart; 
  - avoiding feedback loops such as retry storms
  - measuring, monitoring, and analyzing system behavior in production.
## Humans and Reliability
- 사람은 특성상 실수를 한다. 이때 실수를 한 사람을 비난하는건 비생산 적이다. 시스템적으로 문제를 완화시키는 방법을 취해야 한다.
- As a general principle, when investigating an incident, you should be suspicious of simplistic answers. 
  “Bob should have been more careful when deploying that change” is not productive, but neither is “We must rewrite the backend in Haskell.” Instead, management should take the opportunity to learn the details of how the sociotechnical system works from the point of view of the people who work with it every day, and take steps to improve it based on this feedback.


-----

# Scalability
- Scalability is the term we use to describe a system’s ability to cope with increased load.
- perhaps at a startup, the overriding engineering goal is usually to keep the system as simple and flexible as possible so that you can easily modify and adapt the features of your product as you learn more about customers’ needs. In such an environment, it is counterproductive to worry about hypothetical scale that might be needed in the future.

## Understanding Load
- First, you need a clear understanding of the current load on the system.
- Once you understand the load on your system, you can investigate what happens when the load increases. You can look at this in two ways:
  - When you increase the load in a certain way and keep the system resources unchanged, how is the performance of your system affected?
  - When you increase the load in a certain way, how much do you need to increase the resources if you want to keep performance unchanged?
- Usually the goal is to keep the performance of the system within the requirements of the SLA while also minimizing the cost of running the system. (SLA 가 아니라 SLO 가 더 정확한 것 같다.)

## Shared-Memory, Shared-Disk, and Shared-Nothing Architectures
- vertical scaling or scaling up : 더 많은 core, ram 을 가진 머신으로 대응하기
- shared-memory architecture: scaling up 에 가까움.
- shared-disk architecture: scaling out 에 가까움.
- shared-nothing architecture (horizontal scaling or scaling out): 복수개의 노드(자신의 cpu와 ram을 가지는)를 가지는 분산 시스템, software 에 의해서 조화됨
  - 장: 유연하게 하드웨어 리소스를 조정할 수 있고, fault tolerance 하다.
  - 단: 명시적인 샤딩이 필요하고, 분산시스템의 모든 복잡성이 수반된다.
  - shared-disk architecture 와 비교하면, NAS 같은게 아니라 storage API 를 제공한다.

## Principle for Scalability
- 큰 스케일에서 운영되는 시스템의 아키텍쳐는 어플리케이션에 따라 크게 다르다. 특정 단계에서의 적절한 아키텍쳐는 부하가 10대 증가 했을때는 적합하지 않을 가능성이 있고, 부하의 자릿수가 바뀔 때마다 아키텍쳐를 다시 생각해야 할 수도 있다.
- 확장성을 위한 좋은 기본 원칙은 시스템을 서로 대체로 독립적으로 작동할 수 있는 더 작은 구성 요소들로 나누는 것이고 또한, 필요이상으로 복잡하게 만들지 않는 것이다. 만약 데이터베이스가 단일 서버로 충분하다면 분선 셋업을 가질 필요가 없다. 

----

# Maintainability
- 어플리케이션의 요구사항은 진화하고 소프트웨어가 동작하는 환경도 변한다.
- 이 책에서는 Maintainability 를 위해서 다음과 같은 원칙에 주의를 기울인다.
  - Operability: 조직이 시스템을 쉽게 운영하도록 해라.
  - Simplicity: 잘 이해되고 일관적인 패턴과 구조로 구현하는 것으로 새로운 엔지니어가 시스템을 쉽게 이해하도록 하라. 불필요한 복잡성을 피하라.
  - Evolvability: 엔지니어들이 시스템을 쉽게 조정하고 확장할 수 있도록 하라.

## Operability: Making Life Easy for Operations
- 대규모 시스템에서 자동화는 필수지만, 양날의 검이다. 몇몇 엣지 케이스는 여전히 사람이 다루어야하고 자동화 수준을 높이라면 이런 문제를 해결할 수 있는 숙련된 팀이 필요하다.
  보통 자동화된 시스템에서 발생하는 오류는 수동으로 특정 작업을 수동으로 수행하는 시스템보다 문제 해결이 어렵다. 자동화는 필요하지만 어느정도가 최적점은 어플리케이션마다 다르다.
- 좋은 Operability 는 일상적인 업무를 쉽게 만들어서 운영팀이 고 부가가치 활동에 집중할 수 있도록 하는 것을 의미한다. 데이터 시스템에서는 다음과 같은 방식으로 도움을 줄 수 있다.
  - 런타임 행동에 대한 통찰을 주는 메트릭을 제공한다.
  - 단실 실패점이 되지 않는다.
  - 기본적인 자동화를 제공하면서도, 운영자가 컨트롤 할 수 있는 계기판(instrument)를 제공한다.

## Simplicity: Managing Complexity
- reducing complexity greatly improves the maintainability of software, and thus simplicity should be a key goal for the systems we build.
- Unfortunately, this is easier said than done. Whether something is simple is often a subjective matter, as there is no objective standard of simplicity.
- One attempt at reasoning about complexity breaks it into two categories: essential and accidental. The idea is that essential complexity is inherent in the problem domain of the application, while accidental complexity arises only because of limitations of our tooling.
- One of the best tools we have for managing complexity is abstraction. A good abstraction can hide a great deal of implementation detail behind a clean, simple-to-understand façade.
  - A good abstraction can also be used for a wide range of applications. 
  - Not only is this reuse more efficient than reimplementing a similar thing multiple times, but it also leads to higher-quality software, as quality improvements in the abstracted component benefit all applications that use it.
- For example, high-level programming languages are abstractions that hide machine code, CPU registers, and system calls.
  Of course, when programming in a high-level language, we are still using machine code; we are just not using it directly, because the programming language abstraction saves us from having to think about it.

## Evolvability: Making Change Easy
- It’s extremely unlikely that your system’s requirements will remain unchanged forever. They are much more likely to be in constant flux: you learn new facts, previously unanticipated use cases emerge, business priorities change, users request new features, new platforms replace old platforms, legal or regulatory requirements change, growth of the system forces architectural changes, etc.
- The ease with which you can modify a data system and adapt it to changing requirements is closely linked to its simplicity and its abstractions. Loosely coupled, simple systems are usually easier to modify than tightly coupled, complex ones. Since this is such an important idea, we will use a different word to refer to agility on a data system level: evolvability

----
# Summary
성능, 신뢰성, 확장성, 관리용이성 비 기능적 요구사항에 대해 알아 봤다.
SNS 의 홈 타임 라인을 구현할때 규모가 커질 때 발생하는 도전에 대한 케이스 스터디 부터 시작했다.
이를 통해서 성능과 부하를 측정하는 방법과 몇가지 메트릭에 대해서 배웠다.

확장성은 성능과 밀접하게 연관된 개념으로 부하가 증가할 때 성능을 동일하게 유지할 수 있는 능력을 의미한다.
확장성과 관련된 여러 아키텍처에 대해서 배우고(Shared-Memory/Disk, Shared-Nothing) 부하의 자릿수마다 요구되는 아키텍쳐가 다를 수 있음을 배웠다. 
시스템을 독립적으로 운영될 수 있는 작은 부분으로 나누는 것이 확장성을 달성하기 위한 일반적인 원칙이라는 것을 배웠다.

신뢰 할만 하다는 것은, 기본적으로는 유저의 기대대로 동작한다는 것을 말한다.
신뢰성은 문제가 발생하더라도 계속해서 제대로 동작하는 능력의 의미한다.
failure 와 fault 는 비슷 하지만, 다른 단계를 묘사하는 용어로 failure 가 더 높은 수준이다.
신뢰성을 달성하기 위해서 fault-tolerance 테크닉을 사용할 수 있는데 이는, 시스템의 어떤 컴퍼넌트가 faulty 하더라도 시스템 전체가 failure 하지 않고, 계속 해서 서비스를 제공할 수 있도록 하는 것이다.
하드웨어 fault 와 소프트웨어 fault 에 대해서 배웠다. 소프트웨어 fault 가 연관관계가 높아서 더 다루기 어렵다.
신뢰성을 달성하기 위해서는 사람의 실수에 대해서도 회복력을 세우는 것인데 이는 문화와 관련된 것으로 예를 들어 비난 없는 사후검토와 같은 것이다.

관리용이성을 운영용이성, 단순성, 진화용이성의 측면에서 살펴봤다.  
운영용이성은 일상적인 업무를 쉽게 만들어서 운영팀이 고 부가가치 활동에 집중할 수 있도록 하는 것을 의미한다.
자동화는 필수적이지만 양날의 검이고 어디까지 자동화할지 최적점은 어플리케이션 마다 다르다.

단순성을 추구하는 것은 복잡성을 제거 하는 것이라고 말할 수 있다.
돌발복잡성과 본질적 복잡성을 구분해서 돌발 복잡성을 제거하는 시도를 할 수 있다.
추상화도 복잡도를 제어하기 위한 좋은 예이다. 고수준 프로그래밍언어와 같이 추상화를 통해서 많은것들에 대해서 신경을 끌 수 있다.
이렇게 추상화된 인터페이스는 재사용가능하고 추상화된 요소만 품질 개선을 하는것으로 이를 사용하는 모든 어플리케이션에 이점을 준다.

요건은 계속해서 변한다. 진화용이성은 이러한 변경에대해서 얼마나 시스템을 쉽게 적응 시킬 수 있는지를 말한다.
진화용이성은 시스템의 단순성과 추상화 수준과 관련이 있다. 느슨하게 결합된 단순한 시스템은 강하게 결합된 복잡한 시스템보다 수정하기 쉽다.

