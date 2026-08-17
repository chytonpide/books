# Chapter 5. Encoding and Evolution
- whenever you want to send some data to another process with which you don’t share memory—for example, 
  when you want to send data over the network or write it to a file—you need to encode it as a sequence of bytes.
- 시스템이 진화하면서 스키마도 변화한다. 스키마 변경이 수반하는 문제는 두 방향으로 나타나고 이를 다루는 각각의 방법을 후방 호환성과 전방 호환성 이라고 한다. 
  데이터를 인코딩하는 포맷들에 대해서 배우고, 이 포맷들이 스키마 변경을 다루고, 스키마 변경에 대해서 시스템에 호환성을 지원하는지 배운다. 또 이 포맷들이 데이터 스토리지와 통신에 어떻게 사용되는지 배운다.

※ 후방 호환 = 새 코드가 옛 데이터를 읽을 수 있다 (뒤를 돌아본다)
※ 전방 호환 = 옛 코드가 새 데이터를 읽을 수 있다 (앞을 내다본다)

# 목차

- Formats for Encoding Data
  - Language-Specific Formats
  - JSON, XML and Binary Variants
  - Protocol Buffers
  - Avro
  - The Merits of Schemas
- Modes of Dataflow
  - Dataflow Through Databases
  - Dataflow Through Services: REST and RPC
  - Durable Execution and Workflows
  - Event-Driven Architectures

# Formats for Encoding data
- Programs usually work with data in (at least) two representations:
  - In memory, data is kept in objects, structs, lists, arrays, hash tables, trees, and so on. These data structures are optimized for efficient access and manipulation by the CPU (typically using pointers).
  - When you want to write data to a file or send it over the network, you have to encode it as some kind of self-contained sequence of bytes (e.g., a JSON document). Since a pointer wouldn’t make sense to any other process, this sequence-of-bytes representation often looks quite different from the data structures that are normally used in memory.
- Thus, we need some kind of translation between the two representations. The translation from the in-memory representation to a byte sequence is called encoding (also known as serialization or marshaling), and the reverse is called decoding (aka parsing, deserialization, or unmarshaling).

## Language-Specific Formats
- java.io.Serializable 라는 Language-Specific format 이 있지만, 언어에 종속적이고, 전후방 호환성을 지원하지 않는다. (꽤 옛날에 설계되었기 때문에)
  따라서 언어에 종속적인 포맷을 사용하는건 아주 일시적인 사용 목적이 아니면 적합하지 않다.

## JSON, XML, and Binary Variants
- JSON, XML, and CSV are textual formats, and thus they are somewhat human-readable.
  - json 단점 
    - 부동소수점을 표현할 수 없다.
    - 바이너리 스트링을 지원하지 않아서 base64 로 인코딩해야한다. 즉 json에 들어가는 모든 문자는 unicode 여야한다. unicode 로 맵핑 될 수 없는 바이트는 인코딩, 디코딩이 안된다.  

### JSON Schema
- JSON Schema has become widely adopted as a way to model data whenever it’s exchanged between systems or written to storage.
- schema 를 정의 할수 있지만 전후방 호환성을 지원하면서 schema 를 발전시켜 나가는건 어렵다.

### Binary encodings
- JSON 을 더 압축하는 MessagePack 같은 인코딩 있지만, textual format(텍스트 형식) 이 아니다.

### Protocol Buffers
- Schema 를 간단하게 정의할 수 있다.
- 인코딩하는 모든 데이터에 대해서 스키마를 정의해야하는 제약사항으로 인코딩 할때 필드명 자체를 인코딩 하지 않고, 필드 테그를 인코딩 하는걸로 압축률을 높일 수 있다.
#### Field tags and schema evolution
- Schema 정의에서 필드는 테그 넘버를 가짐으로 나중에 추가된 필드를 쉽게 식별할 수 있다.
  - 필드추가
    - forward compatibility: 예전 코드는 새 데이터의 새 필드를 무시한다.
    - backword compatibility: 새 코드는 옛 데이터의 필드를 기본값으로 채운다.
  - 필드삭제
    - forward compatibility: 옛 코드가 새 데이터를 읽음 — 필드 부재를 기본값으로 처리.
    - backword compatibility: 새 코드가 옛 데이터를 읽음 — 남아 있는 옛 필드를 무시. 

### Avro
- Protocol Buffer 처럼 Schema 를 정의하는 별도의 언어가 있다. 인코딩된 데이터에는 필드의 이름과 타입이 포함되지 않고 특정을 위한 테그도 없음으로 송수신 측의 스키마가 정확히 일치해야 디코딩이 가능하다.
#### The writer's schema and the reader's schema
- Protocol Buffer 에서는 writer 와 reader 의 Schema 의 버전이 다를 수 있지만, Avro 에서는 writer 와 reader 의 Schema 모두가 필요하다.  
#### Schema evolution rules
- compatibility 를 위해선 default 값이 필요하다. avro 에서는 null 은 필드의 타입으로 지정되었을 때만 사용가능하다. 
#### But what is the writer's schema
- 상황에 따라 다른 방식으로 writer 의 schema 가 reader 에게 공유 될 수 있다.
  - Large file with lots of records: avro 의 일반적인 사용은 수많은 수백만 레코드가 포함된 파일인데 이 파일의 시작 부분에 스키마를 둔다.
  - Database with individually written records: 여러 버전의 레코드가 DB 에 저장됨으로 인코딩되는 레코드에 schema 버전을 기록한다. 
  - Sending records over a network connection: 연결을 설정할 때 schema 의 버전을 협상한다.
- 어느 경우든 데이터베이스를 통해서 스키마의 버전관리를 하는것은 유용하다.
#### Dynamically generated schemas
- avro 는 동적으로 Schema 를 생성한다. 예를들어 관계형 DB 의 데이터를 avro 의 파일로 덤프한다고 하면, 테이블 에 기반해서 스키마를 작성한다.
  - writer 와 reader 의 스키마가 나누어져 있기 때문에, 파일에 쓸때 write 스키마는 그대로 고정되고, reader 는 규칙에 따라서 여전히 이전 버전의 파일을 읽을 수 있다. 
- Protocol Buffer 의 경우, 테그번호 매핑을 해야한다. 즉 마이그레이션을 관리해야 한다. 

### The Merits of Schemas
- Schema evolution 은 schema-on-read JSON databases 제공하는 것과 같은 수준의 유연성을 제공하면서, 올바른 데이터를 보장하고 툴링도 제공한다.
  단 운영을 단순하게 유지하기 위해서는 동시에 사용되는 스키마의 수를 최소한으로 제한하는 것이 좋다.

---

# Mode of Dataflow
- Compatibility is a relationship between one process that encodes the data and another process that decodes it.

## Dataflow Through Databases
- 여러 버전의 프로세스가 데이터 베이스에 접근할 수 있음으로 호환성이 필요하다.  
### Different values written at different times
- data outlives code: 예전에 쓰인 데이터와 그 데이터를 다룰 수 없는 새로운 코드가 존재하는 상황. 
- 데이터를 마이그레이션 할 수 있지만 대용량 데이터 셋에서는 비용이 비싸고, 이것은 비동기로 처리된다.
  따라서 저장소가 다양한 과거의 버전으로 인코딩된 레코드가 포함되어 있더라도 schema evolution 을 통해 단일 스키마로 인코딩 된것 처럼 보이게 한다.  
### Archival Storage
- 정기적으로 데이터베이스의 스냅샷을 찍을 수 있다. 이러한 snapshot 을 dump 하면 불변임으로, Avro object container files 과 같은 포맷이 좋다.     

## Dataflow Through Services: REST and RPC
- In some ways, services are similar to databases: they typically allow clients to submit and query data. 
  However, while databases allow arbitrary queries using the query languages we discussed in Chapter 3, 
  services expose an application-specific API that allows only inputs and outputs that are predetermined by the business logic (application code) of the service. 
  This restriction provides a degree of encapsulation: services can impose fine-grained restrictions on what clients can and cannot do.
- 서비스지향 아키텍처의 주요 설계 목표는 각각의 서비스를 독립적으로 진화시키고 배포하는것을 쉽게 하는것이다. 
  따라서 옛, 신 버전의 서버와 옛, 신 버전의 클라이언트가 동시에 동작하는것을 예상하고, 서버와 클라이언트가 사용하는 데이터 인코딩은 서비스 API의 각 버전 간에 호환되어야 한다.

### Web services
- When HTTP is used as the underlying protocol for talking to the service, it is called a web service.
- 많은 웹서비스들이 REST 철학을 따르며 IDL 을 통해 인터페이스를 노출한다. 또 많은 서비스 프레임워크들이 IDL 생성을 지원한다.
  - IDL: interface definition language
    - OpenAPI: IDL 의 한 종류.
  - Service frameworks allow developers to focus on writing the business logic for each API endpoint, while the framework code handles routing, metrics, caching, authentication, and so on

### The problems with remote procedure calls
- 많은 RPC 기술이 있었지만, complexity, compatibility 문제로 널리 퍼지지 못했다. (GRPC 는 성공일까?)
- 지역 호출과 리모트 호출은 근본적으로 다르기 때문에, 원격 서비스의 호출을 로컬 객체와 비슷하게 다루려고 하는 접근은 한계가 있다.

### Load balancers, service discovery, and service meshes
- 다른 서버와 통신하기 위해 엔드 포인트를 클라이언트의 configuration 에 저장하는 방법도 있지만, 서버의 엔드 포인트가 계속 해서 변경 된다면 수동으로 이것을 조정하는 것은 힘들다.
  따라서 높은 가용성을 제공하기 위해서 다음과 같은 load balancing 또는 service discovery 솔루션이 사용된다.
  - hardware load balancing
  - software load balancing
  - DNS
  - service discovery systems
  - service mesh: 클라이언트와 서버 각각이 로드밸런서를 가진다. 복잡하지만 정교한 observability(관측 가능성)을 제공한다.

### Data encoding and evolution for RPC
- 진화용이성을 위해서 RPC 에서도 클라이언트와 서버를 독립적으로 변경하고 배포할 수 있는것이 중요하다. 즉 호환성이 필요하다.
  호환성은 사용하는 인코딩에 따라 특성이 결정된다. 예를들어 gRPC 는 Protocol Buffers 를 사용한다.
- 호환성을 깨는 변경이 필요하게 되면, 결국 API 의 버저닝이 필요하게 된다. 

## Durable Execution and Workflows
- workflow: 여러 서비스 호출로 이루어진 다단계 시퀀스 (예: 결제).
  - 각 단계를 task 또는 activity라고 부른다.
- workflow engine: 워크플로우의 정의와 실행을 관리.
  - 예) Temporal, Dagster, Prefect
- durable execution: service-base architecture 에서는 결제의 여러 단계를 하나의 트랜잭션으로 묶을 수 없다.
  각 단계의 호출 결과를 durable log 에 기록해 두고, 재시도의 경우 기록된 호출은 replay, 이후 지점부터 실제 실행 하는 것. 
- durable execution framework 는 exactly-once semantics 하는 오직 한번만 실행되는 거동을 제공한다.
  - 호출 하는 외부 서비스는 멱등성을 지원해야하고 비 결정론적 코드가 없어야 한다.

## Event-Driven Architecture (sourcing 아님, 통신에 메세지 브로커 사용하는 아키텍쳐)
- RPC 와 비교해서, 메세지 브로커를 통한 통신은 비동기로 다음과 같은 장점을 가진다.
  - 수신자가 가용하지 않을때 버퍼처럼 동작해서 시스템의 신뢰성을 향상시킨다.
  - 시스템 장애가 발생했을 때, 메세지를 재송신한다.
  - 메세지 브로커가 메세지를 중계하기 때문에, service discovery 같은게 필요하지 않다.
  - 같은 메세지를 복수의 수신자에게 전송할 수 있다.
  - 송신자와 수신자를 논리적으로 분리시킨다.

### Message brokers
- 전송 명세가 약속하는 동작의 세부사항은 구현에 따라 다르지만 아래와 같은 용어를 사용한다.
  - queue 에 메세지를 추가한다. consumer 가 메세지를 수신한다. 
  - topic 에 메세지를 추가한다. subscriber 가 메세지를 수신한다.
- 메세지 브로커가 강제하는 인코딩은 없지만, 마찬가지로 호환성은 신경써야 한다. 
  - AsyncAPI: OpenAPI 의 message broker 버전 

### Distributed actor frameworks
- actor model : 하나의 프로세스에서 동시성을 다루기위한 프로그래밍 모델
  - 쓰레드로 다루기 보다는, 각각의 로직을 actor 로 캡슐화 하고 비동기로 메세지를 주고 받으면서 통신한다.
  - actor 는 한번에 하나의 메세지만 처리한다. 
- distributed actor framework 는 매세지 브로커를 통합하고 있고 마찬가지로 호환성을 해결하기 위해 인코딩 방식을 선택해야 할 수 있다.  

---

# Summary
데이터의 포맷 또는 스키마가 변경될 때, 어플리케이션 코드도 함께 변경되어야 하지만, 
서버 클라이언트 아키텍쳐, 롤링 업그레이드 메카니즘을 사용하는 시스템에서는 이것은 불가능 한다.
새로운 코드와 옛 스키마, 오래된 코드와 새 스키마가 공존하는 상황이 발생할 수 있다.
시스템이 계속해서 동작하도록 하기 위해서 다음과 같은 호환성이 필요하고 이것으로 인해 높은 진화용이성을 달성할 수 있다.

- 후방 호환(Backward compatibility): 새 코드가 옛 데이터를 읽을 수 있다 (뒤를 돌아본다) 
- 전방 호환(Forward compatibility): 옛 코드가 새 데이터를 읽을 수 있다 (앞을 내다본다)

프로그램은 대체로 두가지 데이터 표현과 함께 동작한다. 메모리 상의 객체나 행결과 같은 표현과
다름 시스템으로 전송하기 위한 바이트 시퀸스 이다. 이 두 표현사이의 변환이 필요로 하는데 이것을 인코딩과 디코딩 이라고 한다. (마샬링 - 언마샬링) 

데이터 인코딩을 위한 여러가지 형식(Format for data encoding) 에 대해 배운다.
포맷은 데이터를 구조화 하는 방식으로 예를들어서, 어떤 스키마의 타입정보를 첫번째 바이트로 기록한다라는 등의 규칙이다.
인코딩은 데이터를 특정 규칙에 따라 부호화 하는 방법으로, 포맷에 인코딩이 포함될 수 있다.
시스템간에 스키마의 차이가 발생할 수 있음으로 진화용이성을 위해 전후방 호환성을 지원하기 위한 각자의 메카니즘을 가지고 있다.

언어에 특정한 인코딩 방법은, 특정 언어에 종속되어 있기 때문에 한계가 명확하다.

json은 텍스트 형식(textual formats) 이다. json 문서 전체가 유효한 unicode 텍스트 여야 한다는 제약사항이 있고,
문자 인코딩 방식인 utf-8 로 인코딩 되어 있기 때문에, 텍스트 에니터로 열면 그냥 열린다. (format 에 대한 추가적인 정보가 없고, 훨씬 더 통용되는 utf-8 만으로 열 수 있다.)
binary schema–driven formats 은 이와 다르게 포맷을 알아야 하고 디코딩해도 사람이 자연스럽게 읽지 못할 수 있다.

protocol buffers 와 같은 포맷은 필드에 테그넘버를 부여하는 것으로 필드 이름을 페이로드에 포함시키지 않는것으로 좋은 압축효율과 진화용이성을 제공한다.
Protocol Buffers 와 같은 포맷은 스키마 진화 메카니즘을 가지고 있고, 더 나은 진화용이성을 가진다.

데이터흐름의 여러가지 방식(Models of Dataflow)에 대해서 배운다.

데이터가 수백만건일 경우, 마이그레이션은 한번에 일어나지 않는다. 
스키마가 변경될 경우 마이그레이션은 하번에 일어나지 않지만, 클라이언트에게는 단일 스키마로 데이터를 제공할 수 있다.

지역 호출과 리모트 호출은 근본적으로 다르기 때문에, 원격 서비스의 호출을 로컬 객체와 비슷하게 다루려고 하는 RPC 의 접근 방법은 한계가 있다.
REST 는 이와 다르게, 네트워크를 통한 호출을 전제로 한다.

워크플로우는 여러 단계로 이루어진 시퀸스로 각 단계는 서비스데 대한 호출이 될 수 있다. 이 단계는 하나의 트랜잭션으로 묶기 힘들다.
워크플로우의 예로는 결제와 같은 프로세스가 있다. 이러한 워크플로우를 정의와 실행을 관리하는 프레임워크를 워크 플로우 엔진이라고 한다.
워크플로우 엔진은 정지 지점에서 부터의 재시도, 같은 입력에 대해서 같은 결과와 같은 문제를 해결하고 이를 Durable Execution 이라고 한다.

이벤트 소싱 아키텍쳐는 이벤트를 통해서 시스템의 구성 요소들이 비동기 통신을 하는 아키텍처로, 메세지 브로커 사용을 전제로한다.
더 높은 신뢰성, 논리적으로 분리되는 것을 포함해서 여러 이점이 있다.
액터 모델 프로그래밍 방법은 동시성을 다루기위한 것으로 하나의 액터가 각각의 로직과 상태를 가지고 서로 비동기로 통신한다.
같은 프로세스안의 액터간 통신에도 메세지의 전송 실패가 발생할 수 있다는 것을 전제로 한다.

이러한 데이터 흐름의 여러가지 방식에서도 통신간 인코딩이 발생하고, 진화용이성을 위해서 전후방호환성을 지원해야 한다.


