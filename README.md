<h1>찰떡딜</h1>
</div>
<p align="center">
  <img src="https://github.com/user-attachments/assets/d0d4ffcf-e918-4b7f-bb4f-46c990427674" width="150">
</p>

<p align="center">
  중고 상품을 좋아하는 이들이 자신의 중고 상품을 경매를 통해 거래하고, 경제적인 소비 생활을 할 수 있는
</p>

<p align="center">
  <strong>중고 거래 경매 플랫폼</strong>
</p>

<p align="center">
  입니다.
</p>
<br>

# 🔍 프로젝트 개요
- **기간** : 2024.11.18 ~ 2024.12.27
- **프로젝트 명** : 찰떡딜
- **팀원** : 홍민우, 박준영
- **워크 스페이스** : https://www.notion.so/15842bcefb368024b446ee640b473da6
<br>
  
# 🛠️ 기술 스택
- **Java 17**
- **Spring Boot**
- **Spring Security**
- **Validation**
- **JWT**
- **OAuth 2.0**
- **JPA**
- **H2**
- **MySQL**
- **Postman**
- **TaskScheduler**
- **Websocket**
- **Redis**
- **QueryDsl**
<br>

# 📚 기술 스택 선정 이유

<h2>1. Tasksheduler</h2>

이번 프로젝트에서 가장 중요한 서비스는 '**경매**'였습니다.<br>
유저는 경매에 참여하기 위해 입찰을 진행하며, 경매는 **시작일**과 **종료일**이 정해져 있습니다.<br>
하지만 **상품 판매자**가 직접 경매 시작일에 맞춰 **상품을 등록**하고, 종료일에 맞춰 **경매를 종료**해야 한다면, 이는 판매자에게 **번거롭고 불편하게** 느껴질 수 있습니다.<br>
이러한 문제를 해결하기 위해 **서버에서 동적**으로 경매 시작일과 종료일에 맞춰 서비스를 **자동으로 제공**하도록 구현했습니다.<br>
이를 위해 ‘**TaskScheduler**’를 활용하여 **경매 시작과 종료 작업을 효율적**으로 관리했습니다.<br>

<br>
<h2>2. Websocket</h2>

상품 판매자가 경매 등록과 종료를 완료했다면, 이제 남은 것은 **입찰자의 참여**입니다.<br>
하나의 상품에는 **다수의 유저가 동시에 입찰**을 **진행**할 수 있습니다.<br>
한 유저가 **입찰을 진행**했을 경우, 해당 상품의 입찰에 참여 중인 **모든 유저는 실시간으로 입찰 정보를 전달**받아야 합니다.<br>
이러한 문제를 해결하기 위해 **WebSocket** 기술을 채택했습니다.<br>
**WebSocket**을 활용하여 유저 간 **양방향 통신**을 구현함으로써, 입찰이 발생할 때마다 관련 정보를 **실시간으로 송수신**할 수 있도록 했습니다.<br>
이를 통해 모든 입찰 참여자가 동일한 정보를 빠르게 공유받아 **원활한 경매 진행이 가능**하도록 지원했습니다.

<br>
<h2>3. Redis</h2>

**입찰 시스템의 효율성**을 높이고 **DB의 부하를 줄이기 위해** **Redis**를 활용했습니다.
**Redis**는 경량의 **인메모리 데이터베이스**입니다.<br>
**빠른 데이터 읽기 및 쓰기 속도**를 제공하며, 실시간 데이터 처리가 중요한 입찰 시스템에 적합합니다.<br>
입찰이 발생할 때, **DB에 직접 접근하는 대신 Redis에 입찰 정보를 기록**하고, **Redis에서 데이터를 조회하는 방식**을 채택했습니다.<br>
이를 통해 다수의 유저가 동시에 입찰에 참여하더라도 **DB의 부하를 최소화**하고, **입찰 처리 속도를 개선**할 수 있었습니다.<br>

<br>

<h2>4. Querydsl</h2>

**동적 쿼리 생성**을 위해 **QueryDSL**을 활용하여 조건에 맞는 데이터 조회를 유연하게 처리했습니다.<br>
또, **N+1 문제**를 해결하기 위해 **fetchJoin**을 사용하여 연관된 엔티티들을 **한 번의 쿼리로 효율적으로 조회**하도록 최적화했습니다.<br>
이를 통해 데이터 조회 성능을 극대화하고, 불필요한 쿼리 실행을 방지하여 시스템 성능을 향상시켰습니다

<br>

# 📱 와이어 프레임
<h2>회원가입 및 로그인</h2>
<img src="https://github.com/user-attachments/assets/e458d786-c525-4f51-9e5a-fe6ed86a1745" width="150">
<img src="https://github.com/user-attachments/assets/77c11915-05a7-48e1-805f-e18c5121c97f" width="150">
<br><br>

<h2>메인 화면 및 네비게이션 페이지</h2>
<img width="150" alt="메인 페이지@2x" src="https://github.com/user-attachments/assets/fe5f3688-0eb3-41ca-b702-e68f81bdb8bd" />
<img src="https://github.com/user-attachments/assets/cf6d252d-8eb1-45bb-a0a1-2d4930bbfaa1" width="150">
<img src="https://github.com/user-attachments/assets/eade2977-86ea-45d9-b647-39b1d0f39afa" width="150">
<img src="https://github.com/user-attachments/assets/14afda91-2762-4e10-a74d-67f36f371a1b" width="150">
<img src="https://github.com/user-attachments/assets/9a8ff2b3-6fcb-4c22-8445-f63d530730d9" width="150">
<br><br>
<img src="https://github.com/user-attachments/assets/c1e2bb81-ab9d-49a0-b1a2-a6da167e3399" width="150">
<img src="https://github.com/user-attachments/assets/84fbfbef-faa1-423f-9b94-5da94ac6b31b" width="150">
<br><br>

<h2>마이페이지</h2>
<img src="https://github.com/user-attachments/assets/80d80d18-aee4-47b5-9c7b-909dacaba387" width="150">
<img src="https://github.com/user-attachments/assets/b48d766a-c199-4726-ace5-772e09ba4d34" width="150">
<img src="https://github.com/user-attachments/assets/c1fb73fc-7b6f-40fb-a22d-c14c5d5f0e0b" width="150">
<img src="https://github.com/user-attachments/assets/57f0b9e5-e9f3-4ee7-8a0e-ed9f11211c07" width="150">
<img src="https://github.com/user-attachments/assets/2205de1e-dcde-4789-b48d-0fc532e8d170" width="150">
<br><br>

<h2>상품 조회페이지</h2>
<img src="https://github.com/user-attachments/assets/72b057bd-0b6d-497b-ac08-88528b154d0f" width="150">
<img src="https://github.com/user-attachments/assets/bfa598c9-eea4-4998-aec5-729f1dd09ef0" width="150">
<img src="https://github.com/user-attachments/assets/5f9eb093-cc76-4331-a256-d608efd9e0af" width="150">
<img src="https://github.com/user-attachments/assets/db880831-a1ff-4be9-82d0-02df28f54aee" width="150">
<img src="https://github.com/user-attachments/assets/d621d7d8-f61b-44c1-8321-d13bbc102248" width="150">
<br><br>
<img src="https://github.com/user-attachments/assets/1d061e53-0124-4b06-9aca-2dfa5d58252a" width="150">
<img src="https://github.com/user-attachments/assets/7c7c09a9-9425-4ed4-9120-2e67f882adaa" width="150">
<br><br>

# 📔 주요기능
<h2>🙇🏻‍♂️ 1. 회원 기능</h2>

- 회원가입 및 로그인
  - 사용자는 소셜 로그인을 통해 회원가입과 로그인을 간편하게 할 수 있습니다.
  - 또한, 사용자는 이메일로 회원가입을 할 수 있습니다.
  - 이메일로 회원가입을 진행 시에는, 해당 이메일에 발송된 코드를 인증해야만 회원가입이 가능합니다.
  - 회원가입 시, 비밀번호는 암호화되어서 저장도비니다.
  - 로그인 시, AccessToken과 RefreshToken이 발급됩니다.

- JWT
  - AccessToken : 짧은 유효기간을 가지며(1시간), 사용자 요청을 인증하는 데 사용됩니다.
  - RefreshToken : 긴 유효기간을 가지며(일주일), AccessToken이 만료되었을 때, 새로운 토큰을 발급받기 위해 사용됩니다.

- 충전
  - 회원은 입찰을 위한 돈을 충전할 수 있습니다.
 
- 경매 내역 조회
  - 회원은 자신이 입찰한 경매 내역을 확인할 수 있습니다.
  - QueryDSL을 이용하여 조회 시에 발생하는 n + 1 문제를 해결했습니다.
  - 상품에 대한 간단한 정보를 확인할 수 있습니다.
  - 경매가 성공했는지 실패했는지 확인할 수 있습니다.
  - 낙찰된 금액을 확인할 수 있습니다.
  - 경매 상세 내역을 조회할 수 있습니다.
 
- 경매 상세 내역 조회
  - 판매자의 닉네임을 확인할 수 있습니다.
  - QueryDSL을 이용하여 조회 시에 발생하는 n + 1 문제를 해결했습니다.
  - 상품에 대한 자세한 정보를 알 수 있습니다.
  - 낙찰된 금액을 조회할 수 있습니다.
  - 해당 상품에 대한 입찰 정보와, 본인이 입찰한 내역을 확인할 수 있습니다.
    
- 판매 내역 조회
  - 회원은 자신이 판매한 판매 내역을 확인할 수 있습니다.
  - QueryDSL을 이용하여 조회 시에 발생하는 n + 1 문제를 해결했습니다.
  - 상품에 대한 간단한 정보를 확인할 수 있습니다.
  - 판매가 성공했는지 실패했는지 확인할 수 있습니다.
  - 판매된 금액을 확인할 수 있습니다.
  - 판매 상세 내역을 조회할 수 있습니다.

- 판매 상세 내역 조회
  - 낙찰자의 닉네임을 확인할 수 있습니다.
  - QueryDSL을 이용하여 조회 시에 발생하는 n + 1 문제를 해결했습니다.
  - 상품에 대한 자세한 정보를 알 수 있습니다.
  - 판매된 금액을 조회할 수 있습니다.
  - 해당 상품에 대한 입찰 정보와, 본인이 입찰한 내역을 확인할 수 있습니다.
 
  
<br>    
<h2>📦 2. 상품 기능</h2>

- 상품 등록
  - 사용자는 로그인을 했을 경우, 상품을 등록할 수 있습니다.
  - 상품을 등록할 때 사진을 1개 이상 업로드 해야됩니다.
  - 상품을 등록할 때 제목, 상품 설명, 시작 경매일, 종료 경매일, 카테고리 선택은 필수입니다.
  - 상품을 등록할 때 시작은 현재시간 이후여야 합니다.
  - 상품이 등록되면, 서버에서 자동으로 시작 경매일과 종료 경매일에 맞춰 경매를 진행합니다.

- 상품 삭제
  - 회원은 상품을 삭제할 수 있습니다.
  - productId를 입력받아 해당 상품을 삭제합니다.
    
- 상품 수정
  - 회원은 상품을 수정할 수 있습니다.
  - productId를 입력받아 해당 상품을 수정합니다.
  - 상품을 수정할 때 사진을 1개 이상 업로드 해야됩니다.
  - 상품을 수정할 때 제목, 상품 설명, 시작 경매일, 종료 경매일, 카테고리 선택은 필수입니다.
  - 상품을 수정할 때 시작은 현재시간 이후여야 합니다.

- 상품 조회
  - 누구나 상품을 조회할 수 있습니다.
  - QueryDSL을 이용하여 조회 시에 발생하는 n + 1 문제를 해결했습니다.
  - 메인 페이지에서 상품에 대한 간단한 정보를 확인할 수 있습니다.
  - 상품의 상세 정보를 조회할 수 있습니다.
  - 경매가 진행 전인 상품을 조회할 수 있습니다.
  - 경매가 진행 중인 상품을 조회할 수 있습니다.
  - 경매가 종료된 상품을 조회할 수 있습니다.
  - 일주일 간 낙찰된 금액 중 가장 높은 금액과 가장 낮은 금액을 조회할 수 있습니다.
  - 진행된 경매 중 입찰이 많이 이루어진 상품을 조회할 수 있습니다.

<br>
<h2>💸 3. 입찰 기능</h2>

- 경매 진행 전
  - 로그인을 하지 않으면 입찰은 불가능하지만 조회는 가능합니다.
  - 회원들은 경매가 시작되어야 입찰을 할 수 있습니다.
  - 몇 명의 회원이 상품을 조회하고 있는지 확인할 수 있습니다.
 
- 경매 진행 중
  - 서버에서 동적으로 경매가 시작됩니다.
  - 로그인을 하지 않으면 입찰은 불가능하지만 조회는 가능합니다.
  - 회원은 입찰 한계 금액을 등록하여 입찰 금액에 대한 한도를 설정할 수 있습니다.
  - 회원들은 입찰을 할 수 있습니다.
  - 상품 판매자는 입찰을 할 수 없습니다.
  - 입찰 금액은 경매 시작가 또는 현재 경매가보다 높아야 합니다.
  - 현재 잔액이 부족하면 입찰이 불가능합니다.
  - 현재 최고 입찰자가 본인일 경우, 추가 입찰은 불가능합니다.
  - 몇 명의 회원이 상품을 조회하고 있는지 확인할 수 있습니다.
 
- 경매 종료
  - 서버에서 동적으로 경매가 종료됩니다.
  - 경매 종료 시, 현재 연결된 웹소켓들이 종료됩니다.
  - 입찰에 성공한 입찰자는 금액을 지불하고, 낙찰이 결정됩니다.
  - 입찰에 실패한 입찰자들은 입찰 금액을 전액 반환받습니다.
  - 상품 판매자는 낙찰된 금액을 받게 됩니다.
  - 입찰 진행에 대한 정보는 저장됩니다.

<br>

# ⌛️ 경매 로직 흐름도

1. **상품 등록** :
- 유저(판매자)는 상품을 등록할 때, 원하는 **경매 시작일**과 **경매 종료일**을 설정합니다.
  
<br>

2. **스케줄러 등록** :
- 등록된 상품의 **경매 시작일**과 **경매 종료일** 정보를 **TaskScheduler**에 전달합니다.
- **TaskScheduler**는 해당 일정을 기반으로 자동으로 경매 시작 및 종료 작업을 예약합니다.
  
<br>

3. **멀티 쓰레드 대기 :**
- **TaskScheduler**는 3개의 쓰레드를 활용하여 상품의 **경매 시작일 및 종료일을** **관리**합니다.
- 각 쓰레드는 **경매 시작일**과 **종료일**을 지속적으로 모니터링하며 **대기 상태를 유지**합니다.
  
<br>

4. **경매 시작** :
- **경매 시작일**에 도달한 상품이 있을 경우, **TaskScheduler**는 **비동기**적으로 해당 상품의 경매를 시작합니다.
- 3개의 쓰레드를 **비동기적**으로 이용해 다수의 상품이 동일한 **경매 시작일**을 가지더라도 성능 저하 없이 **효율적으로 처리되도록 설계**되었습니다.

<div align="center"> 
  <img src="https://github.com/user-attachments/assets/eda8cdbd-2eae-4689-af3f-8c0e6dd55e38" width="50%" />
</div>

- 경매가 시작되면 해당 상품의 상태가 **TaskScheduler**에 의해 경매 상태가 **BEFORE**에서 **ONGOING**으로 변경됩니다.
- 경매가 시작될 때, **DB 부하**를 줄이기 위해 입찰 정보를 관리하는 **Redis** 인스턴스가 생성됩니다.
  
<br>

5. **경매 중** :
- 경매 시작 시, 경매 전 대기 중이던 유저들에게 **경매가 시작되었다**는 문구를 전송합니다.
- 유저가 경매에 입장할 경우, 해당 경매에 참여 중인 유저 수를 나타내는 **ViewCount**가 +1 증가하며, 나갈 경우 -1 감소하고 경매에 참여하는 유저들에게 조회중인 유저 수를 전송합니다.
- 경매 중인 상품의 실시간 입찰 상황은 **WebSocket**을 통해 클라이언트와 서버 간에 주고받습니다.
- 구매자가 입찰을 하게 되면 서버는 이를 검증 후 **WebSocket**으로 모든 참가자에게 **최신 입찰 정보를 전송**합니다.
- 입찰에 대한 모든 정보는 **Redis**에 저장되어 효율적으로 관리됩니다.
- 최고 입찰가가 변경될 때마다 실시간으로 반영됩니다.
  
<br>

6. **경매 종료** :
- 경매가 종료되면 해당 상품의 상태가 **TaskScheduler**에 의해 경매 상태가 **ONGOING**에서 **ENDED**로 변경됩니다.
- 경매 종료일에 도달한 상품은 **TaskScheduler**에 의해 동적으로 **종료**됩니다.
- **경매 종료** 시, 종료일이 도래한 경매들이 **비동기적**으로 동시에 마무리됩니다.
- **경매 종료 시**, 클라이언트에 경매가 종료되었다는 문구가 표시되며, 연결되어 있던 **WebSocket**이 해제됩니다.
- 종료된 경매의 **경매 정보**, **입찰 정보**, **판매 정보**는 **Redis**에서 조회한 후 데이터베이스(DB)에 저장됩니다. 이후, **Redis**에 저장된 해당 데이터는 **삭제**됩니다.
- **판매자**는 낙찰된 입찰 금액을 지급받고, **낙찰자**는 경매 금액을 지불하며 거래가 완료됩니다.
- **입찰에 실패한 사용자들**은 입찰에 사용한 금액 전액을 환불받습니다.
- 모든 금액 처리 로직은 **비동기적으로** 처리되기 때문에, 이 과정에서 데이터 **일관성**을 보장하기 위해 **@Lock**을 사용하여 **금액 반영 전 조회로 인한 오류를 방지**합니다.

<br><br>

# ❤️‍🩹 문제점 및 해결방안
이 프로젝트에서 몇 가지 문제점이 있었으나, 주어진 기간 내에 프로젝트를 완성하지 못했습니다.<br>
하지만 문제점을 숨기지 않고, 그에 대한 해결책을 명확히 제시하며 풀어나가겠습니다.

--- 

<h2>1. 서버가 종료되었을 경우</h2>

🔴 **문제점**<br>
서버가 종료되었을 경우, TaskScheduler에 의해 예약되었었던 작업들이 모두 중단됩니다.<br>
즉, 서버 종료 시 현재 대기 중인 작업이나 실행 중인 작업들이 모두 취소되며, 재시작 시 예약된 작업들이 복구되지 않습니다.<br>
이는 서버가 종료되면 예약된 작업이 다시 실행되지 않는 문제를 야기할 수 있습니다.<br>

🟢 **해결책**<br>
상품을 등록할 때, ProductRepository에 경매 시작일과 경매 종료일이 저장됩니다.<br>
서버의 실행 전 이벤트리스너의 어노테이션을 활용하여 Product의 경매 시작일과 경매 종료일을 조회한 후, 다시 백그라운드에 쓰레드들을 대기시킵니다.<br>

--- 

<h2>2. 입찰 방식의 문제점</h2>

🔴 **문제점**<br>
웹소켓의 세션을 기준으로 해당 유저의 최신 입찰 금액을 불러오는 방식에서 문제가 발생했습니다.<br>
아래와 같은 방식으로 입찰을 처리했지만, 같은 유저가 나갔다 들어올 경우 새로운 세션으로 인식되면서 이전 입찰 기록을 가져오지 못하는 오류가 있었습니다.

```
try {
    Long lastMoney = (Long) attributes.get("lastMoney");
    if (lastMoney == null) {
	lastMoney = 0L;
    }

    memberService.bidToAuction(member, dtoRequest.getBidAmount(), lastMoney);
    attributes.put("lastMoney", dtoRequest.getBidAmount());
} catch (MoneyException | NullPointerException e) {
    log.info("MoneyEx={}", e.getMessage());
    sendMessage(session, "잔액이 부족합니다.");
    return;
}
```
그로 인해 입찰금액 추적에 실패하게 되었고, 이를 통해 철회금액 로직에서도 추가적인 오류가 발생하는 문제를 일으켰습니다.

🟢 **해결책** <br>
이 문제를 해결하기 위해 Redis의 Hash를 활용하여 유저의 입찰 기록을 세션과 관계없이 지속적으로 관리하도록 변경합니다.<br>
이를 통해 유저가 나갔다가 다시 들어와도 Redis에서 이전 입찰 금액을 쉽게 불러올 수 있게 됩니다.<br>
이렇게 하면 세션 정보가 새로 생성되어도 유저의 입찰 내역을 정확히 추적할 수 있으며, 철회금액 로직에서도 오류를 방지할 수 있습니다.

--- 

<br><br>
# 🎯 트러블 슈팅

<h2>비동기와 트랜잭션</h2>

경매가 종료되면 판매자에게 낙찰 금액을 지급하고, 낙찰에 실패한 유저들에게는 입찰 금액을 환불해야 했습니다.<br>
최초 API 테스트에서는 하나의 경매 종료만 다뤄졌기 때문에 각 유저에게 환급이 제대로 이루어졌고, 버그가 발생하지 않았습니다.<br>
하지만 이후 여러 개의 경매가 동시에 종료되는 상황에서는 환급이 제대로 이루어지지 않는 문제를 발견했습니다.<br>
문제의 원인은 동시에 종료된 3개의 쓰레드가 비동기적으로 실행되면서, 각 유저의 잔액을 조회할 때 반영되지 않은 금액이 조회된다는 점이었습니다.

이를 해결하기 위한 두 가지 방법이 있었습니다.<br>
1. 동기적으로 하나씩 실행한다.<br>
2. 비동기적으로 실행하되, 잔액을 반영하기 전까지는 조회를 하지 못하게 한다.

처음에는 첫 번째 방법을 시도했으나, 동기적으로 실행할 경우 여러 경매가 동시에 종료될 때 시간이 많이 소요된다는 문제를 발견했습니다. 그래서 두 번째 방법을 적용하여 문제를 해결했습니다.

두번 째 방법 중 처음으로 시도했던 방법은 트랜잭션의 격리 수준을 높여서 해결할 수 있을 거라고 생각했습니다.<br>
@Transactional(isolation = Isolation.SERIALIZABLE)을 사용하여 성능은 저하될 수 있지만, 트랜잭션이 격리되면 경매 종료를 처리할 수 있을 거라 판단했습니다.<br>
하지만 각 쓰레드가 별도의 트랜잭션을 가지고 있기 때문에 이 방법으로는 문제를 해결할 수 없었습니다.

두 번째 방법으로는 @Lock을 이용해 Member를 조회할 때 락을 걸어, 다른 쓰레드가 중복된 Member를 조회할 수 없게 하려고 했습니다.

```
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT m FROM Member m WHERE m.id = :memberId")
Optional<Member> lockMemberForUpdate(Long memberId);
```

이렇게 락을 걸었을 때, 다른 쓰레드가 데이터를 반영할 때까지 조회를 차단할 수 있을 것이라 예상했습니다.<br>
하지만 오히려 org.springframework.dao.CannotAcquireLockException 예외가 발생했으며, 이는 데드락 상황에서 발생하는 오류였습니다.<br>
두 개 이상의 트랜잭션이 서로 자원을 기다리며 무한 대기 상태에 빠지는 문제였습니다.

이 문제를 생각해보니, 비동기적으로 쓰레드가 실행될 경우 다음과 같은 상황에서 데드락이 발생할 가능성이 있다고 판단했습니다.<br>
1번 쓰레드가 memberA를 조회한 후, memberB를 조회를 시도할 때,<br>
2번 쓰레드는 memberB를 조회한 후, memberC를 조회를 시도할 때,<br>
3번 쓰레드는 membeC를 조회한 후, memberA를 조회하기 위해 시도할 때, 각 쓰레드가 서로 다음 자원을 기다리며 데드락에 빠지고 있다고 생각했습니다.<br>
이 문제를 해결하기 위해 트랜잭션은 커밋될 때 락이 풀린다는 사실을 파악했고,<br>
현재 트랜잭션이 경매 종료라는 하나의 큰 트랜잭션 안에서 처리되고 있었기 때문에 락을 풀기위해 트랜잭션을 더 세분화해야 한다고 판단했습니다.

따라서, 최종적으로 금액관련 메서드를 새로운 트랜잭션으로 분리하여 처리하였습니다.

```
@Override
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void addMoney(Long memberId, Long amount) {
Member findMember = memberRepository.lockMemberForUpdate(memberId)
	.orElseThrow(() -> new MemberException(ErrorCode.NOT_EXIST_MEMBER));
...
}
```

```
@Override
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void withDraw(Long memberId, Long withDrawMoney) {
Member findMember = memberRepository.lockMemberForUpdate(memberId)
	.orElseThrow(() -> new MemberException(ErrorCode.NOT_EXIST_MEMBER));
...
}

```

금액 관련 메서드에는 새로운 트랜잭션을 생성함으로써 락을 더 빨리 해제할 수 있게 되어 문제를 해결할 수 있었습니다.

<br>
<h2>순환 참조</h2>

![image (1)](https://github.com/user-attachments/assets/839b4c8b-1d32-4d0e-a105-b7948849f17c)
<br>
![image (2)](https://github.com/user-attachments/assets/5ee02b92-d0a7-4182-bec8-7935e3e58b9a)
<br>
서비스 클래스들이 강결합되어 있어 순환참조가 자주 발생하는 문제가 있었습니다.<br>
순환참조가 발생하면 애플리케이션이 정상적으로 실행되지 않거나 의존성 주입에 문제가 발생할 수 있습니다.

순환참조를 해결하는 방법은 두 가지가 있습니다.

첫 번째 방법은 @Lazy 어노테이션을 사용하여 빈 생성을 지연시키는 방식입니다.<br>
하지만 이 방법은 근본적인 해결책이 아니며, 시스템 전체의 동작에 영향을 줄 수 있기 때문에 신중하게 사용해야 합니다.

두 번째 방법은 현재 순환적으로 참조되어 있는 클래스들의 연결고리를 끊는 것입니다.<br>
저는 순환 중인 클래스들에 중간에 클래스를 하나 두어서 순환참조 문제를 해결했습니다.

하지만 서비스 계층의 책임 분리가 제대로 이루어지지 않으면 여전히 순환참조가 발생할 가능성이 있기 때문에,<br>
서비스 계층을 더 세분화하고 책임을 명확히 분리하는 작업이 필요하다는 점을 깨달았습니다.<br>
이와 관련된 설계 패턴의 개념에 대해 더 공부해야겠다고 생각했습니다.

<br>
<h2>메서드 오버로딩과 NULL</h2>

![image](https://github.com/user-attachments/assets/da945b61-d898-4274-822f-e7f87a6076e6)

기존 코드는 fromEntity()를 오버로딩하여  응답 객체를 생성하여 두가지로 선택해서 반환하였다.

하지만 fromEntity(Product product)를 실행하여 생성한 응답 객체에서 imagePath에 null값이 저장되어서 반환되었다.

@Builder로 객체를 생성할때 특정 필드를 제외하면 반환할때 제외된다고 착각한것이다.

착각과 달리 특정 필드를 제외해도 필드에 null값이 저장되어 객체가 생성된다.

![image](https://github.com/user-attachments/assets/db0608e4-6b00-433d-9e58-2ef05e93cae9)

따라서 위와 같이 조회하고 반환 할때 필요한 목적에 맞는 Dto 클래스를 추가로 생성하였다.

<br>
<h2>외래키 제약조건, 데이터 무결성, 고아현상</h2>

![image](https://github.com/user-attachments/assets/4c5541de-0383-4c4c-aa87-d5e15784f8d2)

Photo와 product는 ManyToOne으로 자식과 부모 관계이다. 그리고 부모의 product_id를 외래키로 갖는다.
따라서
1. 부모인 Product를 삭제시 외래키 제약조건에 걸린다. 자식인 Photo에서 참조할 외래키가 없는것이다.
2. 외래키 제약 조건이 없더라도 데이터 무결성을 위배한다.  왜냐하면 부모가 삭제되더라도 데이터베이스에서 자식은 그대로 남아 있다. 이 경우 삭제 메서드 자체가 성공적으로 실행되지만, 데이터베이스에는 무효한 데이터(고아 데이터)가 남는다. 이는 데이터 무결성을 위협할 수 있다.
3. 애플리케이션에서 자식인 Photo를 명시적으로 삭제하지 않으면 고아 데이터가 남아 문제가 될수 있다.

![image](https://github.com/user-attachments/assets/a3d88bb2-5201-433c-a1cf-11504a905098)

따라서 위와 같은 방법으로 해결한다.

OnDelete(action = OnDeleteAction.CASECADE)

부모 데이터가 삭제 되면 자식 데이터가 자동으로 삭제되게 하는 설정이다.

*. 최종 수정 코드는 아래와 같다.

![image](https://github.com/user-attachments/assets/467664c2-142c-410f-9a8f-5f96b4905dff)

팀원의 Thumbnail~코드 관련하여 코드 수정이 필요하여 수정하였다.

1. 양방향 연관관계 설정.
2. repository.save() 쿼리 두번이 아닌 한번만 날라가게 작성.
3. 각 엔티티에 서로 값을 설정하게 한 이유는 한쪽에서만 값을 설정해도 되지만 양쪽에서 값을 설정해서 코드를 볼 때 헷갈리지 않게 하기 위함이다.
4. @OneToMany와casecade.ALL와 product의 addPhoto로 연관관계를 맺고 있기에 Product가 DB에 저장될때 Photo도 같이 저장된다
5. orphanRemoval = true이기 때문에 Product에서 제거된 Photo는 DB에서도 삭제된다.

추가로 팀원이 수정하기 전 코드에서 처음에 OnDelete(action = OnDeleteAction.CASECADE) 없이는 삭제 메소드 실행시 제목과 같은 문제들이 발생하였다.

하지만 며칠이 지나고 오늘 OnDelete(action = OnDeleteAction.CASECADE) 없이 삭제 메소드를 실행해봤는데 제목과 같은 문제들이 발생하지 않고 성공적으로 실행되었다.

왜 이런 현상이 발생하는지는 추가적인 학습이 필요하다.

<br><br>
# 📋 ERD
https://www.erdcloud.com/d/AnQ7aM4ScgmHpkkJa
![image](https://github.com/user-attachments/assets/67c8cd66-63ef-4800-b092-59df3c86c88e)
