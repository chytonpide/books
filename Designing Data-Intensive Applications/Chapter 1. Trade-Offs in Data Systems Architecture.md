# Chapter 1. Trade-Offs in Data Systems Architecture

# Operational Versus Analytical Systems

## Characterizing Transaction Processing and Analytics
- the term transaction nevertheless stuck, referring to a group of reads and writes that form a logical unit. (의미상 하나의 단위)
- BI
- OLTP: online transaction processing 
- OLAP: online analytics processing

## Data Warehousing
- A data warehouse, by contrast, is a separate database that analysts can query to their hearts’ content, without affecting OLTP operations
- This process of getting data into the data warehouse is known as extract–transform–load (ETL) and is illustrated in Figure 1-1.
  - ETL: extract–transform–load
- General-purpose systems can handle small data volumes comfortably, but the greater the scale, the more specialized systems tend to become


### From data warehouse to data lake
- RDS 에 적합 하지 않은 경우 벡터 와 같은 형식, 데이터 사이언티스가 분석에 적합한 형태(파일)로 데이터를 보존하는 곳을 data lake 라고 한다.
- data warehouse 에 들어가기 전에 data lake 에 raw 데이터로 저장되기도 한다.
- It’s sometimes called the sushi principle: “raw data is better”

### Beyond the data lake
- 파일이나 RDS 가 아니라, 데이터를 이벤트의 스트림으로 처리하는 시스템은 훨씬 빠르다.
- 분석된 데이터로 모델을 트레이닝 한 후, 추천시스템 등에 사용될 수 있다.

## Systems of Record and Derived Data
- systems of record: source of truth
- derived data systems: Data in a derived system is the result of taking existing data from another system and transforming or processing it in some way.
- You can derive several datasets from a single source, enabling you to look at the data from different points of view.
- 데이터가 어디서 오는건지 명확히 하는것이, 시스템 아키텍쳐에 대한 혼란을 막는다.

# Cloud Versus Self-Hosting
- cloud vs self-hosting 은 궁극적으로 비지니스 우선순위에 관한 문제이다. 조직의 핵심 역량이나 경쟁 우위를 차지하는 업무는 내부에서, 비핵심적이거나 일상적인 업무는 외부업체에 맡겨야 한다.
- More control, greater investment ←→ Less control, lower investment

## Pros and Cons of Cloud Services
- If you already have experience setting up and operating the systems you need, and if your load is quite predictable
- If your dataset is so large that querying it quickly requires significant computing resources, using the cloud can save money, since you can return unused resources to the provider rather than leaving them idle.
### Cloud Native System Architecture
- The term cloud native is used to describe an architecture that is designed to take advantage of cloud services.
#### Layering of cloud services
- cloud native 서비스를 만든다는건 IaaS 에 서비스를 올리는것과는 다르다. 
- As always with abstractions in computing, there is no one right answer to what you should use. As a general rule, higher-level abstractions tend to be more oriented toward particular use cases. 
#### Separation of storage and compute
- compute instances (VMs) 은 디스크를 일시적인 캐시로 생각한다. 왜냐하면 장애가 발생하거나 다른 더 크거나 작은 인스턴스로 대채되면 로컬 디스크에 접근할 수 없게 되기 때문이다. 따라서 클로우드 서비스는 인스터스와 분리된 가상의 디스크 저장공간을 사용한다. ex) Amazon EBS

## Operations in the Cloud Era
운영업무가 변화 해 왔다.

# Distributed Versus Single-Node Systems
- 분산 시스템을 사용하는 이유는 많다.
※　Node: Each of the processes participating in a distributed system is called a node.

## Problems with Distributed Systems
- 네트워크 통신은 기본적으로 느리다. 한대에서 처리하는게 100개 노드를 가지는것보다 빠른 경우도 있다.
- 트러블 슈팅도 어렵다. 트레이싱 툴을 써야한다.
- 데이터의 정합성을 유지하는것도 어렵다. 어플리케이션 레벨에서 관리해야 한다.

## Microservices and Serverless
- Microservices are primarily a technical solution to a people problem: allowing different teams to make progress independently without having to coordinate with each other.
- Serverless or function as a service (FaaS) Serverless, is another approach to deploying services, in which the management of the infrastructure is outsourced to a cloud vendor [32]. When using VMs, you have to explicitly choose when to start up or shut down an instance; in contrast, with the serverless model, the cloud provider automatically allocates and frees hardware resources as needed, based on the incoming requests to your service.
  - 사용량 단위로 과금 한다는 의미로 Serverless 라는 용어가 많이 사용된다.
- 클라이언트와 서버로 구분하고, 클라이언트가 서버에 요청을 보내도록 하는 것을 서비스 지향 아키텍처(SOA) 라고 하고 이는 MSA 로 발전했다.

## Cloud Computing Versus Supercomputing

# Data Systems, Law and Society

# Summary
OLTP, OLAP 에 대해 배우고, 각각에서 나타나는 데이터의 양상, 엑세스 패턴, 사용자의 양상에 대해서 배웠다.
운영 시스템에서 ETL 을 통해서 데이터를 공급받는 데이터 웨어하우스와 데이터 레이크에 대해서 배웠다.
클라우드 와 셀프호스팅의 장단점에 대해서 배웠다.
분산 시스템과, 싱글노드 시스템의 특징들에 대해서 배웠다. 





