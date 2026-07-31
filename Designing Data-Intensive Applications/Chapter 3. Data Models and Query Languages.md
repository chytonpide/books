# Chapter 3. Data Models and Query Languages
- Data models are perhaps the most important part of developing software, because of the profound effect they have not only on how the software is written, but also on how we think about the problem that we are solving.
- Most applications are built by layering one data model on top of another. For each layer, the key question is how it is represented in terms of the next-lower layer.
- In a complex application there may be more intermediary levels, such as APIs built upon APIs, but the basic idea is still the same: each layer hides the complexity of the layers below it by providing a clean data model. 

- Declarative Query Language: 우리가 사용하는 SQL 은, 사용자가 원하는 데이터의 패턴 — 결과가 충족해야 할 조건과 데이터가 어떻게 변환되어야 하는지을 명시하지만, 그 목표를 달성하는 구체적인 방법은 명시하지 않는다.
  - 이 선언적 언어는 쿼리 엔진의 구현 디테일을 숨긴다. 쿼리를 수정할 필요 없이 데이터베이스 시스템이 성능을 개선할 수 있게 해준다.
  - 선언형 : 원하는 결과를 설명하는 형태, 의미가 보존 되려면!?

---

# Relational Versus Document Models
- The relational model was originally a theoretical proposal, and many people at the time doubted whether it could be implemented efficiently. However, by the mid-1980s, relational database management systems (RDBMSs) and SQL had become the tools of choice for most people who needed to store and query data with some kind of regular structure.
  - Over the years, there have been many competing approaches to data storage and querying. In the 1970s and early 1980s, the network model and the hierarchical model were the main alternatives, but the relational model came to dominate them.
- NoSQL refers not to a single technology but a loose set of ideas around new data models, schema flexibility, scalability, and a move toward open source licensing models.
  - NoSQL and NewSQL ideas have been very influential in the design of data systems, but as the principles have become widely adopted, use of those terms has faded.
  - One lasting effect of the NoSQL movement is the popularity of the document model, which usually represents data as JSON. This model was originally popularized by specialized document databases such as MongoDB and Couchbase, although most relational databases have now also added JSON support.
    Compared to relational tables, which are often seen as having a rigid and inflexible schema, JSON documents are thought to be more flexible.

## The Object-Relational Mismatch
- Much application development today is done in object-oriented programming languages, which leads to a common criticism of the SQL data model: if data is stored in relational tables, an awkward translation layer is required between the objects in the application code and the database model of tables, rows, and columns. 
  The disconnect between the models is sometimes called an impedance mismatch.

### Object-relational mapping
- ORM 프레임워크는 장 단점이 있다. 추상화 누수가 있지만, 단순하고 반복적인 케이스에 잘 맞고, 보일러 플레이트 코드를 줄여준다.

### The document data model for one-to-many relationships
- Some developers feel that the JSON model reduces the impedance mismatch between the application code and the storage layer.
  - schema flexibility
- JSON representation, all the relevant information is in one place, making the query both faster and simpler.
- JSON representation 은, one-to-many relationships 을 명확한 트리 구조로 표현한다. 
- document data model 은 one-to-few 에는 적합할 수 있지만, post 와 comment 같이 many 가 엄청 큰 수가 되면, 하나의 문서에 너무 많은 데이터가 내장됨으로 이때는 관계형이 적합할 수 있다.

## Normalization, Denormalization, and Joins
- Whether you store an ID or a text string is a question of normalization.
  - When you use an ID, your data is more normalized: the information that is meaningful to humans (such as the text Washington, DC) is stored in only one place, and everything that refers to it uses an ID (which has meaning only within the database).
  - When you store the text directly, you are duplicating the human-meaningful information in every record that uses it; this representation is denormalized.
- The advantage of using an ID is that because it has no meaning to humans, it never needs to change
- The downside of a normalized representation is that every time you want to display a record containing an ID, you have to do an additional lookup to resolve the ID into something human-readable.
- Document databases can store both normalized and denormalized data, but they are often associated with denormalization

### Trade-offs of normalization
- As a general principle, normalized data is usually faster to write (since there is only one copy) but slower to query (since it requires joins); denormalized data is usually faster to read (fewer joins) but more expensive to write (more copies to update, more disk space used).
- Normalization tends to be better for OLTP systems, where both reads and updates need to be fast; analytical systems often fare better with denormalized data, since they perform updates in bulk and the performance of read-only queries is the dominant concern.
- In systems of small to moderate scale, a normalized data model is often best because you don’t have to worry about keeping multiple copies of the data consistent with one another, and the cost of performing joins is acceptable. However, in very large-scale systems, the cost of joins can become problematic.

## Many-to-One and Many-to-Many Relationships
- 관계형 모델에서 many-to-many 관계는 associative table 로 표현된다. 자기충족적인 json document 로는 many-to-many 관계를 표현하기 힘드며, 정규화된 표현방식(id 로 다른 문서에 대한 참조를 표현)이 적합하다. 
- Many-to-many 관계에서는 종종 양방향 쿼리가 필요하다. 각 테이블에 서로의 id 를 저장하면 이를 달성 할 수 있으나. 이는 비정규화된 표현방식이다. 한곳에 관계를 저장하는 정규화된 표현방식을 사용하면서 인덱스를 통해서 양방향 쿼리를 효과적으로 지원할 수 있다.

## Stars and Snowflakes: Schemas for Analytics
- Data warehouses are usually relational, and there are a few widely used conventions for the structure of tables in a data warehouse, including a star schema, a snowflake schema, dimensional modeling, and one big table (OBT). 
  These structures are optimized for the needs of business analysts. ETL processes translate data from operational systems into the selected schema.
  - star schema: the connections to these tables (fact and dimension) are like the rays of a star.
    - fact table: Each row of the fact table represents an event that occurred at a particular time. (references to dimension tables)
    - dimension tables: the dimensions represent the who, what, where, when, how, and why of the event.
  - snowflake schema: dimensions are further broken into subdimensions.
  - one big table: 비정규화를 더 진행시켜서 dimension table 를 생략하고 dimension 에 포함된 정보를 fact table 의 열로 통합한다.
- In the context of analytics, such denormalization is unproblematic, since the data typically represents a log of historical data that is not going to change.

## When to Use Which Model
- The main arguments in favor of the document data model are schema flexibility, better performance due to locality, and that for some applications it is closer to the object model used by the application. 
  The relational model counters by providing better support for joins and many-to-one and many-to-many relationships.
- 사용자가 임의로 정한 순서를 저장하는데에는 document data model 이 적합하다. Relational DB 에서는 순서를 지정하기 위한 별도의 필드가 있어야 한다.
```json
{
  "todoListId": 1,
  "items": ["A", "B", "D", "C"]
}
```

### Schema flexibility in the document model
- Document databases are sometimes called schemaless, but that’s misleading as the code that reads the data usually assumes some kind of structure—that is, 
  there is an implicit schema, but it is not enforced by the database. A more accurate term is schema-on-read.
- Schema-on-read is similar to dynamic (runtime) type checking in programming languages, whereas schema-on-write is similar to static (compile-time) type checking.
- Schema-on-read 데이터 베이스에서는 특정 필드가 변경되었을 때 어플리케이션 코드로 이것들을 처리한다. Schema-on-write 데이터 베이스에서는 마이그레이션을 통해서 데이터를 변경한다.
  - Schema-on-read: 예전에 저장된 데이터들을 다루는 코드가 필요하다.
  - Schema-on-write: 마이그레이션에서 `UPDATE` 를 실행하는건 비용이 많이 들고, 대규모 데이터베이스에서의 마이그레이션은 운영적인 도전이 된다.
    - column 을 먼저 추가하고, 읽기가 일어났을 때 값을 채우는 점진적인 마이그레이션을 수행할 수도 있다.
- The schema-on-read approach is advantageous if the items in the collection don’t all have the same structure (i.e., the data is heterogeneous);
  In situations like these, a schema may hurt more than it helps, and schemaless documents can be a much more natural data model.

### Data locality for reads and writes
- A document is usually stored as a single continuous string, encoded as JSON, XML, or a binary variant thereof (such as MongoDB’s BSON). 
  If your application often needs to access the entire document (e.g., to render it on a web page), this storage locality has a performance advantage. 
  If data is split across multiple tables, multiple index lookups are required to retrieve it all, which may require more disk seeks and take more time.
- 이러한 이점은 문서의 상당부분을 동시에 필요로 할때만 적용된다. 대용량 문서의 일부에만 접근해야 하는경우에는 전체를 다 불러오는건 자원 낭비이다. 업데이트를 할때도 문서 전체를 재작성해야함으로 자원낭비이다.
  이러한 이유로 document model 에서는 문서의 크기를 작게 유지하는게 낫다.
- relational model 에서도 Data locality 를 위해서 연관된 데이터를 가까지 저장하는 기능을 지원하는 DB 들이 있다. (e.g., Google’s Bigtable) 

### Query language for documents
- Most relational databases are queried using SQL, but document databases are more varied. 
  Some allow only key-value access by primary key, while others also offer secondary indexes to query for values inside documents, and some provide rich query languages.
- PostgreSQL query
```
SELECT date_trunc('month', observation_timestamp) AS observation_month, 1
       sum(num_animals) AS total_animals
FROM observations
WHERE family = 'Sharks'
GROUP BY observation_month;
```
- MongoDB’s aggregation pipeline
```
db.observations.aggregate([
    { $match: { family: "Sharks" } },
    { $group: {
        _id: {
            year:  { $year:  "$observationTimestamp" },
            month: { $month: "$observationTimestamp" }
        },
        totalAnimals: { $sum: "$numAnimals" }
    } }
]);
```

### Convergence of document and relational databases (문서 데이터베이스와 관계형 데이터 베이스의 수렴)
- Document databases and relational databases started out as very different approaches to data management, but they have grown more similar over time
- This convergence of the models is good news for application developers, because the relational model and the document model work best when you can combine both in the same database. 
  Many document databases need relational-style references to other documents, and many relational databases have sections where schema flexibility is beneficial. 
  Relational–document hybrids are a powerful combination.

※ No schema vs schema. 읽기만 하고 변경하지 않는 경우라면 document model 이 좋을지 모르겠다.

---

# Graph-Like Data Models
- But what if many-to-many relationships are very common in your data? The relational model can handle simple cases of many-to-many relationships, but as the connections within your data become more complex, it becomes more natural to start modeling that data as a graph.
  - vertices, edges

## Property Graphs
※ Representing a property graph with a relational schema
```
CREATE TABLE vertices (
    vertex_id   integer PRIMARY KEY,
    label       text,
    properties  jsonb
);

CREATE TABLE edges (
    edge_id     integer PRIMARY KEY,
    tail_vertex integer REFERENCES vertices (vertex_id),
    head_vertex integer REFERENCES vertices (vertex_id),
    label       text,
    properties  jsonb
);

CREATE INDEX edges_tails ON edges (tail_vertex);
CREATE INDEX edges_heads ON edges (head_vertex);
```
- The edges table is like the many-to-many associative, or join, table we saw in “Many-to-One and Many-to-Many Relationships”, generalized to allow many types of relationship to be stored in the same table.
- 그래프 모델의 한계 중 하나는 edge가 기본적으로 두 vertex 사이의 관계만 표현할 수 있다는 점이다. 예를 들어 3개 이상의 엔티티가 참여하는 관계를 그래프 모델로 표현하려면, 관계를 표현하기 위한 vertex 가 추가로 필요하다. (relationship을 entity로 승격)
  이상의 엔티티가 참여하는 관계를 각각의 edge 로 표현하면 의미가 깨질 수 있다.
- Graphs are good for evolvability: as you add features to your application, a graph can easily be extended to accommodate changes in the application’s data structures.
- 유연한 데이터 모델링이 가능하다. 어떤 vertex 라도 다른 vertex 와 연결되는 edge 를 가질 수 있고, 각각의 vertex 와 edge 는 다른 레이블을 가질 수 있음으로 단일 그래프에 여러 정보를 저장할 수 있다.     

## The Cypher Query Language
- `Cypher` is a query language for property graphs.
※ A Cypher query to find people who emigrated from the US to Europe
```
MATCH
  (person) -[:BORN_IN]->  () -[:WITHIN*0..]-> (:Location {name:'United States'}),
  (person) -[:LIVES_IN]-> () -[:WITHIN*0..]-> (:Location {name:'Europe'})
RETURN person.name
```
※ graph model 에서는 관계를 표현할 수 있다. 

## Graph Queries in SQL
- SQL 을 통해서도 그래프 모델에 질의 하는것은 가능하다.
- The fact that a 4-line Cypher query requires 31 lines in SQL shows how much of a difference the right choice of data model and query language can make.
- GQL : ISO standard, SQL 도 ISO 표준이다.

## Triple Stores and SPARQL
- The triple store model is mostly equivalent to the property graph model, using different words to describe the same ideas.
- In a triple store, all information is stored in the form of very simple three-part statements: (subject, predicate, object). For example, in the triple (Jim, likes, bananas), Jim is the subject, likes is the predicate (verb), and bananas is the object.
- object 가 vertex 가 아닌 값일 경우, predicate 는 edge 가 아니라 property 를 의미한다. 
※　A subset of the data in Figure 3-6, represented as Turtle triples
```
@prefix : <urn:example:>.
_:lucy     a       :Person.
_:lucy     :name   "Lucy".
_:lucy     :bornIn _:idaho.
_:idaho    a       :Location.
_:idaho    :name   "Idaho".
_:idaho    :type   "state".
_:idaho    :within _:usa.
_:usa      a       :Location.
_:usa      :name   "United States".
_:usa      :type   "country".
_:usa      :within _:namerica.
_:namerica a       :Location.
_:namerica :name   "North America".
_:namerica :type   "continent".
```
- RDF 데이터 모델에서는 predicate URI 로 표현한다.
  - <http://my-company.com/namespace#within>
  - http: 는 일종의 프리픽스다. 도메인으로 이름 충돌을 피할 수 있긴 때문에 사용한다.
    - 웹으로 접근가능하지 않아도 문제없다. 
- SPARQL is a query language for triple stores using the RDF data model
※ The same query as Example 3-5, expressed in SPARQL
```
PREFIX : <urn:example:>

SELECT ?personName WHERE {
  ?person :name ?personName.
  ?person :bornIn  / :within* / :name "United States".
  ?person :livesIn / :within* / :name "Europe".
}
```

## Datalog: Recursive Relational Queries (재귀 관계형 쿼리)
- Cypher and SPARQL jump in right away with SELECT, but Datalog takes a small step at a time. We define rules that derive new virtual tables from the underlying facts. 
  These derived tables are like (virtual) SQL views: they are not stored in the database, but you can query them in the same way as a table containing stored facts.
- The Datalog approach requires a different kind of thinking compared to the other query languages discussed in this chapter. 
  It allows complex queries to be built up rule by rule, with one rule referring to other rules, similarly to the way that you break code into functions that call each other.
- Datalog 는 선언적이지는 않지만 규칙들로 잘게 쪼개서 정의하고, 그 규칙들을 조합·재사용 할 수 있다. 따라서 복잡한 쿼리에 더 적합하다.

## GraphQL
- GraphQL interfaces allow developers to rapidly change queries in client code without changing server-side APIs.
- Spring for GraphQL 같은 기술로 백엔드를 적절히 구현해줘야 GraphQL 의 메리트가 유효하다.

---

# Event Sourcing and CQRS
- The idea of using events as the source of truth and expressing every state change as an event is known as event sourcing.
- The principle of maintaining separate read-optimized representations and deriving them from the write-optimized representation is called command query responsibility segregation (CQRS).
- star schema fact table 과 event sourcing 모델은 유사하지만, fact table 은 모든 컬럼 집합을 가지고 있지만, event sourcing 은 각자 다른 타입과 프로퍼티를 가질 수 있다. fact table 은 순서가 정해지지 않은 집합이지만 event sourcing 에서 이벤트의 순서는 매우 중요하다.
- Event Sourcing and CQRS 의 이점
  - For the people developing the system, events better communicate the intent of why something happened.
  - You can have multiple materialized views that are optimized for the particular queries that your application requires. 
    - You can even keep a view only in memory and avoid persisting it, as long as it’s OK to recompute the view from the event log whenever the service restarts.
  - The event log can also serve as an audit log of what has occurred in the system, which is valuable in regulated industries that require such auditability.
  - Event logs can typically handle higher write throughput than databases because of their sequential access patterns.
- Event Sourcing and CQRS 의 단점
  - 이벤트 처리를 결정론적으로 만들기 위해서, 처리 과정에서 외부정보의 의존을 피해서 이벤트에 정보를 포함시키거나 타임스템프 등으로 의존하는 정보를 결정론적으로 만들어야 한다.
  - 이벤트가 불변이어야 한다는 요구사항은, 이벤트에 개인정보가 포함되었을 때 문제를 야기한다. 이 시스템에서 개인정보를 삭제하는건 어렵다.
  - 뷰를 만들기 위해서, 이벤트를 재 처리할 때, 사이드 이펙트에 주의해야 한다.
  - 분산 시스템에서 이벤트의 순서를 보장하는 것은 달성하기 쉽지 않다.

# DataFrames, Matrics, and Arrays
- Instead of using a declarative query language such as SQL, a DataFrame is generally manipulated through a series of commands that modify its structure and content. 
  This matches the typical workflow of data scientists, who incrementally “wrangle” the data into a form that allows them to find answers to the questions they are asking.
- DataFrames are flexible enough to allow data to be gradually evolved from a relational form into a matrix representation, while giving the data scientist control over the representation that is most suitable for achieving the goals of the data analysis or model training process.

---

# Summary
여러 종류의 데이터 모델과 그에 적합한 쿼리 언어에 대해서 배운다.
정규화가 무엇인지에 대해 배운다. 정규화는 인간에게 의미있는 정보를 한곳에 저장하고, 다른 곳에서는 그 정보를 id 로 참조하게 하는 것이다.
id 는 사람에게는 의미가 없는 정보여서 불변이기 때문에 정보에 대한 참조 또한 불변이고, 한 곳에서 정보를 수정할 수 있다. 
먼저 관계형 모델에 대해서 배운다. 관계형 모델은 정규화에 이점이 있으나, 데이터들이 여기자기 흩어져 있고(낮은 로컬리티) 어플리케이션에서 사용하는 객체로 매핑을 하기 위한 별도의 노력이 필요한다.
관계형 모델에서는 SQL 이라는 쿼리 랭귀지를 사용한다.
data warehousing 과 business analytics 에서 snowflake schema 라는 스키마를 사용하기도 한다.

문서 모델은 관계형 모델보다는 어플리케이션에서 사용되는 객체에 대응하지만, 
항상 문서 전체를 다루기 때문에 문서의 크기가 커지면 변경 오퍼레이션에 약점이 있음으로 관계가 적은 데이터에 적합하다.
문서형 모델에서 사용하는 더 표현력이 좋은 쿼리 랭귀지를 사용할 수 있다.
RDS 는 관계형 데이터모델과 문서형 데이터모델을 모두 지원하는 방향으로 진화하고 있다. 

그래프 모델은 오브젝트들간 M:N 관계가 주가 되고, 관계중심 적이고 유연한 구조가 필요할 때 사용한다.
그래프 모델에서는 오브젝트와 그 관계를 vertex 와 edge 로 표현한다. 특이한 점은 관계에도 이름을 붙일 수 있어서 관계 표현에 더 표현력이 좋다.
그레프 모델의 언어는 재귀적인 탐색에 적합하다. 그래프 모델에서 SQL 을 사용할 수는 있느나 장황하고 표현력이 떨어진다.

그래프 모델과 쿼리 랭귀지에도 여러 파생형이 있다. Property Graphs , Cypher, SPAQL 과 같은 언어가 있으며,
SPAQL 에서는 데이터를 subject predicate object 로 표현하는 triple store model 에서 사용된다.
Datalog 라는 언어는 선언적이지는 않지만 규칙들로 잘게 쪼개서 정의하고, 그 규칙들을 조합·재사용 할 수 있다. 따라서 복잡한 쿼리에 더 적합하다.
GraphQL 은 일종의 프레임워크로 개발자가 서버사이드의 API 변경 없이 클라이언트 코드의 쿼리를 빠르게 변경할 수 있도록 한다.

이벤트 모델은 사건들을 표현한 것으로 시스템에 어떤 일이 일어났는지 추론하기 쉽니다. 이벤트를 처리해서 여러 뷰에서 필요한 파생 데이터를 만드는데 이러한 아키텍쳐를 CQRS 라고 한다.
이벤트는 불변이고 계속해서 덧붙일 수 있다. 따라서 기존 이벤트들을 재 처리해서 필요에 따라 파생데이터를 다시 만들 수 있는게 이 모델의 장점이다.
이벤트가 불변이라는 제약사항을 가지고 있기 때문에 개인정보와 같은 데이터를 포함하고 있을때 이것을 삭제 처리하는게 어렵다.

DataFrame 이를 다루은 쿼리언어는 단계적인 명령으로 데이터를 조작할 수 있어서 데이터 분석에 유리하다.
관계형 데이터를 형렬과 같은 형태로 일반화 할 수 있고, 이 데이터를 머신 러닝등에 활용할 수 있다.  

스키마는 데이터 구조에 대한 제약 사항으로 관계형 모델외에는 스키마가 없다고 볼 수도 있다.
하지만 데이터를 사용하는 어플리케이션은 특정한 데이터 형태를 상정하고 있음으로, 단지 스키마가 명시적(쓰기 시 강제 적용)인지, 아니면 암시적(읽기 시 가정)인지의 문제이다.

