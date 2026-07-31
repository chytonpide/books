# Chapter 4. Storage and Retrieval
- In order to configure a storage engine to perform well on your kind of workload, you need to have a rough idea of what the storage engine is doing under the hood.
- 다음 두가지를 시험해본다.
  - log-structured
  - b-trees

# 목차
- Storage and Indexing for OLTP
  - Log-Structured Storage
  - B-Trees
  - Comparing B-Trees and LSM-Trees
  - Multicolumn and Secondary Indexes
  - Storing Values Within Indexes
  - Keeping Everything in Memory
- Data Storage for Analytics
  - Cloud Data Warehouses
  - Column-Oriented Storage
  - Query Execution: Compilation and Vectorization
  - Materialized Views and Data Cubes
- Multidimensional and Full-Text Indexes
  - Full-Text Search
  - Vector Embeddings


# Storage and Indexing for OLTP
- many databases internally use a log, which is an append-only data file.
  - In this book, log is used in the more general sense: an append-only sequence of records on disk.  It doesn’t have to be human-readable; it might be binary and intended only for internal use by the database system.
    ※ Log: 여기서는 로그를 디스크에 덧붙이기만 가능한 레코드의 차례라는, 일반 적인 용어로 사용한다.
- To efficiently find the value for a particular key in the database, we need a different data structure: an index.
  - The general idea is to structure the data in a particular way (e.g., sorted by a key) that makes it faster to locate the data you want. If you want to search the same data in several ways, you may need several indexes on different parts of the data.
  - An index is an additional structure that is derived from the primary data. 
    - Maintaining additional structures incurs overhead, especially on writes.
    - Any kind of index usually slows down writes, because the index also needs to be updated every time data is written.
- This is an important trade-off in storage systems: well-chosen indexes speed up read queries, but every index consumes additional disk space and slows down writes, sometimes substantially.
  - You can then choose the indexes that give your application the greatest benefit, without introducing more overhead on writes than necessary.

---

## Log-Structured Storage
- hashmap + log-structured 방식의 naive 한 구현
  - 메모리에 hashMap 을 두고 key 와 log-structured file 의 offset 을 엔트리로 저장

### The SSTable file format
- sparse(드문) index + Sorted Strings Table
  - Sorted Strings Table 에는 데이터가 sorting 되어 있음.
  - sparse 는 일부 키 와 offset 를 저장. 데이터가 sorting 되어 있다는 제약을 통해서 빠르게 sparse index 를 탐색한 후 이어서, 실제 데이터 테이블의 특정 범위만 탐색 할 수 있다. 

### Constructing and merging SSTables
- 데이터가 sorting 되어 있어야 한다는 제약을 지켜야 하기 때문에 쓰기 작업의 비용이 비싸다. 이를 append-only log 와 sorted file 의 혼합형태를 가지는, log-structured 로 해결할 수 있다.
  - 쓰기를 할 때, trie 와 같은 메모리 상의 자료구조에 이를 저장한다. 이를 memtable 이라고 한다. 
    - trie 는 일종의 상태 머신으로 정렬된 상태에서 쓰기를 메우 효율적으로 할 수 있다. O(key-length) 
  - memtable 의 크기가 커지면 SSTable 로 디스크에 저장한다. SSTable 을 디스크에 쓰면서 memtable 은 초기화 된다.
  - 읽기를 할 때, memtable 부터 가장 최근의 segment, 그보다 오래된 segment 에서 순차적으로 key 를 찾는다.
  - 계속해서 segment 의 병합과 압축(merging and compaction) 프로세스를 백그라운드에서 수행한다.
- segment 를 머지하는 작업은 머지소트 알고리즘과 유사하다.
  - 각각의 segment 는 이미 정렬되어 있다. 
- 삭제 할때는 tombstone 이라는 레코드를 남기고 머지 할 때, 이것들을 전부 삭제한다.    
- 이러한 sorted file 을 병합과 압축(merging and compaction) 하는 원리를 기반으로 하는 storage engine 을 LSM storage engine 이라고 한다.
- ※ LSM: Log-Structured Merge-tree (자료구조)

### Bloom filters
- 키의 부재를 빠르게 확인하기 위한 필터로, bitwise 오퍼레이션을 활용한다.
  - 키의 각 문자의 해시값을 계산해서 나온 비트행렬의 인덱스에 1을 기록한다. (해시함수는 특정 길이로 바꾸는 함수다!)
  - 키 값을 찾츨 때 같은 과정을 진행한다. 
    - 비트행렬의 같은 인덱스에 1이 있다면 있을 수도 있다. (false positive.)
    - 비트행렬의 같은 인덱스가 하나라도 0 이면 값이 없다. 있다면 반드시 해당 인덱스에 1이 있어야 하기 때문에.

### Compaction strategies

---

## B-Trees
- B-trees break the database into fixed-size blocks or pages and may overwrite a page in place.
  Each page can be identified using a page number, which allows one page to refer to another—similar to a pointer, but on disk instead of in memory.
- This algorithm ensures that the tree remains balanced: a B-tree with n keys always has a depth of O(log n).

### Making B-trees reliable
- 동시에 여러 페이지를 덮어쓸때 crash 발생한다면 부패한 트리를 가질 수 있다. 이를 해결하기 위해서 쓰기를 할 때 먼저 WAL: Write-Ahead Log 에 기록을 남기고, crash 가 발생하면 이를 사용해 데이터베이스를 복구 할 수 있다.

### Using B-tree variants

---

## Comparing B-Trees and LSM-Trees

### Read performance
- B-tree: 각 단계(level)가 비교적 작음으로 일반적으로 빠르고 예측가능한 성능을 가진다.
- LSM: 다른 간략화 단계(stage) 의 SSTable 들을 확인해야 한다. Bloom filters 가 필요한 disk I/O operation 을 줄여줄 수 있다.
- LSM 에서 Range query 에 대해서 비용이 더 들 수 있다.
- LSM 에서 많은 쓰기 작업이 읽기의 지연시간 스파이크를 유발할 수 있다. 이는 데이터를 디스크의 쓰는 작업이 충분히 빠르지 않을때 일어날 수 있다. compaction 이 이루어진 뒤에 memtable 을 다시 디스크에 쓸 수 있음으로.  

### Sequential versus random writes
- sequential writes: 적은 빈도로 큰 쓰기를 하는것. 
  - LSM에서 쓰기는 B-Tree 의 페이지 보다 큰, 새 SSTable 파일을 순서대로 디스크에 쓰는 작업이다. 백그라운드에서 이루어지는 Compaction 에서 "새로운 파일을 순서대로 쓴다."
- random writes: 높은 빈도로 산발적인 쓰기를 하는 것.
  - B-Tree 에서는 디스크에서 키를 "찾아서 덮어 쓴다."
- 따라서 LSM 이 쓰기 산출량이 더 높다. SSD 에서도 적은 빈도로 큰 쓰기를 하는 것이, SSD의 GC를 적게 유발해서 성능이 더 좋다.

### Write amplification
- write amplification: 한 작업에서 디스크에 쓰는 총 바이트 수를, 인덱스가 없는 단순한 append-only log 에 쓰는 총 바이트 수 로 나눈것. (바이트 수가 아니라 I/O 관해서 정의되기도 한다.)
  - b-tree 에서 page 를 나누거나 하면서 실제로 디스크에 쓰기를 하는 작업량이 훨씬 클 수 있다. 
  - LSM 에서 compaction 이 일어나면 상당한 양을 다시 쓰기한다.
- write amplification 이 높을 수록 쓰기 병목이 발생할 가능성이 높다.
- write amplification 은 키와 값의 길이, 덮어쓰기와 삽입의 빈도등에 영향을 받지만 일반적인 작업부하에서 LSM 가 낮은 write amplification 를 가진다.
  - b-tree: 작은 변경도 page 전체 rewrite가 필요할 수 있음
  - LSM: 쓰기 시점에는 append/flush 중심이고, SSTable chunk이 압축되어 있어서, write amplification 낮을 수 있음.   

### Disk space usage
- b-tree: 시간이 지날수록 파편화가 일어난다. 예를 들어 많은 수의 키가 삭제되면 파일 안에는 사용되지 않는 페이지가 포함될 수 있다. 페이지의 위치를 재배치하는 프로세스가 필요할 수 도있다. (postgresql 의 vaccum 프로세스)
- LSM: compaction 프로세스가 파일 자체를 다시 쓰기때문에 파편화는 그다지 문제되지 않는다.
  - 삭제된 레코드 표시하는 tombstone 이 여러 compaction 단계를 거쳐서 전파 될 때까지 삭제된 레코드가 상위 레벨에 남아 있을 수 있다.

---

## Multicolumn and Secondary Indexes
- LSM, b-tree:  정렬된 key-value 구조를 디스크에 유지하는 메커니즘이라고 key 를 통해 값을 빨리 찾을 수 있다.  
- 인덱스: 원본 데이터에서 파생된 부가 구조로, 특정 키로 행(또는 행의 위치)을 빨리 찾기 위해 별도로 유지하는 것.
  - value로 찾고 싶으면 그 value를 키로 바꾼 구조를 하나 만들면, 기존 메카니즘(LSM, B-tree)를 사용할 수 있다.
  - 넓은 의미에서 인덱스는 키로 빨리 찾게 해주는 구조 전반을 의미하고 LSM, b-tree 메카니즘도 key 를 인덱싱 한다고 볼 수 있다.
```
"kyoto" → [3, 17, 42]
"osaka" → [8, 25]
```
  - 키에 row ID를 붙여서 유일하게 만든다:
```
"kyoto:3"  → (row 3 데이터)
"kyoto:17" → (row 17 데이터)
"kyoto:42" → (row 42 데이터)
```

---
## Storing Values Within the Index
- The key in an index is what queries search by. Other data may be stored in the index, in addition to the keys, depending on the type of index:
  - key → 행 자체: 행이 인덱스 안에 통째로 들어있음 (clustered index), MySQL InnoDB 의 PK 가 이러한 형태이다. 
  - key → 행의 위치(포인터): 실제 행은 딴 데(heap) 있고 인덱스는 주소만 가짐, MySQL InnoDB 에서 Secondary Indexes 가 이런 형태이다.

## Keeping Everything in Memory
- 데이터 읽기 쓰기 오퍼레이션에 대해서 메모리에 비하면 디스크는 조금 어색하지만 내구성(durability)과 가격에 이점때문에 디스크에 데이터를 쓴다.
- 가격의 이점이 약화되고, 많은 데이터 세트의 용량이 크기 않기 때문에 in-memory-database 가 개발되었다.
- 기본적으로 재시작 하면 데이터가 날라가지만, append-only log 만 디스크에 쓰고, 그것을 다시 in-memory-database 에 불러오는 걸로 내구성(durability) 문제를 해결할 수 있다.
- 비직관적이게도 in-memory databases 의 성능 이점은, 그것이 디스크를 사용하지 않기 때문은 아니다. 디스크를 사용하더라도 OS 의 캐쉬 메카니즘에 의해 디스크 블록이 메모리에 위치함으로 충분히 빠를 수 있다. 

---

# Data Storage for Analytics
- OLTP, OLAP 둘다 SQL 을 사용하지만 아주 다른 쿼리 패턴을 가지고 있어서 데이터 베이스 벤더들은 각각에 특화된 데이터베이스를 제공하는데 집중하고 있다. 
## Cloud Data Warehouses
- cloud data warehouses 는 cloud service 랑 통합하기 쉽다. idle 한 resource 를 되돌려 줘서 비용을 절약할 수 있고, 유연하게 확장할 수 있는 cloud system 의 이점도 가질 수 있다. 
- 분석을 위한 데이터 저장이 object storage 기반의 data lake 로 이동하면서 오픈 소스 데이터 웨어하우스도 다른 시템과 통합할 수 있는 여러 컴퍼넌트들(query engine, storage format, table format, data catalog)로 분리되기 시작했다.  
## Column-Oriented Storage
- 분석을 위한 data warehouse 에서 star schema 를 사용한다고 할 때, select * 와 같이 모든 column 을 결과로 불러오는 일은 거의없다.
  하지만 쿼리의 실행에서, row-oriented 의 OLTP 데이터 베이스에서는, fact 테이블의 모든 attribute 가 포함된 row 의 리스트를 처리하는 과정이 포함된다.  
- Column-Oriented(columnar) Storage 의 아이디어는 각각의 column 의 값들끼리 모아서 저장하는 것이다. 이렇게 모든 attribute 가 포함된 row 리스트를 처리하는게 아니라 쿼리에서 지정하는 column 의 값들만 처리할 수 있다.
  대부분의 분석 DB 에서 columnar storage 를 사용한다. 

### Column compression
- 각 column 의 값의 시퀸스를 보면 값들이 반복됨으로 압축하기에 좋다.
- bitmap encoding
  - 열의 값들은 행 수에 비해 적다. n 개의 개별 값을 각각 고유한 n 개의 bitmap 으로 만든다.
  - bitmap 에서 행의 값이 비트맵이 표현하는 값과 같을 때 1로 표시한다.(이렇게 하면 각 bitmap 길이는 행의 수와 같다.)
- bitmap 의 길이가 길어지고 연속적인 0을 많이 포함하게 됨으로 추가적으로 run-length encoding 을 추가로 적용할 수 있다.   
- `WHERE product_sk IN (31, 68, 69)` 를 실행할 때 해당하는 bitmap 을 찾고 or 로 전부 합치면, 해당하는 값의 row 를 전부 얻을 수 있다.    
- `WHERE product_sk = 30 AND store_sk = 3` 를 실행할 때 bitmap 을 찾고 and 로 전부 합치면, 해당하는 값의 row 를 전부 얻을 수 있다.
  -  column file, product_sk 와 store_sk 의 각각의 행은 같은 row 를 가르키는 파생 데이터 라는 사실에 기반한 처리방법이다.

### Sort order in column storage
- 본체 데이터(fact table)에서 특정 컬럼을 sort key 로 설정해서 저장하면, 마찬가지로 indexing 메카니즘을 사용할 수 있다.
- 이렇게 정렬를 하면 column file 의 압축률도 올라간다. second sort key 까지 설정하면 더 올라간다. 

### Writing to column-oriented storage
- Column-Oriented Storage 데이터 분석에서 쿼리 실행을 빠르게 해준다.
- data warehouses 로의 쓰기작업은 ETL을 통해 방대한 양의 데이터를 일괄적으로 처리한다. 이때 Column-Oriented 레이아웃으로 데이터를 보존한다면,
  일일히 데이터를 중간에 삽입하는건 비용이 큰 작업이 된다. (모든 column file 을 수정해야하니)
  이것들을 큰 단위로 일괄 처리하면 비용을 줄일 수 있고, 
  log-structured 접근 방식에서 메모리에서 row-oriented 방식으로 저장하다가 충분한 데이터가 쌓이면 이것들을 백그라운드에서 column-oriented 레이아웃 저장되도록 일괄 처리한다. 
※ 레이아웃 - column-oriented : 배치 규약. 어떤 접근 패턴에 대해 이웃성(locality)을 최적화할 것인가에 관한 것.
※ 메카니즘 - LSM, B-Tree : 연산규약. 데이터를 어떻게 찾고 갱신 병합 하는가에 관한 것.  

## Query Execution: Compilation and Vectorization
- 분석을 위한 복잡한 쿼리는 여러 단계의 실행 계획으로 분리된다. 이러한 단계를 operator 라고 하며 병렬로 실행 될 수 있다. 
  쿼리 플래너는 어떤 순서로 operator 로 실행할지,  operator 를 어디에서 실행할지를 결정하는 것으로 최적화를 진행한다.
- 각각의 operator 내에서 컬럼의 값들을 대상으로 여러 작업을 시행한다. 
- 수백만 개의 행을 스캔해야 하는 data warehouses 에서의 쿼리는 데이터의 양 뿐만아니라 복잡한 operator 들을 실행하는데 필요한 CPU 시간도 고려해야한다.
  가장 단순한 operator 의 구현형태로 인터프리터 와 같은 형태 있고, 이는 각 행을 순회하면서 작업하나 너무 니다.
- Query compilation
  - SQL 쿼리를 받아서 실행하기 위한 코드를 생성하고 최종적으로 머신 코드로 컴파일 해서 사용한다.
  - 각 행을 순회하기는 하나 컴파일된 머신 코드를 CPU 시간이 줄어든다.
- Vectorized processing
  - 쿼리를 컴파일하지 않고 인터프리트해서 실행하지만, 행을 하나씩 순회하는 대신 하나의 컬럼에서 여러 값을 묶음 단위로 처리하여 속도를 높인다.
  - bitmap 과 같은 압축된 데이터를 해제해서 메모리에 올리지 않고 그대로 사용함으로 메모리를 절약한다.
- 두가지 모두 기본적으로 메모리를 연속적으로 사용함으로 캐쉬 히트가 증가해서 속도가 빠르다.

## Materialized Views and Data Cubes
- A materialized view is an actual copy of the query results, written to disk, whereas a virtual view is just a shortcut for writing queries.
- When the underlying data changes, a materialized view needs to be updated accordingly.
  Performing such updates means more work on writes, but materialized views can improve read performance in workloads that repeatedly need to perform the same queries.
- count, sum, avg 등 특정 집약(aggregate) 는 쿼리에 자주 사용됨으로 미리 계산해놓으면 유용하다. 미리 계산된 값들을 materialized aggregates 라고 한다.
  이 값들을 여러 dimension 의 조합으로 저장해 둔 것을 data cube 라고 한다.

---

# Multidimensional and Full-Text Indexes
- B-Tree 와 LSM-Tree 는 하나의 어트리뷰트에 대해서 범위 쿼리가 가능하다. 하지만 때때로 하나의 어트리뷰트로 검색은 충분하지 않다.
- multicolumn index
  - concatenated index: column 을 이어 붙여서 몇가지 필드를 조합한 키로 만들어 사용하는 것. 인덱스의 정의가 필드가 어떤 순서로 조합되는지에 의해 특정된다.
    - 접근이 계층적 
    - lastname, firstname 으로 concatenated index 를 만든다면, lastname 으로 먼저 필터링 하는 쿼리에응 효과적이겠지만 firstname 부터 시작하는 쿼리에는 아무런 효용이 없다.    
  - multidimensional indexes: column 의 필드를 대등하게 취급하는 조합키를 만들어 사용하는 것. 
    - 접근이 사각형 

## Full-Text Search
- full-text search 를 multidimensional query 의 한 종류로  생각 할 수 있다.
  - inverted index
    - text 에 나타나는 단어들을 term 이라 하고 이것들을 하나의 dimension 으로 취급할 수 있다. term 에 대해서 문서의 id 에 대한 index 를 만들고, 
      문서 id 에 대해서 bitmap 으로 인코딩 하면 bit wise 연산으로 어떤 term 들을 가지는 문서를 빠르게 찾을 수 있다.(vectorized 처리)
  - n-gram
    - 단어의 경계없이 특정 길이 n 의 substrings 으로 나누는 방법, n 자 이상인 substrings 으로 문서를 찾을 수 있다.
- 오탈자 대응
  - edit distance 라는 개념을 도입해서, 매칭을 느스한게 한다. 
    term 는 trie 같은 형태로 보존되는데, 검색 시점에서 edit distance k 인 오토마톤을 만들고, 이를 trie 와 교차해서 거리가 k 이내인 것들을 찾는다. 
  
## Vector Embedding
- semantic search 는 문서의 컨샙과 유저의 의도를 이해하려고 한다. 문서의 의도를 이해 하기 위해서 LLM 과 같은 embedding 모델을 사용하여 문서를 vector 값으로 바꾼다.
- 유사한 의미를 가진 문서들은 벡터값이 비슷하게 계산된다.
- semantic search 엔진에서 유저의 쿼리는 vector 값으로 바뀌고, vector index 를 통해서 유사한 값을 찾는 문서를 찾는다.  
- vector 값은 많은 dimension 을 가지고 정렬·공간분할 기반 인덱싱 방법으로는 이웃성 보존이 안되기 때문에, 특화된 방법을 사용한다.
  - Flat indexes: 쿼리 벡터값에 대해서 모든 벡터값들과 유사도를 비교한다. 정확하지만 느리다.
  - Inverted file indexes: 비교해야할 벡터값들의 수를 줄이기 위해서, 벡터 공간을 나누고 중심점을 둔 다음, 그 값과 유사도를 비교해 파티션을 고른 뒤, 그 파티션 안의 벡터값들과 유사도를 비교한다. 결과는 근사적이다.
  - Hierarchical Navigable Small World (HNSW) indexes: 
    백터공간의 부분집합을 레이어로 나누고, 그래프를 탐색한다. 레이어가 아래로 내려갈 수록 부분집합은 촘촘해 진다. 
    가장 비슷한 백터 값을 찾으면 아래의 레이어로 이동한다. 
    가장 아래의 레이어에 도달할 때 까지 이를 반복한다. 결과는 근사적이다.
  - sparse index 와 비슷하게 대표값으로 범위를 좁히지만 Inverted file indexes, HNSW 의 결과는 근사값이다. 
- 많은 vector database 가 IFI 와 HNSW 를 구현하고 있다.


---

# Summary

데이터를 저장하고 불러오는 방법에 대해서 배운다.
데이터 베이스에서 키로 값을 효율적으로 찾기 위해서 인덱스라는 자료구조를 사용한다. 인덱스는 원본 데이터에서 파생된 추가적인 구조이다.
종류에 상관없이 인덱스는 쓰기에 간접비를 발생시킴으로, 어플리케이션에 적합한 인덱싱 방식을 선택해야 한다.

효율적으로 저장하고 불러오는 방식으로 log-structured 접근 방식이 있다. 엔트리가 덧붙이기만 할 수 있는 저장공간에 추가 변경 삭제 될때 마다 이를 기록 하는 방식이다.
이 방식을 구현한 것으로 LSM 메카니즘이 있다. 불러오기를 효율적으로 할 수 있는 전재조건은 정렬임으로 키를 정렬해서 저장해야 하는데 엔트리가 추가할때 데이터 전체를 다시 정렬하는 것은 비용이 크다.
LSM-tree 는 은 이를 변경 작업에 바로 응답할 수 있는 memtable 과 메모리상의 저장공간을 버퍼로 두고, 사이즈의 임계에 도달하면 디스크에 저장하는데 이를 segment 저장한 다음
백그라운드에서 이들 segment 의 간결화 작업을 진행하는 것으로 쓰기의 효율성을 달성한다. 
segment 의 파일 포멧은 SSTable 으로 Sorted Strings Table 으로 내부에 bloom filter 를 위한 해시값과 sparse index 가 위치하고 있다. 
데이터를 읽을 때는 sparse index 와 segment 의 키의 부재를 빠르게 확인 할 수 있는 bloom filter 를 사용한다.

또 다른 방식으로 update-in-place 방식이 있다. 이는 엔트리를 수정할때 저장된 자리를 바로 찾아서 수정하는 방식이다.
이 방식을 구현한 것으로 B-tree 메카니즘이 있다. B-tree 는 고정된 크기의 page(block) 로 데이터를 나누고, 각 페이지는 다른 페이지에 대한 참조를 갖는다.
이로 인해 계층적인 트리구조가 된다. 자식 페이지가 얼마나 많은 다른 자식 페이지를 가지는지가를 branching factor 라고 한다.
이러한 알고리즘으로 전체 데이터 베이스는 균형된 상태(balanced tree)를 가지고 n 개의 key 에 대해서 O(log n) 의 깊이를 가진다. (시간 복잡도가 O(log n) 이다.)

LSM-Tree 와 B-Tree 를 비교해 본다. 
읽기에 한해서는 키를 찾는 속도가 예측 가능하기 때문에 B-tree 가 성능이 좋다. 
LSM 이 쓰기 산출량이 더 좋다. LSM 은 큰 작업을 적은 횟수로 하고(sequential writes) B-Tree 는 작은 작업을 빈번하게 한다.(random writes)
쓰기 삭업에 수반되는 디스크에 써지는 바이트 수를 Write amplification 이라고 하는데, 
LSM-Tree 은 전체를 다시 쓰는 경우가 있지만, B-Tree 는 page 를 나누는 과정이 있기 때문에 write amplification 이 높다.
write amplification 이 높으면 쓰기 성능이 떨어진다.
디스크 사용에 있어서 B-tree 는 시간이 지날수록 파편화가 일어난다.(사용 하는 페이지를 정리하는 별도의 작업이 필요할 수 있다.) LSM-Tree 는 주기적으로 간결화가 일어나기 때문에 이런 문제가 없다.

여러 컬럼을 가지는 데이터 베이스에서도 다른 행에 대해서 secondary index 만드는데 같은 메카니즘을 적용할 수 있다. 단 key 처럼 유일하다는 제약사항이 없음으로 별도의 장치가 필요하다.
secondary index 는 보통 행의 위치에 대한 참조(pk) 를 가지고, pk 는 인덱스와 행이 한쌍으로 저장되는 clustered index 방식으로 저장된다.

메모리 가격이 저렴해 지면서 데이터 베이스에 메모리에 저장하려는 in-memory-database 시도가 일어났다.
직관과는 달리 in-memory-database 가 빠른 이유는, 디스크에서 읽은 데이터는 이미 캐쉬되어서 메모리에 존재함으로 
메모리가 디스크보다 빠르기 때문이 아니라, 디스크를 사용하는 DB 가 디스크에 저장하기 위한 형태를 가지고 있고 이를 변환하는 비용 때문에 발생한다.

분석을 위한 Data Storage 에 대해서 배운다.
cloud data warehouse 를 사용하면 cloud 서비스와 통합하기 쉽다. idle 한 리소스에 대해서 비용을 지불하지 않기 때문에 비용이 절약된다.
오픈소스 데이터 웨어하우스도 다른 시스템과 쉽게 통합할 수 있도록 여러 컴퍼넌트로 분리되었다.

데이터 웨어 하우스는 많은 column 을 가지는 star schema 를 가지고 있다. 쿼리는 보통 특정한 column 을 타겟으로 하는데 모든 column 을 포함하는 row 를 다루는 것은 비효율적이다.
Column-Oriented Storage 는 column 별로 데이터를 모아서 저장하는 것으로, 쿼리에서 타겟으로 하는 column 만을 처리할 수 있다.
대부분의 분석 데이터 베이스에서 이 방법을 사용한다.

Column-Oriented Storage 에서 각각의 column 값들이 반복되는 성질(star schema 이기 때문에)을 이용해서 Column compression 을 할 수 있다.
약간의 압축과 그상태로 연산 가능한 표현으로 bitmap 을 활용하는 방법이 있다. 
column 의 값들의 종류만큼 bitmap 을 준비해 놓는다. bitmap 의 각 행은 특정한 값을 대표하고, 열은 column 의 값들의 sequence 이다.
대표하는 값과 실제 값이 일치하는 곳을 1를 설정하는 것으로 bitmap 을 구성한다.
이렇게 하면 쿼리에 대해서 bitwise 한 연산으로 쿼리를 처리 할 수 있다. 다른 column file 도 같은 위치가 같은 row 와 연결되어 있다는 제약사항이 있음으로 다른 column 에 대해서도 bitwise 연산이 가능하다.
bitmap 의 크기가 커진다면 0 이 반복된다는 성질을 활용해서 run-length encoding 적용해서 더욱 압축 할 수 있다.
본체 데이터(fact table)에서 특정 컬럼을 sort key 로 설정해서 정렬된 상태로 저장하면 압축률을 올릴 수 있다.

Column-Oriented Storage 는 데이터 분석에서 쿼리 실행을 빠르게 한다.  
data warehouses 의 쓰기작업은 ETL 을 통해 방대한 데이터를 일괄 처리하는데 이때 Column-Oriented 레이아웃으로 데이터를 변환하는건 비용이 크다.
log-structured 접근 방식에서 처럼 row-oriented 방식으로 저장하다가 데이터가 일정량 모였을 때 백그라운드에서 column-oriented 로 변환을 처리한다.

쿼리를 효과적으로 실행하기 위한 두가지 방식인 Compilation and Vectorization 에 대해서 배운다.
분석을 위한 복잡한 쿼리는 여러단계의 실행 계획으로 분리되고 각각의 단계를 operator 라고 한다.
각각은 병렬로 실행될 수 있고 쿼리플래너를 통해서 operator 에 대한 최적화가 이루어진다.
수백만 개의 행을 스캔해야 하는 data warehouse 에서는 데이터의 양 뿐만 아니라 operator 들의 cpu 시간도 고려해야 한다.

가장 단순한 operator 구현은 인터프리터와 같은 형태로 각 행을 순회하며 처리한다.
Query compilation 은 SQL 쿼리를 받아서 이를 실행하기 위한 코드를 생성한다. 각 행을 순회하는건 인터프리터와 같으나 JIT 과 유사하게 머신코드를 사용해서 실행시간을 단축한.
Vectorized processing 은 쿼리를 컴파일 하지 않고 인터프리터 처럼 동작하지만, bitmap 과 같은 압축된 표현을 그대로 처리함으로 처리량이 높다. 압축을 풀지 않고 그대로 처리하는 것도 장점이다.
두 방법이 같이 사용되고, 기본적으로 메모리를 연속적으로 사용함으로 캐쉬 히트가 증가해서 속도가 빠르다.

materialized view 는 쿼리 결과의 사본을 디스크에 저장한 것이다. 기반이 되는 데이터가 변경되었을 때 이것을 업데이트 하는건 단순한 쓰기작업 이상을 의미하지만, 
같은 쿼리를 반복적으로 수행하는 업무에서는 쿼리 성능을 높일 수 있다.
materialized aggregates 는 materialized view 의 한 종류로, 자주 사용되는 쿼리인 count, sum, avg 등의 특정 집약을 미리 계산해 놓은 것이다.
OLAP 에서 여러 dimension 의 조합으로 계산된 값을 저장해 두는 구조를 data cube 라고 한다.

특정한 하나의 column 에 대해서만 검색하는게 충분하지 않을 수 있다. 이때 multicolumn index 를 사용할 수 있다.
concatenated index 는 몇가지 column 의 값들을 이어 붙인 것을 키로 사용하는 방법으로. 인덱스의 정의가 필드가 어떤 순서로 조합되는지에 의해 특정된다.
전화번호부와 같은 정렬 방식으로 접근이 계층적일때 효과적이다.
multidimensional index 는 column 을 대등하게 취급해서 값들을 조합키를 만들어 사용한다. 
latitude, longitude 와 같이 대등한 속성에 대해서 multidimensional index 를 만들면 효과적인 범위 검색이 가능한다. 

full-text search 는 keyword 가 등장하는 text document 들의 집합을 찾는 것을 의미한다.
이는 검색하는 각각의 단어(term) 에 대한 multidimensional query 라고 볼 수 있다.
inverted index 방식은 문서들에 나타난 각 term 를 키로 두고 문서 id(리스트) 를 값으로 둔 구조로, 이를 bitmap 으로 구성하면 bitwise 연산을 사용할 수 있다.
예를 들어 두가지 term 을 포함하는 문서를 찾으면 각 term 에 대해서 각각의 bitmap 을 불러와서 다시 bitwise and 연산을 하는 것으로 결과를 찾을 수 있다.
n-gram 은 단어의 경계없이 특정 길이의 n 의 substrings 으로 나누는 방법, n 자 이상인 substrings 으로 문서를 찾을 수 있다.
오탈자에도 대응할 수 있도록 edit distance 라는 개념을 도입해서 매칭을 느슨한게 한다.

LLM 과 같은 embedding 모델을 사용하여 문서의 컨샙과 의도를 vector 값으로 변경하는 것을 Vector Embedding 이라고 한다.
유사한 의미를 가진 문서들은 벡터값이 비슷하게 계산된다.
vector 값은 많은 dimension 을 가지고 있어서 기존 정렬·공간분할 기반 인덱싱 방법으로는 이웃성 보존이 안되기 때문에, 특화된 방법을 사용한다.
Flat indexes 는 쿼리 벡터값에 대해서 모든 벡터값들과 유사도를 비교한다. 정확하지만 느리다.
Inverted file indexes 는 비교해야할 벡터값들의 수를 줄이기 위해서, 벡터 공간을 나누고 중심점을 둔 다음, 그 값과 유사도를 비교해 파티션을 고른 뒤, 그 파티션 안의 벡터값들과 유사도를 비교한다. 결과는 근사적이다.
Hierarchical Navigable Small World (HNSW) indexes 는 백터공간의 부분집합을 레이어로 나누고, 그래프를 탐색한다. 레이어가 아래로 내려갈 수록 부분집합은 촘촘해 진다. 가장 비슷한 백터 값을 찾으면 아래의 레이어로 이동한다. 
가장 아래의 레이어에 도달할 때 까지 이를 반복한다. 결과는 근사적이다.
sparse index 와 비슷하게 대표값으로 범위를 좁히지만 Inverted file indexes, HNSW 의 결과는 근사값이다.
많은 vector database 가 IFI 와 HNSW 를 구현하고 있다.


OLTP
- log-structured (LSM-tree)
  - compaction 을 백그라운드 프로세스로 둔다.
- update-in-place (B-tree)
  - 다른 구조를 두지는 않지만, 균현잡힌 트리 구조로 sparse index 를 완전히 내재화 한다. 

OLAP
- 덜 읽기 → column-oriented layout + bitmap 으로 압축 
- 미리계산 → materialized aggregate (data cube)
- CPU 시간 단축 → 컴파일, vectorization (bitmap 표현을 사용해서 bitwise 연산)

multidimensional / full-text / vector
- multicolumn index: 쿼리 모양에 따라 다른 인덱스가 요구됨
  - concatenated index
  - multidimensional index
    - lat, long 등, 대등학 속성에 유리
- full-text search : 전통적으로 키워드를 포함하는 문서를 찾음
  - inverted index
    - term: 단어 경계
    - n-gram: n 길이의 substirng 사용
- vector embedding : LLM 으로 의미를 vector 값으로 변경
  - 많은 dimension 을 가지는 vector 값에 대한 indexing 방법 
    - IFI
    - HNSW


무결성(Integrity)과 정합성(Consistency) ?