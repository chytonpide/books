# What is SST
Serverless Stack, 서버리스 애플리케이션을 쉽고 안전하게 개발·배포할 수 있게 해주는 프레임워크.
cdk 랑 다른점은 더 높은 레벨의 컴퍼넌트를 정의할 수 있다는 것, 간단하게 리소스를 링크할 수 있다는 것, dev mode(개발중 람다함수의 변경을 바로 반영) 를 제공한다는 것 이다. 

## Component
```ts
const cluster = new sst.aws.Cluster("MyCluster", { vpc });

new sst.aws.Service("MyService", {
  cluster,
  loadBalancer: {
    ports: [{ listen: "80/http" }]
  }
});
```
어플리케이션의 어떤 기능을 정의한것, 필요한 `Infrastructure` 가 정의된다. 

## Configure
```ts
new sst.aws.Function("MyFunction", {
  handler: "src/lambda.handler",
  transform: {
    role: (args) => ({
      name: `${args.name}-MyRole`
    })
  }
});
```
컴퍼넌트의 설정을 변경할 수 있다. `transform` 을 사용해서 인프라가 생성될때 설정 변경등을 실행할 수 있다. 
### Transform
- function: Lambda function 변경
- logGroup: Lambda 의 LogGroup 리소스 변경
- role: Lambda 의 role 변경

## Providers
인프라 제공회사, 기본적으로 aws 와 Cloudflare 제공

## Link resources
```ts
const bucket = new sst.aws.Bucket("MyBucket");

const cluster = new sst.aws.Cluster("MyCluster", { vpc });

new sst.aws.Service("MyService", {
  cluster,
  link: [bucket],
  loadBalancer: {
    ports: [{ listen: "80/http" }]
  }
});
```
link, bind 를 통해 lambda 가 어떤 리소스를 쓸지 자동으로 연결한다.
위 코드에서 컨테이너의 앱은 s3 을 바로 사용할 수 있다. 
## Project structure
- Drop-in mode: 앱코드와 인프라 코드가 같은 층위에 존재, 예를 들어 sst.config.ts 가 nextjs 프로젝트안에 존재.
```
my-nextjs-app
├─ next.config.js
├─ sst.config.ts
├─ package.json
├─ app
├─ lib
└─ public
```
- Monorepo: sst.config.ts 를 상위에 놓고 층위를 나누기.
```
my-sst-app
├─ sst.config.ts
├─ package.json
├─ packages
│  ├─ functions
│  ├─ frontend
│  ├─ backend
│  └─ core
└─ infra
```

## CLI
기본 cl 인터페이스다. 
``
sst dev
``
dev 모드를 사용하면 로걸에서 서버처럼 lambda를 실행/테스트할 수 있게 해준다.
- 인프라 변경 사항을 배포하는 **워처(watcher)**를 실행합니다.
- Live 모드로 함수를 실행하여, 재배포하지 않고도 코드를 수정하고 테스트할 수 있게 해줍니다.
- 로컬 머신을 VPC 안의 리소스와 연결하는 터널을 생성합니다.
- 프론트엔드와 컨테이너 서비스를 개발 모드(dev mode)로 실행하고, 이를 인프라에 연결합니다.
`Live`는 람다 함수의 코드 수정 → 저장 → 바로 실행에 반영되는 것.
```
sst deploy --stage production
```
deploy 한다. stage 파라미터에 환경을 설정할 수 있다.
```
sst deploy --stage pr-123
```
PR 도 디플로이 할 수 있다!?

------------------------
# Workflow
sst 에서는 프로바이더의 콘솔에 로그인 할 필요 없이 코드로 모든것을 관리할 수 있다.
sst app 은 `sst.config.ts` 파일에 의해서 설정된다.
Credentials 이 필요하다. 자격증명이 필요하다. aws 에서 유저를 만든후 ~/.aws/credentials 에 키가 설정되어 있어야 한다. (이는 aws cli 설정시 초기화 돤다.)

