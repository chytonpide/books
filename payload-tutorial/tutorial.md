- 어드민은 데이터를 관리하는 곳, 스키마는 코드로 작성한다. - Payload의 의도된 설계
- page.tsx, page.client.tsx 의 관계: 서퍼 컴퍼넌트와 클라이언트 컴퍼넌트
  - generateStaticParams(): 빌드 시점에 한번 실행되어서 빌드 결과가 .next/ 에 만들어진다.
  - 빌드 시점엔 "전송 가능한 모든 산출물"이 다 결정된다.  (HTML, RSC payload, JS 청크). 런타임에 새로 생기는 건 없다.
  - Server Component는 HTML/RSC만 가고 거기서 끝
    -  Server Component 도 dynamic rendering 이면, 서버에서 실행된다. (SSR)
      - 빌드 후에 새로 추가된 Server Component 페이지는 한번 실행된후 캐싱된다.  
  - Client Component는 HTML/RSC + JS 청크가 가서 hydration으로 한 번 더 살아남.
```
       ┌─ Server Components: HTML 만들고 → JS는 버림 (클라에 안 감)
RSC ───┤                                                                                                                                                                                                       
       └─ Client Components: HTML도 만들고 → JS도 같이 보냄 (hydration용)            
``` 
          

