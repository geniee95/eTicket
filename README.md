![image](https://user-images.githubusercontent.com/82796103/120915696-27f0d280-c6e0-11eb-8c50-ef2441ddd473.png)


# 서비스시나리오

기능적 요구사항

1. 관리자는 티켓을 등록한다. 
2. 티켓이 등록되면, 티켓의 기본 가격이 할당되고 정책에 따라 할인가격이 적용된다. 
3. 사용자는 티켓을 예약한다. 
4. 티켓의 상태가 '예약가능'이고, 만료일이 경과하지 않았을 때 예약 가능하고, 티켓의 상태는 '예약됨'으로 변경된다. 
5. 사용자 등급이 'VIP'일때는 추가 할인 혜택이 적용된다. 
6. 사용자는 티켓 예약을 취소할 수 있다. 
7. 관리자와 사용자는 티켓 정보, 예약현황, 가격정보를 조회할 수 있다. 
(결재는 본 시나리오에 적용하지 않음)

비기능적 요구사항

1. 트랜잭션
i. 티켓 예약 전에 티켓의 상태가 '예약가능'이고, 만료일이 경과하지 않았는지를 반드시 확인한다. -> Synch 호출
ii. 티켓 예약을 취소하면 티켓의 상태가 '예약가능'으로 변경되고, 사용자 등급에 따라 적용된 추가 할인 정책이 원상복구 되어야 한다.  --> SAGA, 보상 트랜잭션

2. 장애격리
i. 티켓관리(ticket) 서비스가 과중되면, 예약(reservation)을 잠시 후에 하도록 유도한다. --> Circuit breaker, fallback
ii. 가격정책(pricing) 서비스가 수행되지 않더라도 365일 24시간 티켓예약을 취소할 수 있어야 한다. --> Asynch(event-driven), Eventual Consistency

3. 성능
i. 사용자와 관리자가 티켓 정보, 예약현황, 가격정보 조회시 성능을 고려하여 별도의 view로 구성한다. --> CQRS


# 체크포인트

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
 
 
# 분석/설계

## Event Storming 결과
* MSAEz 로 모델링한 이벤트스토밍 결과:  http://www.msaez.io/#/storming/mD3qVoL8dKTd723pyildzLwnvQq2/e90de0f65fb6a78f77f6412a03d85d24


### Event 도출

![image](https://user-images.githubusercontent.com/36217195/123536311-826fd280-d764-11eb-8816-0980f1ef8f07.png)




### 부적격 Event 탈락

![image](https://user-images.githubusercontent.com/36217195/123536406-15107180-d765-11eb-8bf1-bd3f1a18f9d1.png)

- 중복되거나 잘못된 도메인 이벤트들을 걸러내는 작업을 수행함
- 현업이 사용하는 용어를 그대로 사용(Ubiquitous Language)


### Actor, Command 부착

![image](https://user-images.githubusercontent.com/36217195/123536612-5a816e80-d766-11eb-9c4a-4bacd895a146.png)




### Aggregate 로 묶기

![image](https://user-images.githubusercontent.com/36217195/123536708-eeebd100-d766-11eb-9078-926f85142462.png)



### Bounded Context로 묶기

![image](https://user-images.githubusercontent.com/36217195/123536851-9e28a800-d767-11eb-8c6f-c02af09d575d.png)



### Policy 부착/이동 및 Context Mapping (괄호는 수행주체)

![image](https://user-images.githubusercontent.com/36217195/123537225-4d19b380-d769-11eb-9dde-41bfa940ca4e.png)


### 완성된 모형

![image](https://user-images.githubusercontent.com/36217195/123535964-87338700-d762-11eb-98e6-58e569d92806.png)


### 완성본에 대한 기능적/비기능적 요구사항을 커버하는지 검증

![image](https://user-images.githubusercontent.com/36217195/123537839-90295600-d76c-11eb-8a61-f1c1d26dd95e.png)


![image](https://user-images.githubusercontent.com/36217195/123537855-a59e8000-d76c-11eb-9c0a-a1c78a5e13bd.png)


![image](https://user-images.githubusercontent.com/36217195/123537887-d383c480-d76c-11eb-9342-9055172240cd.png)



![image](https://user-images.githubusercontent.com/36217195/123537943-20679b00-d76d-11eb-92fb-4dee049377c7.png)



### 비기능 요구사항에 대한 검증

![image](https://user-images.githubusercontent.com/36217195/123538161-62451100-d76e-11eb-8c71-167696c19d6a.png)



## 헥사고날 아키텍처 다이어그램 도출

![image](https://user-images.githubusercontent.com/36217195/123538459-febbe300-d76f-11eb-9cf5-ca71034d0bde.png)



# 구현

분석/설계 단계에서 도출된 헥사고날 아키텍처에 따라, 구현한 각 서비스의 실행방법은 아래와 같다.
(포트넘버 : 8081 ~ 8085, 8088)

    cd rent
    mvn spring-boot:run  

    cd bike
    mvn spring-boot:run

    cd billing
    mvn spring-boot:run 

    cd userDeposit
    mvn spring-boot:run  

    cd rentAndBillingView
    mvn spring-boot:run
    
    cd gateway
    mvn spring-boot:run
    
## Gateway 적용
API GateWay를 통하여 마이크로 서비스들의 집입점을 통일할 수 있다. 다음과 같이 Gateay를 적용하였다.
    
	server:
	port: 8088
	
	---
	
	spring:
	profiles: default
	cloud:
		gateway:
		routes:
			- id: rent
			uri: http://localhost:8081
			predicates:
				- Path=/rents/** 
			- id: bike
			uri: http://localhost:8082
			predicates:
				- Path=/bikes/** 
			- id: billing
			uri: http://localhost:8083
			predicates:
				- Path=/billings/** 
			- id: userDeposit
			uri: http://localhost:8084
			predicates:
				- Path=/userDeposits/** 
			- id: rentAndBillingView
			uri: http://localhost:8085
			predicates:
				- Path=/rentAndBillingView/**

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
			- id: rent
			uri: http://rent:8080
			predicates:
				- Path=/rents/** 
			- id: bike
			uri: http://bike:8080
			predicates:
				- Path=/bikes/** 
			- id: billing
			uri: http://billing:8080
			predicates:
				- Path=/billings/** 
			- id: userDeposit
			uri: http://userDeposit:8080
			predicates:
				- Path=/userDeposits/** 
			- id: rentAndBillingView
			uri: http://rentAndBillingView:8080
			predicates:
				- Path=/remtAmdBillingViews/**

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
    
    
    
    
    
## DDD 적용
MSAEZ.io를 통하여 도출된 Aggregate는 Entity로 선언하여 PRE/POST PERSIST/UPDATE/DELETE 반영하였으며, Repository Pattern을 적용하여 ACID를 구현하였다.

### Rent 서비스의 Rent.java

	package gbike;

	import javax.persistence.*;

	import org.springframework.beans.BeanUtils;
	import gbike.external.BikeService;

	import java.util.Date;

	@Entity
	@Table(name = "Rent_table")
	public class Rent {

	    @Id
	    @GeneratedValue(strategy = GenerationType.AUTO)
	    private Long rentid;
	    private Long userid;
	    private Long bikeid;
	    private String status;
	    private Date starttime;
	    private Date endtime;
	    private String endlocation;

	    private static final String STATUS_RENTED = "rented";
	    private static final String STATUS_RETURNED = "returned";

	    @PrePersist
	    public void onPrePersist() throws Exception {
		//bike가 사용가능 상태인지 확인한다.
		boolean result = RentApplication.applicationContext.getBean(gbike.external.BikeService.class)
			.chkAndUpdateStatus(this.getBikeid());
		System.out.println("bike.chkAndUpdateStatus --------  " + result);
		if (result) {
		    //bike가 사용가능 상태이므로, rent에 저장할 값을 set 한다. 
		    this.starttime = new Date(System.currentTimeMillis());
		    this.status = STATUS_RENTED;
		    System.out.println("onPrePersist .... ");
		} else {
		    throw new Exception(" 자전거는 대여할 수 없는 상태입니다. " + this.getBikeid());
		}
	    }

	    @PostPersist
	    public void onPostPersist() {
		//Rent를 저장했으므로, Rented 이벤트를 pub 한다. 
		System.out.println("onPostPersist ....  rentid :: " + this.rentid);
		Rented rented = new Rented();
		BeanUtils.copyProperties(this, rented);
		rented.publishAfterCommit();
	    }

	    @PreUpdate
	    public void onPreUpdate() {
		//Returned로 업데이트 할 때 저장할 값을 set 한다. 
		System.out.println("onPreUpdate .... ");
		this.endtime = new Date(System.currentTimeMillis());
		this.status = STATUS_RETURNED;
	    }

	    @PostUpdate
	    public void onPostUpdate() {
		//Rent를 returned 상태로 저장했으므로, Returned 이벤트를 pub 한다. 
		System.out.println("onPostUpdate .... ");
		Returned returned = new Returned();
		BeanUtils.copyProperties(this, returned);
		returned.publishAfterCommit();
	    }

	    public Long getRentid() {
		return rentid;
	    }

	    public void setRentid(Long rentid) {
		this.rentid = rentid;
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

	    public String getEndlocation() {
		return endlocation;
	    }

	    public void setEndlocation(String endlocation) {
		this.endlocation = endlocation;
	    }

	    public Long getUserid() {
		return userid;
	    }

	    public void setUserid(Long userid) {
		this.userid = userid;
	    }

	    public Long getBikeid() {
		return bikeid;
	    }

	    public void setBikeid(Long bikeid) {
		this.bikeid = bikeid;
	    }


	}


### Bike 서비스의 PolicyHandler.java

	package gbike;

	import gbike.config.kafka.KafkaProcessor;
	import com.fasterxml.jackson.databind.DeserializationFeature;
	import com.fasterxml.jackson.databind.ObjectMapper;
	import org.springframework.beans.factory.annotation.Autowired;
	import org.springframework.cloud.stream.annotation.StreamListener;
	import org.springframework.messaging.handler.annotation.Payload;
	import org.springframework.stereotype.Service;

	@Service
	public class PolicyHandler{
		@Autowired BikeRepository bikeRepository;

		@StreamListener(KafkaProcessor.INPUT)
		public void wheneverReturned_UpdateStatusAndLoc(@Payload Returned returned){

			if(returned.isMe()){
				
				Bike bike = bikeRepository.findByBikeid(Long.valueOf(returned.getBikeid()));
				
				//bike.setStatus(returned.getStatus());
				bike.setStatus("사용가능");
				bike.setLocation(returned.getEndlocation());
				
				bikeRepository.save(bike);
			}
				
		}

		@StreamListener(KafkaProcessor.INPUT)
		public void whatever(@Payload String eventString){}


	}



## 시나리오 검증

## 1. 초기데이터 구축
초기 데이터는 아래와 같이 정의하였다.

    [UserDeposit 등록]
    http POST http://20.194.44.70:8080/userDeposits userid=1 deposit=100000
    http POST http://20.194.44.70:8080/userDeposits userid=2 deposit=200000
    http POST http://20.194.44.70:8080/userDeposits userid=3 deposit=200000
    
    [UserDeposit 확인]
    http GET http://20.194.44.70:8080/userDeposits
 
 ![image](https://user-images.githubusercontent.com/84724396/121111293-9309e880-c849-11eb-8e74-9263cf46e734.png)


    [Bike 등록]
    http POST http://20.194.44.70:8080/bikes bikeid=1 status=사용가능 location=분당_정자역_1구역
    http POST http://20.194.44.70:8080/bikes bikeid=2 status=사용중 location=분당_정자역_1구역
    http POST http://20.194.44.70:8080/bikes bikeid=3 status=불량 location=분당_정자역_1구역
    
    [Bike 확인]
    http GET http://20.194.44.70:8080/bikes
 ![image](https://user-images.githubusercontent.com/84724396/121111431-d1070c80-c849-11eb-9de0-7e625c5a7c42.png)

 

타 마이크로서비스의 데이터 원본에 접근없이(Composite 서비스나 조인SQL 등 없이) 도 조회가 가능하도록 rentAndBillingView 서비스의 CQRS를 통하여 Costomer Center 서비스를 구현하였다.
rentAndBillingView View를 통하여 사용자가 rental한 bike 정보와 billing 정보를 조회할 수 있으며 반납 후 billing 상태를 확인할 수 있다. 

### 2.자전거 대여
### 2.1 대여(rent) 화면
    http POST http://20.194.44.70:8080/rents userid=1 bikeid=1
    
![image](https://user-images.githubusercontent.com/84724396/121114074-0e6d9900-c84e-11eb-970c-82c39fa6350d.png)

### 2.2 대여(rent) 후 bikes 화면
     자전거 상태가 '사용 가능' -> '사용중' 으로 변경된다.     
     http GET http://20.194.44.70:8080/bikes

![사용중](https://user-images.githubusercontent.com/84724396/121121127-fd2a8980-c859-11eb-9955-54988c8b331e.PNG)
    
### 2.3 대여(rent) 후 billings 화면
    bill이 하나 생성된다.
    http GET http://20.194.44.70:8080/billings

![image](https://user-images.githubusercontent.com/84724396/121126700-901bf180-c863-11eb-9b92-22cc6d227ff4.png)


### 2.4 대여(rent) 후 rentAndBillingView 화면(CQRS)
    rent한 정보를 조회할 수 있다.
    http GET http://20.194.44.70:8080/rentAndBillingViews

![image](https://user-images.githubusercontent.com/84724396/121114171-34933900-c84e-11eb-98b6-b02faf2e5b6b.png)

### 3. 반납(return)  
### 3.1 반납(return) 화면
     http PATCH http://20.194.44.70:8080/rents/1 endlocation=분당_정자역_3구역

![image](https://user-images.githubusercontent.com/84724396/121116196-1b3fbc00-c851-11eb-8a4c-8cd4820edda7.png)

### 3.2 반납(return) 후 bike 화면
     자전거 상태가 '사용중' -> '사용 가능'으로 변경된다.
     http GET http://20.194.44.70:8080/bikes
    
![사용가능](https://user-images.githubusercontent.com/84724396/121120558-c30cb800-c858-11eb-96a5-c8c54c9945b5.PNG)


### 3.3 반납(return) 후 userDeposit 화면
    요금이 계산되어 deposit이 차감된다.
    http GET http://20.194.44.70:8080/userDeposits

![image](https://user-images.githubusercontent.com/84724396/121115821-8b017700-c850-11eb-8c05-02510b6189af.png)


### 3.4 반납(return) 후 bill 화면
    bill 이 종료된다.
    http GET http://20.194.44.70:8080/billings
    
![image](https://user-images.githubusercontent.com/84724396/121126749-a3c75800-c863-11eb-84d2-bab86df2299f.png)


### 3.5 반납(return) 후 rentAndBillingView 화면(CQRS)
    rent와 billing 정보를 조회한다.
    http GET http://20.194.44.70:8080/rentAndBillingViews

![image](https://user-images.githubusercontent.com/84724396/121115890-a66c8200-c850-11eb-8d7d-bd73289a35e5.png)


### 4. 자전거 대여 불가 화면 (Request / Response)

     1. rent 신청를 하면 bike에서 자전거 상태를 체크하고 '사용 가능'일 때만 rent 가 성공한다.    
     http POST http://20.194.44.70:8080/rents bikeid=2 userid=2

![image](https://user-images.githubusercontent.com/84724396/121115300-e2ebae00-c84f-11eb-9266-8c05b0a3f2d3.png)


![image](https://user-images.githubusercontent.com/84724396/121115456-10d0f280-c850-11eb-9377-c33ef6e31514.png)

      2. 자전거 생태 체크를 하는 bike 서비스를 내리고 rent 신청을 하면 자전거 생태 체크를 할 수 없어 rent를 할 수 없다.

![오류1](https://user-images.githubusercontent.com/84724396/121119465-a3749000-c856-11eb-8772-f00832f5c3fd.PNG)


    위와 같이 Rent -> Bike -> Return -> Billing -> userDeposit 순으로 Sequence Flow 가 정상동작하는 것을 확인할 수 있다.
    (대여불가 자전거는 예외)

    대여 후 Status가 "사용중"으로, 반납하면 Status가 "사용가능"으로 Update 되는 것을 볼 수 있으며 반납이후 사용자의 예치금은 정산 후 차감된다.

    또한 Correlation을 key를 활용하여 userid, rentid, bikeid, billid 등 원하는 값을 서비스간의 I/F를 통하여 서비스 간에 트랜잭션이 묶여 있음을 알 수 있다.



## Polyglot 프로그래밍 적용

rent 서비스와 기타 bike, billing, bikeDepository 등 서비스는  다른 DB를 사용하여 폴리글랏을 만족시키고 있다.

### rent의 pom.xml DB 설정 코드

  ![image](https://user-images.githubusercontent.com/82796103/120737666-73ad4b80-c529-11eb-828e-f3089b929ca9.png)

### 기타 서비스의 pom.xml DB 설정 코드

  ![image](https://user-images.githubusercontent.com/82796103/120737496-1dd8a380-c529-11eb-907a-7a8b1a3a8bcd.png)



# 운영
## namespace 생성
	  kubectl create ns gbike

## Deploy / Pipeline
### git에서 소스 가져오기
	git clone https:/github.com/skcc-1st-team/gbike.git

### Build 하기

	cd /home/project/gbike/bike
	mvn clean
	mvn compile
	mvn package

	cd /home/project/gbike/billing
	mvn clean
	mvn compile
	mvn package

	cd /home/project/gbike/rent
	mvn clean
	mvn compile
	mvn package

	cd /home/project/gbike/rentAndBillingView
	mvn clean
	mvn compile
	mvn package

	cd /home/project/gbike/userDeposit
	mvn clean
	mvn compile
	mvn package

	cd /home/project/gbike/gateway
	mvn clean
	mvn compile
	mvn package

### Docker Image Push/deploy/서비스생성

	cd /home/project/gbike/bike
	az acr build --registry skcc1team --image skcc1team.azurecr.io/bike:latest .
	kubectl create deploy bike --image=skcc1team.azurecr.io/bike:latest -n gbike
	kubectl expose deploy bike --type=ClusterIP --port=8080 -n gbike

	cd /home/project/gbike/billing
	az acr build --registry skcc1team --image skcc1team.azurecr.io/billing:latest .
	kubectl create deploy billing --image=skcc1team.azurecr.io/billing:latest -n gbike
	kubectl expose deploy billing --type=ClusterIP --port=8080 -n gbike

	cd /home/project/gbike/rent
	az acr build --registry skcc1team --image skcc1team.azurecr.io/rent:latest .
	kubectl create deploy rent --image=skcc1team.azurecr.io/rent:latest -n gbike
	kubectl expose deploy rent --type=ClusterIP --port=8080 -n gbike

	cd /home/project/gbike/rentAndBillingView
	az acr build --registry skcc1team --image skcc1team.azurecr.io/rentandbillingview:latest .
	kubectl create deploy rentandbillingview --image=skcc1team.azurecr.io/rentandbillingview:latest -n gbike
	kubectl expose deploy rentandbillingview --type=ClusterIP --port=8080 -n gbike

	cd /home/project/gbike/userDeposit
	az acr build --registry skcc1team --image skcc1team.azurecr.io/userdeposit:latest .
	kubectl create deploy userdeposit --image=skcc1team.azurecr.io/userdeposit:latest -n gbike
	kubectl expose deploy userdeposit --type=ClusterIP --port=8080 -n gbike

	cd /home/project/gbike/gateway
	az acr build --registry skcc1team --image skcc1team.azurecr.io/gateway:latest .
	kubectl create deploy gateway --image=skcc1team.azurecr.io/gateway:latest -n gbike
	kubectl expose deploy gateway --type=LoadBalancer --port=8080 -n gbike

### yml파일 이용한 deploy

	cd /home/project/gbike/rent
	kubectl apply -f ./kubernetes/deployment.yml -n gbike

- deployment.yml 파일

![image](https://user-images.githubusercontent.com/82796103/121019311-43d89f00-c7da-11eb-8744-7c42d81baca4.png)


### Deploy 완료

![image](https://user-images.githubusercontent.com/82796103/121105067-479e0d00-c83e-11eb-93a6-4a051d7eb45f.png)



## ConfigMap
	시스템별로 변경 가능성이 있는 설정들을 ConfigMap을 사용하여 관리한다.
	
### application.yml 파일에 ${api.url.bikeservice} 설정
![image](https://user-images.githubusercontent.com/82796103/121114706-1c6fe980-c84f-11eb-8e86-024a6e33a3e8.png)

![image](https://user-images.githubusercontent.com/82796103/121021504-6cfa2f00-c7dc-11eb-9269-528765e63ab1.png)

### deployment-config.yaml
![image](https://user-images.githubusercontent.com/82796103/121037602-8a35fa00-c7ea-11eb-889e-d8a03ae445b6.png)

### configMap 
![image](https://user-images.githubusercontent.com/82796103/121039821-61166900-c7ec-11eb-9c88-a9bb5221f924.png)


## Autoscale (HPA)

### 부하 테스트 siege Pod 설치
	kubectl apply -f - <<EOF
	apiVersion: v1
	kind: Pod
	metadata:
	  name: siege
	spec:
	  containers:
	    - name: siege
	    image: apexacme/siege-nginx
	EOF

### Auto Scale-Out 설정
deployment.yml 파일 수정

        resources:
          limits:
            cpu: 500m
          requests:
            cpu: 200m
	    
Auto Scale 설정

	kubectl autoscale deployment bike --cpu-percent=20 --min=1 --max=3 -n gbike

### Auto Scale Out 발동 확인

- 부하 시작 (siege) : 동시접속 100명, 120초 동안 
	
	siege -c100 -t120S -v http://20.194.44.70:8080/bikes

![autoscale1](https://user-images.githubusercontent.com/82795748/121107122-55559180-c842-11eb-8542-bbfef1463584.jpg)

- Scale out 확인

![autoscale2](https://user-images.githubusercontent.com/82795748/121107303-a4032b80-c842-11eb-958c-a64e98bda3ce.jpg)

![autoscale3](https://user-images.githubusercontent.com/82795748/121107154-643c4400-c842-11eb-9033-69c1a3114eb2.jpg)

## Self-healing (Liveness Probe)

- userdeposit 서비스 정상 확인

![liveness1](https://user-images.githubusercontent.com/84724396/121038124-fdd80700-c7ea-11eb-9063-ce9360b36278.PNG)


- deployment.yml 에 Liveness Probe 옵션 추가
```
cd ~/gbike/userDeposit
vi deployment.yml

(아래 설정 변경)
          livenessProbe:
            httpGet:
              path: '/actuator/health'
              port: 8081
            initialDelaySeconds: 3
            periodSeconds: 5
```

![liveness43](https://user-images.githubusercontent.com/84724396/121042427-a471d700-c7ee-11eb-9140-3e59ac801fed.PNG)



- gbike pod에 liveness가 적용된 부분 확인

  kubectl describe deploy userdeposit -n gbike

![liveness42](https://user-images.githubusercontent.com/84724396/121044305-65448580-c7f0-11eb-9d1a-29b4b0118904.PNG)


- userdeposit 서비스의 liveness가 발동되어 2번 retry 시도 한 부분 확인

![image](https://user-images.githubusercontent.com/84724396/121130881-fa379500-c869-11eb-9921-b24701660a72.png)


# Circuit Breaker

- 서킷 브레이킹 프레임워크의 선택 : Spring FeignClient + Hystrix 옵션을 사용하여 구현함

- Hystrix를 설정 : 요청처리 쓰레드에서 처리시간이 1200 밀리가 넘어서기 시작하여 어느정도 유지되면 CB 회로가 닫히도록(요청을 빠르게 실패처리, 차단) 설정

- 동기 호출 주체인 Rent 서비스에 Hystrix 설정

- rent/src/main/resources/application.yml 파일

```
	feign:
	  hystrix:
		enabled: true
	hystrix:
	  command:
		default:
		  execution.isolation.thread.timeoutInMilliseconds: 1200
```

- 부하에 대한 지연시간 발생코드 BikeController.java 지연 적용

![circuit](https://user-images.githubusercontent.com/82795748/121125003-c9069700-c860-11eb-9a4f-1ffb5e20a550.jpg)

- 부하 테스터 siege툴을 통한 서킷 브레이커 동작확인 : 동시 사용자 5명, 10초 동안 실시

	siege -c5 -t10S -r10 -v --content-type "application/json" 'http://20.194.44.70:8080/rents POST {"bikeid": "1", "userid": "1"}'

- 결과

![image](https://user-images.githubusercontent.com/82796103/121124344-b9d31980-c85f-11eb-9d9b-2778f3fcb06a.png)

![image](https://user-images.githubusercontent.com/82796103/121125220-2995d400-c861-11eb-96ef-01f771097e2e.png)


## Zero-downtime deploy (readiness probe)

- readiness 옵션 제거 후 배포 - 신규 Pod 생성 시 downtime 발생

![image](https://user-images.githubusercontent.com/82795726/121106857-d06a7800-c841-11eb-85cd-d7ad08ff62db.png)

- readiness 옵션 추가하여 배포

![image](https://user-images.githubusercontent.com/82795726/121106445-fc392e00-c840-11eb-9b8c-b413ef06b95e.png)

![image](https://user-images.githubusercontent.com/82795726/121106524-225ece00-c841-11eb-9953-2febeab82108.png)

- Pod Describe에 Readiness 설정 확인

![image](https://user-images.githubusercontent.com/82795726/121110068-a61bb900-c847-11eb-9229-63701496846a.png)

- 기존 버전과 새 버전의  pod 공존

![image](https://user-images.githubusercontent.com/82795726/121109942-6e147600-c847-11eb-9dae-9dfce13e8c62.png)
