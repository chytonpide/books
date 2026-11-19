## 느낌
개론적인 책
어떤 층위의 다른 구현체를 통해서 그 층위에 대한 이해가 늘어남.
모르는 지식인 아니지만 전체적인 뷰를 선명하게 해줌.

애초에 용어들이 모호하고 미묘하다. 잘본된 부분도 있어서 여러 블로그의 기사를 읽으면서도 아리송할만도 했다.
운이 나쁜건지 좋은건지 난 커리어에 걸쳐서 심각한 동시성 문제는 만날 수 없었고, 흔히 말하는 낙관적 락, 비관적 락으로 문제를 방지할 수 있었다.


## 문장
- 데이터 모델이 왜 중요한지

- Errors will inevitably happen, but many software developers prefer to think only about the happy path rather than the intricacies of error handling. For example, popular object-relational mapping (ORM) frameworks such as Rails ActiveRecord and Django don’t retry aborted transactions—the error usually results in an exception bubbling up the stack, so any user input is thrown away, and the user gets an error message. This is a shame, because the whole point of rolling back transactions is to enable safe retries.
- acid 에 관한것


## AI가 준것

repository 추상화로 분리되는 건 쿼리를 어떻게 작성하느냐(SQL 문법, ORM 매핑)입니다. 분리되지 않는 건 그 쿼리가 어떤 성능과 동시성 특성을 갖느냐입니다. 
인덱스, lock 범위, 격리 수준의 효과는 "어떤 조건으로 얼마나 자주, 동시에 접근하느냐"에 달려 있고, 그건 유즈케이스가 정합니다. 
추상화가 문법은 감춰도 비용은 감추지 못하는, 전형적인 leaky abstraction입니다.