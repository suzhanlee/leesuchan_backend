# API 7: 계좌 단건 조회

계좌번호로 계좌 정보를 조회합니다.

---

## 1. Endpoint

```
GET /api/accounts/{accountNumber}
```

---

## 2. Path Parameter

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| accountNumber | String | Y | 계좌번호 (3~20자) |

---

## 3. Request

**Request Body**: 없음

**Request Example**:
```http
GET /api/accounts/1234567890 HTTP/1.1
Host: localhost:8080
```

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
    "id": 1,
    "accountNumber": "1234567890",
    "accountName": "홍길동",
    "balance": 100000,
    "createdAt": "2026-01-01T12:00:00",
    "updatedAt": "2026-01-01T12:30:00"
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
| data.id | Long | 계좌 ID |
| data.accountNumber | String | 계좌번호 |
| data.accountName | String | 계좌명 |
| data.balance | Long | 잔액 (원 단위) |
| data.createdAt | String | 생성일시 (ISO 8601) |
| data.updatedAt | String | 수정일시 (ISO 8601) |
| message | String/null | 추가 메시지 |

---

## 5. Error Response

### 5.1 계좌 미조회 (404 Not Found)

**HTTP Status**: `404 Not Found`

**Response Body**:
```json
{
  "status": {
    "success": false,
    "code": "ACCOUNT_001",
    "message": null
  },
  "data": null,
  "message": "계좌를 찾을 수 없습니다."
}
```

### 5.2 Bean Validation 실패 (400 Bad Request)

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
  "message": "계좌번호는 3~20자여야 합니다."
}
```

---

## 6. Error Status Code

| HTTP Status | 에러 코드 | 설명 |
|-------------|----------|------|
| 200 | SUCCESS | 성공 |
| 404 | ACCOUNT_001 | 계좌를 찾을 수 없습니다 |
| 400 | VALIDATION_ERROR | 요청 파라미터 유효성 검증 실패 |
| 500 | INTERNAL_ERROR | 서버 내부 오류 |

---

## 7. 비즈니스 로직

1. 계좌번호로 계좌 조회
2. 존재하지 않는 계좌일 경우 `AccountNotFoundException` 발생
3. 계좌 정보를 `AccountResponse`로 변환하여 반환

---

## 8. 시퀀스 다이어그램

```
Client → Controller → GetAccountQueryService → AccountRepo
  │        │                  │                    │
  │─ GET   │                  │                    │
  │        │─ execute()       │                    │
  │        │                  │                    │
  │        │                  │─ findByAccountNumber()
  │        │                  │─────────────────────▶│
  │        │                  │◀─────────────────────│
  │        │                  │                    │
  │        │                  │─ AccountResponse.from()
  │        │◀─────────────────│                    │
  │◀───────│                  │                    │
```

---

## 9. 관련 파일

| 파일 | 경로 |
|------|------|
| GetAccountQueryService | `service/src/main/java/com/leesuchan/service/GetAccountQueryService.java` |
| AccountController | `service/src/main/java/com/leesuchan/service/AccountController.java` |
| AccountExceptionHandler | `service/src/main/java/com/leesuchan/service/AccountExceptionHandler.java` |
| AccountRepository | `account/src/main/java/com/leesuchan/account/domain/repository/AccountRepository.java` |
| AccountNotFoundException | `account/src/main/java/com/leesuchan/account/domain/exception/AccountNotFoundException.java` |

---

## 10. 테스트

| 테스트 파일 | 경로 |
|-------------|------|
| GetAccountQueryServiceTest | `service/src/test/java/com/leesuchan/service/GetAccountQueryServiceTest.java` |
| GetAccountE2ETest | `service/src/test/java/com/leesuchan/service/GetAccountE2ETest.java` |
