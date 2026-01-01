# API 8: 계좌 목록 조회

계좌 목록을 페이지네이션으로 조회합니다.

---

## 1. Endpoint

```
GET /api/accounts?page=0&size=20&sort=createdAt,desc
```

---

## 2. Query Parameter

| 필드 | 타입 | 필수 | 기본값 | 설명 |
|------|------|------|--------|------|
| page | Integer | N | 0 | 페이지 번호 (0부터 시작) |
| size | Integer | N | 20 | 페이지 사이즈 |
| sort | String | N | - | 정렬 기준 (예: createdAt,desc) |

---

## 3. Request

**Request Body**: 없음

**Request Example**:
```http
GET /api/accounts?page=0&size=20&sort=createdAt,desc HTTP/1.1
Host: localhost:8080
```

**Query Parameter Examples**:

| 요청 | 설명 |
|------|------|
| `/api/accounts` | 첫 페이지, 기본 사이즈(20) |
| `/api/accounts?page=1` | 두 번째 페이지 |
| `/api/accounts?page=0&size=10` | 첫 페이지, 10개 |
| `/api/accounts?sort=accountNumber,asc` | 계좌번호 오름차순 정렬 |
| `/api/accounts?sort=createdAt,desc` | 생성일 내림차순 정렬 (최신순) |

---

## 4. Response

### 4.1 성공 응답 (200 OK)

**Response Body**:
```json
{
  "status": {
    "success": true,
    "code": "SUCCESS",
    "message": "성공"
  },
  "data": {
    "content": [
      {
        "id": 1,
        "accountNumber": "1234567890",
        "accountName": "홍길동",
        "balance": 100000,
        "createdAt": "2026-01-01T12:00:00",
        "updatedAt": "2026-01-01T12:30:00"
      },
      {
        "id": 2,
        "accountNumber": "9876543210",
        "accountName": "김철수",
        "balance": 50000,
        "createdAt": "2026-01-01T11:00:00",
        "updatedAt": "2026-01-01T11:00:00"
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 20,
      "sort": {
        "sorted": true,
        "unsorted": false,
        "empty": false
      },
      "offset": 0,
      "paged": true,
      "unpaged": false
    },
    "totalPages": 1,
    "totalElements": 2,
    "last": true,
    "first": true,
    "numberOfElements": 2,
    "size": 20,
    "number": 0,
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    },
    "empty": false
  },
  "message": null
}
```

**Field Descriptions**:

| 필드 | 타입 | 설명 |
|------|------|------|
| status.success | Boolean | 성공 여부 |
| status.code | String | 응답 코드 |
| status.message | String | 응답 메시지 |
| data.content | Array | 계좌 목록 |
| data.content[].id | Long | 계좌 ID |
| data.content[].accountNumber | String | 계좌번호 |
| data.content[].accountName | String | 계좌명 |
| data.content[].balance | Long | 잔액 (원 단위) |
| data.content[].createdAt | String | 생성일시 (ISO 8601) |
| data.content[].updatedAt | String | 수정일시 (ISO 8601) |
| data.pageable.pageNumber | Integer | 현재 페이지 번호 |
| data.pageable.pageSize | Integer | 페이지 사이즈 |
| data.totalPages | Integer | 전체 페이지 수 |
| data.totalElements | Long | 전체 데이터 수 |
| data.last | Boolean | 마지막 페이지 여부 |
| data.first | Boolean | 첫 페이지 여부 |
| data.numberOfElements | Integer | 현재 페이지 데이터 수 |
| data.size | Integer | 페이지 사이즈 |
| data.number | Integer | 현재 페이지 번호 |
| data.empty | Boolean | 데이터 존재 여부 |
| message | String/null | 추가 메시지 |

---

## 5. Error Response

### 5.1 잘못된 페이지 요청 (400 Bad Request)

**HTTP Status**: `400 Bad Request`

**Response Body**:
```json
{
  "status": {
    "success": false,
    "code": "VALIDATION_ERROR",
    "message": null
  },
  "data": null,
  "message": "page는 0 이상이어야 합니다."
}
```

---

## 6. Error Status Code

| HTTP Status | 에러 코드 | 설명 |
|-------------|----------|------|
| 200 | SUCCESS | 성공 |
| 400 | VALIDATION_ERROR | 요청 파라미터 유효성 검증 실패 |
| 500 | INTERNAL_ERROR | 서버 내부 오류 |

---

## 7. 비즈니스 로직

1. 페이지네이션 정보로 계좌 목록 조회
2. 삭제되지 않은 계좌만 조회 (deletedAt IS NULL)
3. 최신순 정렬 (기본: createdAt DESC)
4. Page<Account>를 Page<AccountResponse>로 변환

---

## 8. 페이지네이션 예시

| 요청 | 응답 |
|------|------|
| `page=0&size=5` (총 12개) | 첫 5개, totalPages=3, last=false |
| `page=1&size=5` (총 12개) | 6~10번째, totalPages=3, last=false |
| `page=2&size=5` (총 12개) | 마지막 2개, totalPages=3, last=true |
| `page=10&size=5` (총 12개) | 빈 페이지, empty=true |

---

## 9. 시퀀스 다이어그램

```
Client → Controller → GetAccountsQueryService → AccountRepo
  │        │                  │                     │
  │─ GET   │                  │                     │
  │        │─ execute(pageable)                  │
  │        │                  │                     │
  │        │                  │─ findAll(pageable)  │
  │        │                  │─────────────────────▶│
  │        │                  │◀─────────────────────│
  │        │                  │                     │
  │        │                  │─ map(AccountResponse.from)
  │        │◀─────────────────│                     │
  │◀───────│                  │                     │
```

---

## 10. 관련 파일

| 파일 | 경로 |
|------|------|
| GetAccountsQueryService | `service/src/main/java/com/leesuchan/service/GetAccountsQueryService.java` |
| AccountController | `service/src/main/java/com/leesuchan/service/AccountController.java` |
| AccountRepository | `account/src/main/java/com/leesuchan/account/domain/repository/AccountRepository.java` |
| AccountRepositoryImpl | `infra/database/src/main/java/com/leesuchan/infra/database/repository/AccountRepositoryImpl.java` |
| AccountJpaRepository | `infra/database/src/main/java/com/leesuchan/infra/database/repository/AccountJpaRepository.java` |

---

## 11. 테스트

| 테스트 파일 | 경로 |
|-------------|------|
| GetAccountsQueryServiceTest | `service/src/test/java/com/leesuchan/service/GetAccountsQueryServiceTest.java` |
| GetAccountsE2ETest | `service/src/test/java/com/leesuchan/service/GetAccountsE2ETest.java` |
