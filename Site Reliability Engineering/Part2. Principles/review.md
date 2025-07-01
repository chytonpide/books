# 원리 (Principles)
이 섹션에서는 SRE 팀이 일반적으로 어떻게 일하는지를 뒷받침하는 원칙들—SRE 운영의 일반적인 영역에 영향을 미치는 패턴, 행동, 그리고 관심사들을—살펴본다.

## 리스크 포용하기 (Embracing Risk)
SRE 는 넓은 시야에서 보면 에러 버짓이라는 수단을 사용해서 위험을 평가하고 수용하는 엔지니어링 방식이다.

### Managing Risk
리스크 관리에 관하여, 서비스의 신뢰성을 관리한다는 것은 리스크를 관리한다는 것이다. 
100% 가용성은 달성 불가능하고 유저가 원하지도, 알아 챌 수도 없다. (수치가 커질수록 달성을 위한 비용은 기하급수적으로 증가한다.)
따라서 서비스를 충분히 신뢰성 있게 만들되, 그 이상으로 만들지 않도록 한다.

### Measuring Service Risk
구글에서는 시스템의 특성을 대표할 수 있는 객관적인 지표를 식별해 내어서 목표를 설정함으로써 성능을 평가하고 변화를 추적한다.
하지만 리스크의 경우 여러 요인을 하나의 지표로 다루기는 힘들다.
다양한 시스템유형에 대해서 실용적이고 일관되게 다루기 위해서 구글은 예기치 않은 다운타임에 unplanned downtime 에 초점을 맞춘다.
time-based availability 라는 측정장법이 있지만 구글에서는 리퀘스트의 성공비율을 나타내는 aggregate availability 를 사용한다.  
```
availablity = successful requests / total requests
```
어플리케이션에서 각 리퀘스트의 중요도는 다르지만 요청의 성공률로 계산된 가용성은 사용자 관점에서 보는 다운타임을 합리적으로 근사하는 지표가 될 수 있다.
이 지표는 사용자에게 직접 서비스를 제공하지 않는 시스템에도 적용할 수 있고 이때, 리퀘스트의 성공과 실패 명확하게 정의되어 있다.
구글에서는 분기별로 서비스의 가용성 목표를 설정하고 실제 성과를 매주 또는 매일 추적한다.  

### Risk Tolerance of Services
These product managers are charged with understanding the users and the business, and for shaping the product for success in the marketplace.

The target level of availability for a given Google service usually depends on the function it provides and how the service is positioned in the marketplace.

Types of failures 계속..





에러 버짓은 SRE 와 제품 개발팀의 동인을 일치 시키고 공동의 소유권을 강조한다.
에러 버짓은 구체적인 수치로써 여러 팀에 걸쳐서 의사소통을 원할하게 하며 출시 속도를 결정하는데 도움을 준다.




## 서비스 수준 목표 (Service Level Objectives)
SRE 는 확고한 SLO 를 가진다. 이는 indicators, objectives, agreements 는 분리된다.

## 반복작업 제거하기 (Eliminating Toil)
SRE 의 가장 중요한 일중 하나는 귀찮은일을 제거하는 것 이다. 
SRE 는 toil 을 서비스가 성장할때 리니어하게 증가하는, 오래 지속되는 가치를 만들지 않는 반복적인 운영업무를 의미한다.  

## 분산시스템 감시 (Monitoring Distributed Systems)
모니터를 하지 않으면 시스템에 무슨일이 일어나는지 알 수 없다.  

## 구글에서의 자동화의 진화 (The Evolution of Automation at Google)

## 릴리즈 엔지니어링 (Release Engineering)

## 단순함 (Simplicity)
잘 작동하는 복잡한 시스템은 반드시 잘 작동하는 단순한 시스템에서 진화해온 것이다.





