![image](https://user-images.githubusercontent.com/36217195/123538820-8b1ad580-d771-11eb-883c-005c50b20180.png)



# 서비스시나리오

## 기능적 요구사항
```
1. 관리자는 티켓을 등록한다. 
2. 티켓이 등록되면, 티켓의 기본 가격이 생성되고 요일에 따라 할인가격이 적용된다. (기본가격은 1만원, 수/목요일은 20% 할인) 
3. 사용자는 티켓을 예약한다. 
4. 티켓의 상태가 '예약가능'이고, 만료일(Enddate)이 경과하지 않았을 때 예약 가능하고, 예약에 성공하면 티켓의 상태는 '예약됨'으로 변경된다. 
5. 사용자 등급이 'VIP'일때는 추가 할인 혜택이 적용된다. (VIP일 때 1천원 추가 할인)
6. 사용자는 티켓 예약을 취소할 수 있다.
7. 관리자와 사용자는 티켓 정보, 사용자 예약현황, 가격을 조회할 수 있다.
```
## 비기능적 요구사항

```
1. 트랜잭션
  - 티켓 예약 전에 티켓의 상태가 '예약가능'이고, 만료일이 경과하지 않았는지를 반드시 확인한다. -> Synch 호출
  - 사용자가 예약을 취소하면 티켓의 상태가 '예약가능'으로 변경되고, 가격도 사용자 등급에 따라 적용된 추가 할인 정책이 원상복구 되어야 한다.  --> SAGA, 보상 트랜잭션

2. 장애격리
  - 티켓관리(ticket) 서비스가 과중되면, 예약(reservation)을 잠시 후에 하도록 유도한다. --> Circuit breaker, fallback
  - 가격(price) 서비스가 수행되지 않더라도 365일 24시간 티켓예약을 취소할 수 있어야 한다. --> Asynch(event-driven), Eventual Consistency

3. 성능
  - 사용자와 관리자가 티켓 정보, 예약현황, 가격정보 조회시 성능을 고려하여 별도의 view로 구성한다. --> CQRS
```


# 체크포인트
```
1. Saga
2. CQRS
3. Correlation
4. Req/Resp
5. Gateway
6. Deploy/ Pipeline
7. Polyglot
8. Config Map/ Persistence Volume
9. Circuit Breaker
10. Autoscale (HPA)
11. Zero-downtime deploy (Readiness Probe)
12.Self-healing (Liveness Probe)
 ```
 
# 분석/설계


## Event Storming 결과
* MSAEz 로 모델링한 이벤트스토밍 결과:  http://www.msaez.io/#/storming/mD3qVoL8dKTd723pyildzLwnvQq2/e90de0f65fb6a78f77f6412a03d85d24


### Event 도출

![image](https://user-images.githubusercontent.com/36217195/123536311-826fd280-d764-11eb-8816-0980f1ef8f07.png)



### 부적격 Event 탈락

![image](https://user-images.githubusercontent.com/36217195/123536406-15107180-d765-11eb-8bf1-bd3f1a18f9d1.png)

- 중복되거나 잘못된 도메인 이벤트들을 걸러내는 작업을 수행함


### Actor, Command 부착

![image](https://user-images.githubusercontent.com/36217195/123536612-5a816e80-d766-11eb-9c4a-4bacd895a146.png)


### Aggregate 로 묶기

![image](https://user-images.githubusercontent.com/36217195/123536708-eeebd100-d766-11eb-9078-926f85142462.png)


### Bounded Context로 묶기
![image](https://user-images.githubusercontent.com/36217195/123536851-9e28a800-d767-11eb-8c6f-c02af09d575d.png)


### Policy 부착/이동 및 Context Mapping 
- 예약은 사용자가 티켓을 예약/취소하고 이력을 관리함
- 티켓은 관리자가 티켓을 등록하고, 사용자 예약에 따른 티켓의 상태를 관리함
- 가격은 정책에 따른 할인을 적용하거나 취소함
![image](https://user-images.githubusercontent.com/36217195/123537225-4d19b380-d769-11eb-9dde-41bfa940ca4e.png)


### 완성된 모형

![image](https://user-images.githubusercontent.com/36217195/123535964-87338700-d762-11eb-98e6-58e569d92806.png)


### 완성본에 대한 기능적/비기능적 요구사항을 커버하는지 검증

![image](https://user-images.githubusercontent.com/36217195/123537839-90295600-d76c-11eb-8a61-f1c1d26dd95e.png)

1. 관리자는 티켓을 등록한다. (OK)
2. 티켓이 등록되면, 티켓의 기본 가격이 할당되고 정책에 따라 할인가격이 적용된다. (OK)

![image](https://user-images.githubusercontent.com/36217195/123537855-a59e8000-d76c-11eb-9c0a-a1c78a5e13bd.png)

3. 사용자는 티켓을 예약한다. (OK)
4. 티켓의 상태가 '예약가능'이고, 만료일이 경과하지 않았을 때 예약 가능하고, 티켓의 상태는 '예약됨'으로 변경된다. (OK)
5. 사용자 등급이 'VIP'일때는 추가 할인 혜택이 적용된다. (OK)

![image](https://user-images.githubusercontent.com/36217195/123537887-d383c480-d76c-11eb-9342-9055172240cd.png)

6. 사용자는 티켓 예약을 취소할 수 있다.(OK)
   티켓은 다시 '예약가능' 상태로 변경되고, 가격도 사용자 등급에 따른 할인 적용을 원복한다. 


![image](https://user-images.githubusercontent.com/36217195/123537943-20679b00-d76d-11eb-92fb-4dee049377c7.png)

7. 관리자와 사용자는 티켓 정보, 사용자 예약현황, 가격을 조회할 수 있다. (OK)


### 비기능 요구사항에 대한 검증

![image](https://user-images.githubusercontent.com/36217195/123538161-62451100-d76e-11eb-8c71-167696c19d6a.png)
```
1.트랜잭션
  - 티켓 예약 전에 티켓의 상태가 '예약가능'이고, 만료일이 경과하지 않았는지를 반드시 확인한다. -> Synch 호출(OK)
  - 사용자가 예약을 취소하면 티켓의 상태가 '예약가능'으로 변경되고, 가격도 사용자 등급에 따라 적용된 추가 할인 정책이 원상복구 되어야 한다.  --> SAGA, 보상 트랜잭션 (OK)

2. 장애격리
  - 가격(price) 서비스가 수행되지 않더라도 365일 24시간 티켓예약을 취소할 수 있어야 한다. --> Asynch(event-driven), Eventual Consistency (OK)
  - 티켓관리(ticket) 서비스가 과중되면, 예약(reservation)을 잠시 후에 하도록 유도한다. --> Circuit breaker, fallback (OK)

3. 성능
  - 사용자와 관리자가 티켓 정보, 예약현황, 가격정보 조회시 성능을 고려하여 별도의 view로 구성한다. --> CQRS (OK)
```
## 헥사고날 아키텍처 다이어그램 도출

![image](https://user-images.githubusercontent.com/36217195/123538459-febbe300-d76f-11eb-9cf5-ca71034d0bde.png)

- Chris Richardson, MSA Patterns 참고하여 Inbound adaptor와 Outbound adaptor를 구분함
- 호출관계에서 PubSub 과 Req/Resp 를 구분함
- reservation의 경우 Polyglot 검증을 위해 Hsql로 셜계


# 구현
## DDD 적용
MSAEZ.io를 통하여 도출된 Aggregate는 Entity로 선언하였고, Repository Pattern을 적용하기 위해 Spring Data REST의 RestRepository를 적용하였다. 

### ticket 서비스의 ticket.java
```java
package ticket;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;
import javax.persistence.Table;

import org.springframework.beans.BeanUtils;

@Entity
@Table(name = "Ticket_table")
public class Ticket {
    @Id
    private Long ticketId;
    private String status; // 예약가능, 예약됨, 만료됨
    private Date starttime;
    private Date endtime;

    @PostPersist
    public void onPostPersist() {
        Registered registered = new Registered();
        BeanUtils.copyProperties(this, registered);
        registered.publishAfterCommit();
    }

    @PostUpdate
    public void onPostUpdate() {
        // 티켓의 상태가 변경되었을 때, StatusUpdated 이벤트 Pub
        StatusUpdated statusUpdated = new StatusUpdated();
        BeanUtils.copyProperties(this, statusUpdated);
        statusUpdated.publishAfterCommit();
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getStarttime() {
        return starttime;
    }

    public void setStarttime(Date starttime) {
        this.starttime = starttime;
    }

    public Date getEndtime() {
        return endtime;
    }

    public void setEndtime(Date endtime) {
        this.endtime = endtime;
    }

}
```


### ticket 서비스의 PolicyHandler.java
```java
package ticket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import ticket.config.kafka.KafkaProcessor;

@Service
public class PolicyHandler {
    @Autowired
    TicketRepository ticketRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverCancelled_Cancel(@Payload Cancelled cancelled) {

        if (cancelled.validate()) {
            //예약을 취소한 경우, 티켓의 상태를 '예약가능'으로 변경
            Ticket ticket = ticketRepository.findByTicketId(Long.valueOf(cancelled.getTicketId()));
            ticket.setStatus("예약가능");
            ticketRepository.save(ticket);
        }

    }

}
```

### ticket 서비스의 TicketRepository.java
```java
package ticket;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="tickets", path="tickets")
public interface TicketRepository extends PagingAndSortingRepository<Ticket, Long>{

    Ticket findByTicketId(Long ticketId);

}
```


분석/설계 단계에서 도출된 헥사고날 아키텍처에 따라, 구현한 각 서비스의 실행방법은 아래와 같다.
(포트넘버 : 8081 ~ 8084, 8088)
```shell
cd reservation
mvn spring-boot:run  

cd ticket
mvn spring-boot:run

cd price
mvn spring-boot:run 

cd view
mvn spring-boot:run  
    
cd gateway
mvn spring-boot:run
```

## Gateway 적용
API GateWay를 통하여 마이크로 서비스들의 집입점을 통일할 수 있다. 다음과 같이 Gateay를 적용하였다.
 ```yaml  
server:
  port: 8088

---

spring:
  profiles: default
  cloud:
    gateway:
      routes:
        - id: reservation
          uri: http://localhost:8081
          predicates:
            - Path=/reservations/** 
        - id: ticket
          uri: http://localhost:8082
          predicates:
            - Path=/tickets/** 
        - id: price
          uri: http://localhost:8083
          predicates:
            - Path=/prices/** 
        - id: view
          uri: http://localhost:8084
          predicates:
            - Path= /views/**
      globalcors:
        corsConfigurations:
          '[/**]':
            allowedOrigins:
              - "*"
            allowedMethods:
              - "*"
            allowedHeaders:
              - "*"
            allowCredentials: true


---

spring:
  profiles: docker
  cloud:
    gateway:
      routes:
        - id: reservation
          uri: http://reservation:8080
          predicates:
            - Path=/reservations/** 
        - id: ticket
          uri: http://ticket:8080
          predicates:
            - Path=/tickets/** 
        - id: price
          uri: http://price:8080
          predicates:
            - Path=/prices/** 
        - id: view
          uri: http://view:8080
          predicates:
            - Path= /views/**
      globalcors:
        corsConfigurations:
          '[/**]':
            allowedOrigins:
              - "*"
            allowedMethods:
              - "*"
            allowedHeaders:
              - "*"
            allowCredentials: true

server:
  port: 8080
 ```   

이후 시나리오 검증은 Gateway를 적용한 상태에서 수행한다.    


## 시나리오 검증

### [티켓 등록 및 가격 계산]

1. 관리자는 티켓을 등록한다. 
2. 티켓이 등록되면, 티켓의 기본 가격이 할당되고 정책에 따라 할인가격이 적용된다. 
  * 기본 가격 정책은 티켓의 starttime 이 수요일/목요일일 때 20% 할인이 적용된다.
  * 아래 케이스에서 
    * ticketId=2, 3의 starttime인 2021-07-07 이 수요일이고, 
    * ticketId=5 의 starttime인 2020-06-25 이 목요일이다. 
   
   
```Shell
### 티켓 등록 ###
http POST http://localhost:8088/tickets ticketId=1 status=예약가능 starttime=2021-07-06 endtime=2021-07-06
http POST http://localhost:8088/tickets ticketId=2 status=예약가능 starttime=2021-07-07 endtime=2021-07-07
http POST http://localhost:8088/tickets ticketId=3 status=예약가능 starttime=2021-07-07 endtime=2021-07-07
http POST http://localhost:8088/tickets ticketId=4 status=예약됨 starttime=2021-12-31 endtime=2021-12-31
http POST http://localhost:8088/tickets ticketId=5 status=예약가능 starttime=2020-06-25 endtime=2020-06-25
```   

```Shell
### 티켓 생성 결과 확인 ###
http GET http://localhost:8088/tickets
```
![image](https://user-images.githubusercontent.com/36217195/123546120-48b7bf80-d796-11eb-9eb7-3d74a2b6e8b9.png)

```Shell
###v기본 가격할당 및 정책에 따른 할인가격 적용###
http GET http://localhost:8088/prices
```
![image](https://user-images.githubusercontent.com/36217195/123546622-553d1780-d798-11eb-8832-f855b7c9e90d.png)
![image](https://user-images.githubusercontent.com/36217195/123546655-7dc51180-d798-11eb-98b2-47fb016a5722.png)


### [예약 및 사용자 등급별 할인가격 적용]
3. 사용자는 티켓을 예약한다. 
4. 티켓의 상태가 '예약가능'이고, 만료일이 경과하지 않았을 때 예약 가능하고, 티켓의 상태는 '예약됨'으로 변경된다. 
5. 사용자 등급이 'VIP'일때는 추가 할인 혜택이 적용된다. 

```Shell
### CASE 1) 예약 성공 - 화요일 티켓, Silver 등급 사용자로 할인이 적용되지 않음
http POST http://localhost:8088/reservations ticketId=1 userId=A userGrade=Silver   
http GET http://localhost:8088/tickets/1
http GET http://localhost:8088/prices/1

### CASE 2) 예약 성공 - 수요일 티켓(20% 할인 정책), Silver 등급 사용자로 추가 할인 없음
http POST http://localhost:8088/reservations ticketId=2 userId=A userGrade=Silver   
http GET http://localhost:8088/tickets/2
http GET http://localhost:8088/prices/2

### CASE 3) 예약 성공 - 수요일 티켓(20% 할인 정책), VIP 등급 사용자(1000원 추가 할인)
http POST http://localhost:8088/reservations ticketId=3 userId=B userGrade=VIP
http GET http://localhost:8088/tickets/3
http GET http://localhost:8088/prices/3

### CASE 4) 예약 실패 - 이미 '예약됨' 상태의 티켓 예약 시도
http POST http://localhost:8088/reservations ticketId=4 userId=B userGrade=VIP   

### CASE 5) 예약 실패 - endtime이 예약시점 이전인 티켓 예약 시도
http POST http://localhost:8088/reservations ticketId=5 userId=B userGrade=VIP    
```
CASE 1) 예약 성공 - 화요일 티켓, Silver 등급 사용자로 할인이 적용되지 않음
![image](https://user-images.githubusercontent.com/36217195/123550154-385c1080-d7a7-11eb-826a-c6dab4fc1047.png)

CASE 2) 예약 성공 - 수요일 티켓(20% 할인 정책), Silver 등급 사용자로 추가 할인 없음
![image](https://user-images.githubusercontent.com/36217195/123550185-604b7400-d7a7-11eb-90e6-fb7bcf4f2efb.png)

CASE 3) 예약 성공 - 수요일 티켓(20% 할인 정책), VIP 등급 사용자(1000원 추가 할인)
![image](https://user-images.githubusercontent.com/36217195/123550208-83762380-d7a7-11eb-9848-76d762d1a31a.png)

CASE 4) 예약 실패 - 이미 '예약됨' 상태의 티켓 예약 시도
![image](https://user-images.githubusercontent.com/36217195/123550279-c932ec00-d7a7-11eb-9e18-9e59ce5e9133.png)
![image](https://user-images.githubusercontent.com/36217195/123550282-cdf7a000-d7a7-11eb-8375-8905f3b5e7fc.png)

CASE 5) 예약 실패 - endtime이 예약시점 이전인 티켓 예약 시도
![image](https://user-images.githubusercontent.com/36217195/123550328-00090200-d7a8-11eb-8cc8-8945c517a44f.png)
![image](https://user-images.githubusercontent.com/36217195/123550364-1a42e000-d7a8-11eb-8cb2-4de3831efd5c.png)


### [예약 취소]
6. 사용자는 티켓 예약을 취소할 수 있다.
7. 관리자와 사용자는 티켓 정보, 사용자 예약현황, 가격을 조회할 수 있다.
```Shell
### 예약 취소 전 view (3번 티켓 예약된 상황)
http GET http://localhost:8088/views/3

### 예약 취소
http PATCH http://localhost:8088/reservations/3 status=CANCELLED 

### 얘역 취소 후 view - 예약은 'CANCELLED', 티켓은 '예약가능', 가격은 VIP 등급 할인전 가격(수요일 20%만 적용)
http GET http://localhost:8088/views/3
```
![image](https://user-images.githubusercontent.com/36217195/123551034-03ea5380-d7ab-11eb-9038-87a582b7ab05.png)
![image](https://user-images.githubusercontent.com/36217195/123550459-858cb200-d7a8-11eb-855e-0bc31e4352bc.png)
![image](https://user-images.githubusercontent.com/36217195/123551055-20868b80-d7ab-11eb-9685-7af1b9596600.png)



## Polyglot 프로그래밍 적용

reservation 서비스는 hsql DB를, ticket, price 서비스는 h2 DB를 적용하여 폴리글랏을 만족시키고 있다.

### reservation의 pom.xml DB 설정 코드
```xml
<dependency>
	<groupId>org.hsqldb</groupId>
	<artifactId>hsqldb</artifactId>
	<scope>runtime</scope>
</dependency>
```

### ticket, price 서비스의 pom.xml DB 설정 코드
```xml
<dependency>
	<groupId>com.h2database</groupId>
	<artifactId>h2</artifactId>
	<scope>runtime</scope>
</dependency>
```


# 운영
## namespace 생성
```shell
kubectl create ns eticket
```

## Deploy / Pipeline
### git에서 소스 가져오기
```shell
git clone https://github.com/geniee95/eTicket.git
```
### Build 하기
```shell
cd reservation
mvn package

cd ticket
mvn package

cd price
mvn package

cd view
mvn package

cd gateway
mvn package
```
### Docker Image Build/Push, deploy/service 생성 (yaml 이용)
```shell
cd reservation
az acr build --registry genie --image genie.azurecr.io/reservation:v1 .
kubectl create -f ./kubernetes/deployment.yml -n eticket
kubectl create -f ./kubernetes/service.yaml -n eticket

cd ticket
az acr build --registry genie --image genie.azurecr.io/ticket:v1 .
kubectl create -f ./kubernetes/deployment.yml -n eticket
kubectl create -f ./kubernetes/service.yaml -n eticket

cd price
az acr build --registry genie --image genie.azurecr.io/price:v1 .
kubectl create -f ./kubernetes/deployment.yml -n eticket
kubectl create -f ./kubernetes/service.yaml -n eticket

cd view
az acr build --registry genie --image genie.azurecr.io/view:v1 .
kubectl create -f ./kubernetes/deployment.yml -n eticket
kubectl create -f ./kubernetes/service.yaml -n eticket

cd gateway
az acr build --registry genie --image genie.azurecr.io/gateway:v1 .
kubectl create -f ./kubernetes/deployment.yml -n eticket
kubectl create -f ./kubernetes/service.yaml -n eticket
```	

### Deploy 완료
![image](https://user-images.githubusercontent.com/36217195/123649763-fa252680-d864-11eb-9e26-61d8c8843eee.png)




## ConfigMap
* 시스템별로 변경 가능성이 있는 설정들을 ConfigMap을 사용하여 관리한다.
	* reservation 서비스에서 호출하는 ticket 서비스 url을 ConfigMap을 사용하여 구현하였다. 
	
### application.yml 파일에 ${api.url.bikeservice} 설정

* reservation application.yaml 설정

![image](https://user-images.githubusercontent.com/36217195/123677110-b2f85f00-d87f-11eb-85f7-1783fc22771d.png)


* FeignClient 호출부분 

![image](https://user-images.githubusercontent.com/36217195/123677247-d7543b80-d87f-11eb-984e-48f1a5a4709e.png)


* reservation deploy.yaml 에 env 설정

![image](https://user-images.githubusercontent.com/36217195/123677373-feab0880-d87f-11eb-997f-476e404076df.png)


* configmap 생성 및 조회

```shell
kubectl create configmap ticketurl --from-literal=url=http://ticket:8080 -n eticket
kubectl get configmap ticketurl -o yaml -n eticket
```
![image](https://user-images.githubusercontent.com/36217195/123676230-a1628780-d87e-11eb-9928-e79b6b104554.png)


* reservation pod에서 환경변수 확인
```
kubectl exec -it pod/reservation-65c474dbb6-2jx2d -n eticket -- /bin/sh
$ env
```
![image](https://user-images.githubusercontent.com/36217195/123676733-4a10e700-d87f-11eb-9830-a2bfcdb5f628.png)




# 동기 호출/서킷 브레이커/장애격리

- 서킷 브레이킹 프레임워크의 선택 : Spring FeignClient + Hystrix 옵션을 사용하여 구현함

- Hystrix를 설정 : 요청처리 쓰레드에서 처리시간이 610 밀리가 넘어서기 시작하여 어느정도 유지되면 CB 회로가 닫히도록(요청을 빠르게 실패처리, 차단) 설정

- 동기 호출 주체인 reservation 서비스에 Hystrix 설정

- reservation/src/main/resources/application.yml 파일

```
	feign:
	  hystrix:
		enabled: true
	hystrix:
	  command:
		default:
		  execution.isolation.thread.timeoutInMilliseconds: 610
```

- 부하에 대한 지연시간 발생코드 TicketController.java 지연 적용
(400 ms에서 증감 220 안에서 랜덤하게 부하 발생)

![image](https://user-images.githubusercontent.com/36217195/123723115-af3afb80-d8c4-11eb-80cc-f388f530ac29.png)


### 부하 테스트 siege Pod 설치
```
kubectl apply -f -<<EOF
apiVersion: v1
kind: Pod
metadata:
  name: siege
spec:
  containers:
    - name: siege
      image: apexacme/siege-nginx
EOF
```

- 부하 테스터 siege툴을 통한 서킷 브레이커 동작확인 : 동시 사용자 10명, 10초 동안, 10번 반복 실시
```shell
##초기 데이터 등록
http POST http://52.231.95.4:8080/tickets ticketId=1 status=예약가능 starttime=2021-07-06 endtime=2021-07-06

kubectl exec -it pod/siege -c siege -n eticket -- /bin/bash
$ siege -c10 -t10S -r10 -v --content-type "application/json" 'http://reservation:8080/reservations POST {"ticketId":"1"}'
```

- 결과
* 0.61 secs가 넘어가는 경우, 서킷브레이커가 동작하여 500 에러를 발생시킴

![image](https://user-images.githubusercontent.com/36217195/123723273-00e38600-d8c5-11eb-95ae-9beabb8b0d47.png)

. . . . . 

![image](https://user-images.githubusercontent.com/36217195/123723317-18bb0a00-d8c5-11eb-9f96-bc9ca7d75e8c.png)
![image](https://user-images.githubusercontent.com/36217195/123723340-2a041680-d8c5-11eb-8e7e-10dfa06b9dc5.png)


## Autoscale Out (HPA)
앞서 서킷브레이커는 빠른 실패를 통해 시스템을 안정되게 운영할 수 있게 해줬지만, 자원이 부족하여 response time이 길어지는 경우에 사용자의 요청이 실패가 된다. 따라서, 이에 대한 보완책으로 자동화된 확장 기능을 적용하고자 한다.
### Auto Scale-Out 설정
ticket 서비스의 deployment.yml 파일 수정 (deployment_hpa.yml)

        resources:
          limits:
            cpu: 500m
          requests:
            cpu: 200m
	    
Auto Scale 설정
replica를 동적으로 늘려주도록 HPA를 설정한다. 설정은 CPU 사용량이 15프로를 넘어서면 replica를 10개까지 늘려준다.
```
kubectl autoscale deployment ticket --cpu-percent=15 --min=1 --max=10 -n eticket
```
* CB에서 했던 방식대로 워크로드를 걸어준다.
- 부하 테스터 siege툴을 통한 서킷 브레이커 동작확인 : 동시 사용자 100명, 60초 동안 실시
```shell
##초기 데이터 등록
http POST http://52.231.95.4:8080/tickets ticketId=1 status=예약가능 starttime=2021-07-06 endtime=2021-07-06

kubectl exec -it pod/siege -c siege -n eticket -- /bin/bash
$ siege -c100 -t60S -r10 -v --content-type "application/json" 'http://reservation:8080/reservations POST {"ticketId":"1"}'
```

- Scale out 확인
* before

![image](https://user-images.githubusercontent.com/36217195/123725745-984ad800-d8c9-11eb-8de9-2e961810461b.png)

* after

![image](https://user-images.githubusercontent.com/36217195/123726401-dd233e80-d8ca-11eb-8dca-8ecc799ddb98.png)



## Zero-downtime deploy (readiness probe)

- readiness 설정은 앱의 무정지 배포가 가능하게 한다.
- ticket 서비스에서 readiness 설정을 제거한 yml 파일(deployment_no_readiness.yml)로 deploy 생성 후, siege 부하 테스트 실행해둔 뒤 상태에서 다른 버전을 앱을 재배포 했을 때, 오류 발생 여부를 알아본다. 

```shell
kubectl create -f ./kubernetes/deployment_no_readiness.yml -n eticket
#초기데이터 설정
http POST http://52.231.95.4:8080/tickets ticketId=1 status=예약가능 starttime=2021-07-06 endtime=2021-07-06

#siege 테스트 
kubectl exec -it pod/siege -c siege -n eticket -- /bin/bash
siege -c1 -t180S -r1 -v --content-type "application/json" 'http://reservation:8080/reservations POST {"ticketId":"1"}'

# app 새버전으로의 배포 시작 (두 개 버전으로 버전 바꿔가면서 테스트)
kubectl set image deployment ticket ticket=genie.azurecr.io/ticket:v4 -n eticket
kubectl set image deployment ticket ticket=genie.azurecr.io/ticket:v5 -n eticket
```

* readiness 미적용 경우
- ContainerCreating 상태의 pod도 호출되어서, 요청이 오류를 반환하였다.

![image](https://user-images.githubusercontent.com/36217195/123740310-2da69600-d8e3-11eb-9ddf-3babbd409c66.png)



* readiness 적용한 경우
- 컨테이너가 생성 완료된 pod만 호출되도록 보장하여, 구/신 버전이 동시에 존재하지만 오류를 발생시키지는 않는다. 

![image](https://user-images.githubusercontent.com/36217195/123739455-a86eb180-d8e1-11eb-9fdf-e9b894d6aa51.png)





## Self-healing (Liveness Probe)

- deployment_test_liveness.yml 에 Liveness Probe 옵션을 아래와 같이 수정하여 적용

![image](https://user-images.githubusercontent.com/36217195/123746494-aeb65b00-d8ec-11eb-9258-2573c4674761.png)

```
kubectl apply -f ./kubernetes/deployment.yml -n eticket
kubectl apply -f ./kubernetes/deployment_test_liveness.yml -n eticket
```

- ticket 서비스의 liveness가 발동되어 약 14분간 6번의 retry 시도 한 부분 확인


![image](https://user-images.githubusercontent.com/36217195/123746750-06ed5d00-d8ed-11eb-9c47-c2d9e3b88790.png)





