
- SLO 가 중요하다. 목적 없이는 평가도 관리도 할 수 없다. 이것으로 인센티브가 설계된다.
- 리스크를 수용해야한다.
- 프로젝트와 운영업무는 반반이어야 한다. 이를 통해 SREs 를 유치할 수 있고 SREs 를 통해 확장성의 레버리지를 달성 할 수 있다.
- 구글은 복잡한 인프라 구성을 가지고 있다. 서비스들은 기본적으로 복수개의 인스턴스가 있으며 RPC를 통해 통신한다. 
- 시스템의 규모와 인력이 상관관계를 가지면 안된다. 복잡도가 증가할 때만 인력이 증가해야 한다.
- 공통되는 문제(중복되는 엔지니어링 노력을)를 식별해 내고 이를 해결하기 위한 추상화된 소프트웨어를 제공하는 것으로 확장성을 달성해야 한다. (generic solutions, reproducible!)