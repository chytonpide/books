# Software Engineering in SRE

## Why is Software Engineering Within SRE Important
구글의 규모에서 필요한 타사 도구가 없기 때문에 내부적으로 소프트웨어를 개발 해야 했고 이는 SREs 를 통해 이루어 졌다.
SREs 는 다음과 같은 이유로 소프트웨어를 효과적으로 개발할 수 있다.
- 실제 프러덕션 환경에 대한 깊은 지식이 있기 때문에 여러 방면을 고려하여 소프트웨어를 만들 수 있다.
- 소프트웨어를 개발하려고 하는 문제 분야에 직접 몸담고 있기 때문에 요구사항을 쉽게 이해할 수 잇다.
- 조직내에서 다른 동료 SREs 에게 좋은 피드백을 얻을 수 있고, 이 내부 사용자는 알파버전에 관대하다.

SRE의 기본 원칙 중 하나인 "팀 규모가 서비스 성장에 따라 직접 확장되어서는 안 된다" 을 지키기 위해서 비효율을 제거해야 하고,  
프러덕션 시스템을 직접 경험한 사람들이 이에 기여할 수 있는 도구를 개발하는 것은 의미가 있다.  
한편 SRE 내에서의 소프트웨어 개발 프로젝트는 커리어를 개발하고 코딩스킬이 녹슬게 두고싶지 않은 SREs 의 요구에도 부합한다.  
또한 다양한 기술을 가진 엔지니어를 유치함으로써 이로인해 문제 해결 접근방식의 사각지대를 방지 할 수 있다.  


## Auxon Case Study: Project Background and Problem Space
### Traditional Capacity Planning
캐패시티 플래닝은 아래의 사이클로 근사화 할 수 있다.
- Collect demand forecasts.
- Devise build and allocation plans.
- Review and sign off on plan.
- Deploy and configure resources.
위의 주기가 반복되고 수요에 맞게 공급을 수동으로 조정해야한다.
### Brittle by nature
작은 변경에도 계획 전체를 수정해야한다.

### Laborious and imprecise
캐패시티 플래닝을 수동으로 하는것은 노력이 많이 들고 정화하지 못한다.
리소스를 효율적으로 사용하기 위한 빈 패킹은 NP-hard 이다. 
```
P: 다항시간 내에 해답을 구할 수 있는 문제, 결정론적 알고리즘이 있는 문제 (예,두수의 합을 구할 수 있는 문제)
NP: 다항시간 내에 해답을 구할 수 있는지는 알지 못하지만, 정답인지 판별할 수 있는 문제.
P는 NP의 부분집합.
NP-hard: NP의 문제가 다항시간내에 변환이 가능하면 NP-hard 이다.
NP-complete: NP-hard 이면서 NP인 문제
```

### Our Solution: Intent-Based Capacity Planning
Specify the requirements, not the implementation.
의도에 대해서 프로그래밍 방식으로 인코딩하면, 할당계획을 자동으로 생성한다. (최적을 찾는 어떤 알고리즘이 적용된다.)


## Intent-Based Capacity Planning
의도는 서비스 소유자가 서비스를 어떻게 운영하고자 하는지에 대한 원리이다.  
의도는 여러 추상화 단계를 가질 수 있고 모든 추상화 단계가 전달되면 좋지만 사람이 이해할 수 있는 레벨이 가장 적합하다.

### Precursors to Intent (의도의 선행물) 
종속성, 성능 메트릭을 우선순위와 함께 제시함으로써 서비스의 의도를 파악할 수 있다.
#### Dependencies
프러덕션의 종속성은 중첩되어 있고 이것을 고려해야한다. 예를 들어 foo가 bar에 의존할 때 지연이 30 밀리초 이내라는 요구사항이 있다면 foo 와 bar 의 위치에 대해 고려해야 한다.

#### Performance metrics
성능 메트릭은 종속성 사이의 접착제 역할을 한다. 예를들어 foo가 bar에 의존할 때 foo 가 n 개의 사용자 쿼리를 처리할 때, bar 는 어느정도의 Mbps 를 가져야 하는지 고려해야 한다.

#### Prioritization
리소스의 제약사항은 트레이드 오프를 초래한다. 예를들어 foo에 대한 n+2 가, bar 의 n+1 보다 중요할 수 있다.  
의도 기반 계획을 사용하면 우선순위를 세분화 하거나 개괄적으로 설정할 수 있다.  

### Introduction to Auxon
의도를 설명하는데는 dependencies 와 performance metrics(dependencies 의 glue역할), Prioritization 이 중요하다.  
Auxon은 Configuration Language Engine 과 Solver 로 나누어진다.  
Configuration Language Engine 은 intent configuration 가 도출해 낸 혼합 정수 프로그래밍을 해석하여 allocaiton plan 을 도출한다.  

### Requirements and Implementation: Successes and Lessons Learned
SRE들은 이미 수동으로 capacity 플래닝을 하고 있었기 때문에 비효율과, 자동화를 통한 개선의 기회를 잘 이해할 수 있는 위치에 있었다.  
SRE들은 수많은 서비스의 프러덕션에 관련되어 있었고, Auxon의 개발자이면서 문제공간을 직업 경함한 소비자 였다.   
이것이 Auxon 의 신뢰도와 정당성을 부여하는데 도움이 되었다.  

#### Approximation(근사치, 예광탄):
문제의 범위에 대해서 대해서 잘 알지 못할 경우에는, 완벽과 솔루션의 순수성에 집중하지말고 일단 만들고 반복해라.  
Auxon 프로젝트에서는 일단 Stupid Solver 를 먼저 만들었다. 이것이 최적해를 주지는 않았지만 솔루션이 실현 가능하다는 것을 알게 되었다.  
느슨하게 연결해야 변경이 용이하다. 모호한 요구사항을 설계를 일반화 하고 모듈화 하는데 사용할 수 있다.  
Auxon이 소비자에 대해서 무지하게 일반적으로 유용한 allocation plan을 도출 하도록했다.  
이렇게 하면 하위 시스템은 각각의 자체 통합지점을 가지면 된다.  
Auxon 사례에서 얻을 수 있는 한가지 모토가 있다면, 일단 시작하고 반복하라는 것이다.   
**완벽한 디자인을 기다리지 말고 일단 설계와 개발을 진행하고, 불확실한 영역에서는 상위 수준의 전략이나 프로세스가 변경되더라도 막대한 재작업 비용이 발생하지 않도록 소프트웨어를 유연하게 설계해야 한다.**   


#### Raising Awareness and Driving Adoption
프로젝트를 알리고 도입을 촉진하는건 적지 않은 노력이 든다. 일관된 접근과 유저의 옹호가 필요하다.     
초기 버전부터 문서를 제공해야한다. 만약 어렵거나 혼란스로우면 직접 솔루션을 작성할 것이다.  

#### Set Expectation
**기대치를 잘 설정해야한다. 최종 목표와, 최소 성공기준을 구분하는게 중요하다.**  
너무 많은 것을 약속하면 안되고 소규모 릴리즈를 통해 점진적인 진전을 보여주면 사용자의 신뢰를 얻을 수 있다.   
전체 로드맵을 공개하고 어떤게 가능하고 가능하지 않은지 공개한다음, 계속 해서 업데이트 해야한다.  
Auxon 의 경우, 다음과 같은 것을 약속했다.  
 - 구성 작업을 통해 수동으로 빈 패킹을 하는 수고스러움을 즉각적으로 덜어줄 것이다.
 - 기능이 추가되도 기존 구성은 그대로 사용할 수 있지만, 이점의 범위는 커질 것이다.

#### Identify appropriate customers
기존 팀들은 자체적인 자동화 솔류션을 가지고 있기때문에 유저로써 적합하지 않았다.  
새로운 팀들은 자동화 솔루션을 자신들이 작성하던, 다른것을 도입하던 해야했기 대문에 Auxon에 관심을 보였다.   
이 커스터머들의 성공은 프로젝트의 유용성을 입증하고, 커스터머들을 Auxon의 지지자로 만들었다.  
이후 이 사례는 다른 팀들의 Auxon 사용에 대한 동기부여를 했다.  
#### Customer service
사용자가 엔지니어여도 학습곡선이 존재한다. 따라서 얼리 어덥터 들이 온보딩을 하는 것을 도와줘야한다.  
커스터머서비스를 통해서 피드백을 얻고 옹호자를 만들 수 있다.  
#### Designing at the right level
Auxon 을 다른 서비스에대해 무지하게 하는것은 중요한 설계 컨셉이다.   
특정한 유저에 집중하지 않음으로써 여러 유저들을 얻을 수 있었다.  
**또 의식적으로 조직은 모든 팀이 Auxon 을 사용하도록 하는것에 목표를 두지 않았다.  
몇몇의 엣지케이스를 커버하는데 드는 비용이 너무 클 수 있기 때문이다.**   
#### Team Dynamic
제네럴 리스트와 스페셜리스트로 팀을 구성하는데 이점이 있다는걸 발견했다.  
다향한경험은 사각지대를 커버하고, 잘못된 혹은 너무 작은 범위의 유즈케이스를 가정하는 함정도 방지한다.  
스페셜 리스트는 조직이 작다면 아웃소싱을 할 수 도 있다.  
Auxon 의 경우 초기 설계 문서를, 운영 연구 및 정량분석을 전문적으로 하는 부서에게 검토받았다.  
이후에는 통계및 수학 백그라운드를 영입했다. 이 맴버는 프로젝트의 기본기능이 완성되고 정교함을 더하는것이 과제가 되었을 때 개선이 필요한 부분을 파악했다.  
일반적으로 프로젝트가 초기성공을 거둔 다음, 추가적인 멤버가 추가적인 전문지식으로 팀의 기술이 강화될 수 있을 때 스페셜리스트를 영입하는게 좋다.  

## Fostering Software Engineering in SRE
프로젝트가 성공적이려면, 문제영역에서 직접적인 경험이 있는 프로젝트에서 일하는게 관심이 있는 엔지니어가 참가해야한다.  
프로젝트가 조직 전반의 목표에 부합하도록 하면, 조직간 검토를 통해 중복되는 노력을 방지하고, 스태핑이 더욱 쉽다.  

프로젝트가 좋지 않다는 징후는, 너무 많은 움직이는 타켓을 노리거나, 전부아니면 전무의 반복적인 개발방식이 적용할 수 없는 프로젝트이다.  
프로젝트의 범위또한 중요한다. 너무 넓고 일반적이면 유즈케이스를 정확히 충족할 수 없고, 가치를 덜 제공할것이고 너무 좁으면, 전체 조직에서 적은 영향만 미칠 것임으로 다른 팀이 이 프로젝트에 대한 기여가 후순위로 밀릴 수 있다.
	
### Successfully Building a Software Engineering Culture in SRE: Staffing and Development Time
SRE는 큰 그림을 보는데 초점을 맞춰서, "설계가 있는데 왜 디자인이 필요하냐" 라는 말을 하기도 한다.  
사용자 대면 프러덕트를 개발하는 엔지니어나 PM과 같이 협력하게 하면, 제품 개발과 프러덕션 환경 경험의 장점을 모두 갖춘 소프트웨어 개발 문화를 구축할 수있다.  
별도의 개발 시간을 확보해주는것이 중요하다.  
사이드 프로젝트에서 시작해서, 계속해서 남는 시간에 개발을 하거나, 구조화된 프로세스를 따르는 정식 프로젝트가 되거나, 리더쉽의 지원을 받는 대규모 프로젝트가 될 수 있다.  
프로젝트에 참가하더라도 SRE 는 계속해서 SRE 업무를 해야한다. 커스터머이면서 크리에이터인 점에서 귀중한 관점을 얻을 수 있다.

### Getting There (달성 하는 방법)
프러덕션 지원을 중점으로 하는팀에서 소프트웨어 개발한다는 것은 기술적인 과제인 동시에 조직의 변화이다.  
SREs 들은 당장 문제를 해결하려고 하는데 문제를 해결하기 위한 임시방편은 확장되기 힘들고, 중복된 노력을 만든다.  
소프트웨어 개발한다는 것은 이러한 SREs 의 본능에 거스르는 것이다.  
팀 성장과, 조직 전반에서 사용되는 프러덕트 개발중 목표가 어떤 것인지 생각해야한다.  
구글은 다음과 같은 가이드라인을 제공한다.  
- Create and communicate a clear message: 전략과 계획을 정의하고 공유해야한다.
- Evaluate your organization’s capabilities: SRE 조직에서는 제품을 구축하는 경험이 흔하지 않음으로 이 부분에서 경험이 풍부한 멤버를 포함하여 팀을 구성해야한다.
- Launch and iterate: 첫 번째 제품은 비교적 간단하고 달성 가능한 목표, 즉 논란의 여지가 없거나 기존 솔루션이 없는 제품을 목표로 삼아야 한다.
- Don’t lower your standards: SRE 조직 내부에서 사용하는 프러덕트는 기준이 낮아 질 수 있다. 외부 사용자가 사용하는 다른 프러덕트와 같은 기준을 부여해야 한다.

## Conclusions
SRE가 개발 도구에 제공하는 고유한 실무 경험은 오래된 문제에 대한 혁신적인 접근 방식으로 이어질 수 있다. 
SRE가 주도하는 소프트웨어 프로젝트는 대규모 서비스 지원을 위한 지속 가능한 모델을 개발하는 데에도 눈에 띄게 도움이 된다.  

