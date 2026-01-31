# Reservation Event Platform

누구나 예약 이벤트를 생성하고 참가자를 모집할 수 있는 선착순 예약 플랫폼

## 핵심 컨셉
- **이벤트 생성자**: 자유롭게 예약 이벤트를 생성 (팬미팅, 스터디, 워크샵 등)
- **참가자**: 오픈 시간에 선착순으로 예약 경쟁
- **대기열 시스템**: Redis Sorted Set 기반 공정한 순서 관리

## 기술 스택

| 구분 | 기술 |
|------|------|
| Backend | Kotlin + Spring Boot 3.x |
| Frontend | Vue.js 3 + Nuxt 3 |
| Database | PostgreSQL 15+ |
| Cache/Queue | Redis 7+ (Sorted Set) |
| Message Queue | RabbitMQ |
| Container | Docker + Docker Compose |
| Auth | JWT + Redis (블랙리스트) |

## 아키텍처

### Clean Architecture 레이어
```
Domain → Application → Infrastructure → Presentation
```

- **Domain**: 엔티티, Repository 인터페이스, 도메인 서비스
- **Application**: UseCase, DTO, Port 인터페이스
- **Infrastructure**: JPA 구현, Redis 어댑터, RabbitMQ, Security
- **Presentation**: Controller, WebSocket Handler

### 프로젝트 구조
```
reservation-project/
├── backend/                 # Kotlin + Spring Boot
│   └── src/main/kotlin/com/reservation/
│       ├── domain/
│       ├── application/
│       ├── infrastructure/
│       └── presentation/
├── frontend/                # Vue.js + Nuxt 3
│   ├── pages/
│   ├── components/
│   ├── composables/
│   └── stores/
├── docker-compose.yml
└── CLAUDE.md
```

## 도메인 모델

### User (사용자)
- id, email, password, name, role(USER/ADMIN)
- 이벤트 생성자이자 참가자가 될 수 있음

### Event (예약 이벤트)
- id, creatorId, title, description
- openAt (예약 오픈 시간)
- closeAt (예약 마감 시간)
- maxParticipants (최대 참가자 수)
- status (DRAFT/SCHEDULED/OPEN/CLOSED/CANCELLED)
- settings (JSON: 대기열 사용 여부, 결제 필요 여부 등)

### Slot (예약 슬롯) - 선택적
- id, eventId, name, quantity, price
- 이벤트 내 세부 옵션 (예: VIP석, 일반석)

### Reservation (예약)
- id, eventId, userId, slotId (nullable)
- status (PENDING/CONFIRMED/CANCELLED/EXPIRED)
- createdAt, confirmedAt

### Payment (결제) - 선택적
- id, reservationId, amount, method, status
- 이벤트 설정에 따라 결제 필요 여부 결정

## 핵심 기능

### 1. 이벤트 생성/관리
- 이벤트 CRUD
- 예약 오픈 시간 설정
- 슬롯(옵션) 설정
- 참가자 현황 조회

### 2. 대기열 시스템 (Redis Sorted Set)
```kotlin
// 대기열 진입: ZADD queue:{eventId} {timestamp} {userId}
// 순위 조회: ZRANK queue:{eventId} {userId}
// 대기열 이탈: ZREM queue:{eventId} {userId}
```
- WebSocket으로 실시간 순위 업데이트
- 주기적으로 상위 N명 예약 가능 상태 전환
- 이벤트 설정에 따라 대기열 사용 여부 결정

### 3. 예약 처리 (RabbitMQ)
```
[사용자] → [API Server] → [RabbitMQ] → [Consumer] → [DB Write]
```
- Exchange: `reservation.exchange` (Direct)
- Queue: `reservation.create.queue`, `reservation.cancel.queue`

### 4. 동시성 제어
- 분산 락: Redisson (Redis SETNX)
- 재고 관리: Redis DECR 원자적 감소

### 5. 결제 (모킹) - 선택적
- 이벤트 설정에서 결제 필요 여부 설정
- MockPaymentAdapter: 랜덤 딜레이 + 성공/실패 시뮬레이션

## API 엔드포인트

```
# 인증
POST   /api/v1/auth/signup          # 회원가입
POST   /api/v1/auth/login           # 로그인 (JWT 발급)
POST   /api/v1/auth/logout          # 로그아웃
POST   /api/v1/auth/refresh         # 토큰 갱신

# 이벤트 (생성자)
POST   /api/v1/events               # 이벤트 생성
GET    /api/v1/events/my            # 내가 만든 이벤트 목록
PUT    /api/v1/events/{id}          # 이벤트 수정
DELETE /api/v1/events/{id}          # 이벤트 삭제
GET    /api/v1/events/{id}/participants  # 참가자 목록

# 이벤트 (참가자)
GET    /api/v1/events               # 이벤트 목록 (공개)
GET    /api/v1/events/{id}          # 이벤트 상세
GET    /api/v1/events/{id}/slots    # 슬롯 목록

# 대기열
POST   /api/v1/queue/enter          # 대기열 진입
GET    /api/v1/queue/position       # 대기 순위 조회
DELETE /api/v1/queue/leave          # 대기열 이탈
WS     /ws/queue                    # 실시간 순위

# 예약
POST   /api/v1/reservations         # 예약 생성
GET    /api/v1/reservations/{id}    # 예약 조회
GET    /api/v1/reservations/my      # 내 예약 목록
DELETE /api/v1/reservations/{id}    # 예약 취소

# 결제 (선택적)
POST   /api/v1/payments             # 결제 요청
GET    /api/v1/payments/{id}        # 결제 상태 조회
```

## 이벤트 설정 옵션 (settings JSON)

```json
{
  "useQueue": true,           // 대기열 사용 여부
  "queueBatchSize": 100,      // 한 번에 입장시킬 인원
  "requirePayment": false,    // 결제 필요 여부
  "maxReservationsPerUser": 1,// 1인당 최대 예약 수
  "reservationTimeLimit": 600 // 예약 제한 시간 (초)
}
```

## 사용자 플로우

### 이벤트 생성자
1. 로그인
2. 이벤트 생성 (제목, 설명, 오픈 시간, 옵션 설정)
3. 슬롯 추가 (선택)
4. 이벤트 게시
5. 참가자 현황 모니터링

### 참가자
1. 이벤트 목록 탐색
2. 관심 이벤트 상세 확인
3. 오픈 시간에 대기열 진입
4. 순서 도달 시 예약 진행
5. (선택) 결제 완료

## 실행 방법

```bash
# 전체 서비스 실행
docker-compose up -d

# 백엔드만 실행
cd backend && ./gradlew bootRun

# 프론트엔드만 실행
cd frontend && npm run dev
```

## 스케일 아웃 고려사항

1. **Stateless 설계**: 세션은 Redis에 저장
2. **Redis Cluster**: 대기열 데이터 분산
3. **DB 읽기 복제본**: 조회 성능 향상
4. **Consumer 스케일링**: RabbitMQ Consumer 인스턴스 증가

## 개발 컨벤션

- Kotlin 코드 스타일: ktlint
- 커밋 메시지: Conventional Commits
- 브랜치 전략: Git Flow
