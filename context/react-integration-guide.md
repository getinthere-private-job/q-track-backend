# React 연동 가이드

Q-Track 백엔드 API와 React 프론트엔드 연동을 위한 개발 가이드입니다.

## 📋 목차
1. [서버 기본 정보](#서버-기본-정보)
2. [인증 방식 (JWT)](#인증-방식-jwt)
3. [공통 응답 형식](#공통-응답-형식)
4. [에러 응답 형식](#에러-응답-형식)
5. [API 엔드포인트 목록](#api-엔드포인트-목록)
6. [DTO 구조 상세](#dto-구조-상세)
7. [권한 기반 접근 제어](#권한-기반-접근-제어)
8. [개발 팁](#개발-팁)

---

## 서버 기본 정보

### 기본 URL
```
http://localhost:8080
```

### API 베이스 경로
```
/api
```

### 인증 필요 없음 (Public)
- `POST /api/users/signup` - 회원가입
- `POST /api/users/login` - 로그인
- `GET /api.html` - API 문서
- `GET /static/**` - 정적 리소스

### 인증 필요 (모든 API)
- 나머지 모든 API는 JWT 토큰이 필요합니다.

---

## 인증 방식 (JWT)

### 토큰 사용 방법
모든 인증이 필요한 API 요청 헤더에 다음 형식으로 토큰을 포함해야 합니다:

```
Authorization: Bearer {token}
```

### 토큰 발급 (로그인)
로그인 API를 통해 JWT 토큰을 발급받습니다.

**요청:**
```json
POST /api/users/login
Content-Type: application/json

{
  "username": "testuser",
  "password": "password123"
}
```

**응답:**
```json
{
  "status": 200,
  "msg": "성공",
  "body": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "id": 111,
    "username": "testuser",
    "role": "USER"
  }
}
```

### 토큰 유효기간
- JWT 토큰은 발급일로부터 **5일** 동안 유효합니다.
- 토큰이 만료되면 재로그인이 필요합니다.

### 권한(Role) 종류
- `USER`: 일반 사용자
- `MANAGER`: 관리자
- `ADMIN`: 최고 관리자

---

## 공통 응답 형식

모든 API 응답은 다음 형식으로 래핑됩니다:

### 성공 응답
```typescript
interface Resp<T> {
  status: number;      // HTTP 상태 코드 (200)
  msg: string;         // 응답 메시지 ("성공")
  body: T;             // 실제 데이터 (제네릭 타입)
}
```

### 예시
```json
{
  "status": 200,
  "msg": "성공",
  "body": {
    // 실제 데이터
  }
}
```

---

## 에러 응답 형식

에러 발생 시에도 동일한 `Resp` 형식을 따릅니다:

```json
{
  "status": 400,        // HTTP 상태 코드 (400, 401, 403, 500)
  "msg": "에러 메시지",  // 에러 상세 메시지
  "body": null          // 항상 null
}
```

### 에러 상태 코드
- `400` (BAD_REQUEST): 잘못된 요청 (유효성 검사 실패 등)
- `401` (UNAUTHORIZED): 인증 실패 (토큰 없음, 잘못된 토큰)
- `403` (FORBIDDEN): 권한 없음
- `500` (INTERNAL_SERVER_ERROR): 서버 내부 오류

---

## API 엔드포인트 목록

### 1. 사용자 관리 (`/api/users`)

| 메서드 | 엔드포인트 | 권한 | 설명 |
|--------|-----------|------|------|
| POST | `/api/users/signup` | Public | 회원가입 |
| POST | `/api/users/login` | Public | 로그인 |
| GET | `/api/users/{id}` | Authenticated | 사용자 조회 |

### 2. 공정 관리 (`/api/processes`)

| 메서드 | 엔드포인트 | 권한 | 설명 |
|--------|-----------|------|------|
| GET | `/api/processes` | Authenticated | 공정 목록 조회 |
| GET | `/api/processes/{id}` | Authenticated | 공정 상세 조회 |

### 3. 시스템 코드 관리 (`/api/system-codes`)

| 메서드 | 엔드포인트 | 권한 | 설명 |
|--------|-----------|------|------|
| GET | `/api/system-codes` | Authenticated | 시스템 코드 조회 (전체 또는 codeGroup 필터) |

**Query Parameters:**
- `codeGroup` (optional): 코드 그룹 필터링

### 4. 부품 관리 (`/api/items`)

| 메서드 | 엔드포인트 | 권한 | 설명 |
|--------|-----------|------|------|
| GET | `/api/items` | Authenticated | 부품 목록 조회 |
| GET | `/api/items/{id}` | Authenticated | 부품 상세 조회 |
| POST | `/api/items` | MANAGER, ADMIN | 부품 생성 |
| PUT | `/api/items/{id}` | MANAGER, ADMIN | 부품 수정 |
| DELETE | `/api/items/{id}` | ADMIN | 부품 삭제 |

### 5. 일별 생산 데이터 관리 (`/api/daily-productions`)

| 메서드 | 엔드포인트 | 권한 | 설명 |
|--------|-----------|------|------|
| GET | `/api/daily-productions` | Authenticated | 일별 생산 목록 조회 |
| GET | `/api/daily-productions/{id}` | Authenticated | 일별 생산 상세 조회 |
| POST | `/api/daily-productions` | USER, MANAGER, ADMIN | 일별 생산 생성 |
| PUT | `/api/daily-productions/{id}` | USER, MANAGER, ADMIN | 일별 생산 수정 |
| DELETE | `/api/daily-productions/{id}` | MANAGER, ADMIN | 일별 생산 삭제 |

### 6. 품질 기록 관리 (`/api/quality-records`)

| 메서드 | 엔드포인트 | 권한 | 설명 |
|--------|-----------|------|------|
| GET | `/api/quality-records` | Authenticated | 품질 기록 목록 조회 |
| GET | `/api/quality-records/{id}` | Authenticated | 품질 기록 상세 조회 |
| POST | `/api/quality-records` | USER, MANAGER, ADMIN | 품질 기록 생성 |
| PUT | `/api/quality-records/{id}` | USER, MANAGER, ADMIN | 품질 기록 수정 |
| DELETE | `/api/quality-records/{id}` | MANAGER, ADMIN | 품질 기록 삭제 |
| PUT | `/api/quality-records/{id}/evaluate` | USER, MANAGER, ADMIN | 품질 기록 평가 |
| GET | `/api/quality-records/evaluation-required` | Authenticated | 평가 필요 목록 조회 |

### 7. 통계 (`/api/quality-records/statistics`)

| 메서드 | 엔드포인트 | 권한 | 설명 |
|--------|-----------|------|------|
| GET | `/api/quality-records/statistics/by-process` | Authenticated | 공정별 NG 비율 통계 |
| GET | `/api/quality-records/statistics/by-item` | Authenticated | 부품별 NG 비율 통계 |

**Query Parameters (선택):**
- `startDate` (optional): 통계 시작일 (yyyy-MM-dd 형식)
- `endDate` (optional): 통계 종료일 (yyyy-MM-dd 형식)

---

## DTO 구조 상세

### User

#### Request
```typescript
// Signup
interface UserSignupRequest {
  username: string;    // 필수
  password: string;    // 필수
  role: 'USER' | 'MANAGER' | 'ADMIN';  // 필수
}

// Login
interface UserLoginRequest {
  username: string;    // 필수
  password: string;    // 필수
}
```

#### Response
```typescript
// Signup
interface UserSignupResponse {
  id: number;
  username: string;
  role: 'USER' | 'MANAGER' | 'ADMIN';
}

// Login
interface UserLoginResponse {
  token: string;       // JWT 토큰
  id: number;
  username: string;
  role: 'USER' | 'MANAGER' | 'ADMIN';
}

// Get
interface UserGetResponse {
  id: number;
  username: string;
  role: 'USER' | 'MANAGER' | 'ADMIN';
}
```

### Process

#### Response
```typescript
// List / Get
interface ProcessResponse {
  id: number;
  code: string;
  name: string;
  description: string;
  sequence: number;
}
```

### SystemCode

#### Response
```typescript
interface SystemCodeResponse {
  id: number;
  codeGroup: string;
  codeKey: string;
  codeValue: string;
  description: string;
  isActive: boolean;
}
```

### Item

#### Request
```typescript
// Create
interface ItemCreateRequest {
  code: string;           // 필수
  name: string;           // 필수
  description?: string;   // 선택
  category?: string;      // 선택
}

// Update
interface ItemUpdateRequest {
  name: string;           // 필수
  description?: string;   // 선택
  category?: string;      // 선택
}
```

#### Response
```typescript
// List / Get / Create / Update
interface ItemResponse {
  id: number;
  code: string;
  name: string;
  description: string;
  category: string;
}

// Delete
interface ItemDeleteResponse {
  id: number;
}
```

### DailyProduction

#### Request
```typescript
// Create
interface DailyProductionCreateRequest {
  itemId: number;              // 필수
  productionDate: string;      // 필수 (yyyy-MM-dd 형식)
  totalQuantity: number;       // 필수 (0 이상)
}

// Update
interface DailyProductionUpdateRequest {
  totalQuantity: number;       // 필수 (0 이상)
}
```

#### Response
```typescript
// List / Get / Create / Update
interface DailyProductionResponse {
  id: number;
  itemId: number;
  productionDate: string;      // yyyy-MM-dd 형식
  totalQuantity: number;
}

// Delete
interface DailyProductionDeleteResponse {
  id: number;
}
```

### QualityRecord

#### Request
```typescript
// Create
interface QualityRecordCreateRequest {
  dailyProductionId: number;   // 필수
  processId: number;           // 필수
  okQuantity: number;          // 필수 (0 이상)
  ngQuantity: number;          // 필수 (0 이상)
}

// Update
interface QualityRecordUpdateRequest {
  okQuantity: number;          // 필수 (0 이상)
  ngQuantity: number;          // 필수 (0 이상)
}

// Evaluate
interface QualityRecordEvaluateRequest {
  expertEvaluation: string;    // 필수 (전문가 평가 내용)
}
```

#### Response
```typescript
// List
interface QualityRecordListResponse {
  id: number;
  dailyProductionId: number;
  processId: number;
  okQuantity: number;
  ngQuantity: number;
  totalQuantity: number;       // okQuantity + ngQuantity 자동 계산
  ngRate: number;              // (ngQuantity / totalQuantity) * 100 자동 계산
  expertEvaluation: string | null;
  evaluationRequired: boolean; // 자동 계산 (NG 비율 5% 초과 또는 전일 대비 10% 증가)
  evaluationReason: string | null;
}

// Get
interface QualityRecordGetResponse {
  id: number;
  dailyProductionId: number;
  processId: number;
  okQuantity: number;
  ngQuantity: number;
  totalQuantity: number;
  ngRate: number;
  expertEvaluation: string | null;
  evaluationRequired: boolean;
  evaluationReason: string | null;
  evaluatedAt: string | null;  // ISO 8601 형식 (LocalDateTime)
  evaluatedBy: number | null;  // User ID
}

// Create / Update
interface QualityRecordCreateUpdateResponse {
  id: number;
  dailyProductionId: number;
  processId: number;
  okQuantity: number;
  ngQuantity: number;
  totalQuantity: number;
  ngRate: number;
  evaluationRequired: boolean;
  evaluationReason: string | null;
}

// Evaluate
interface QualityRecordEvaluateResponse {
  id: number;
  dailyProductionId: number;
  processId: number;
  expertEvaluation: string;
  evaluatedBy: number;         // User ID
  evaluatedAt: string;         // ISO 8601 형식
}

// Delete
interface QualityRecordDeleteResponse {
  id: number;
}

// Statistics - By Process
interface QualityRecordStatisticsByProcessResponse {
  processId: number;
  processCode: string;
  processName: string;
  totalNgQuantity: number;
  totalQuantity: number;
  ngRate: number;              // BigDecimal (소수점 2자리)
}

// Statistics - By Item
interface QualityRecordStatisticsByItemResponse {
  itemId: number;
  itemCode: string;
  itemName: string;
  totalNgQuantity: number;
  totalQuantity: number;
  ngRate: number;              // BigDecimal (소수점 2자리)
}
```

---

## 권한 기반 접근 제어

### 권한 레벨
1. **PUBLIC**: 인증 없이 접근 가능
2. **Authenticated**: 로그인만 하면 접근 가능 (모든 Role)
3. **USER, MANAGER, ADMIN**: 해당 Role 이상만 접근 가능
4. **MANAGER, ADMIN**: MANAGER 또는 ADMIN만 접근 가능
5. **ADMIN**: ADMIN만 접근 가능

### 권한별 API 접근 범위

#### USER 권한으로 가능한 작업
- 모든 조회 API
- 일별 생산 데이터 생성/수정
- 품질 기록 생성/수정/평가

#### MANAGER 권한으로 가능한 작업
- USER 권한의 모든 작업
- 부품 생성/수정
- 일별 생산 데이터 삭제
- 품질 기록 삭제

#### ADMIN 권한으로 가능한 작업
- MANAGER 권한의 모든 작업
- 부품 삭제

---

## 개발 팁

### 1. API 호출 예시 (Axios)

```typescript
import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

// Axios 인스턴스 생성
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 요청 인터셉터: 토큰 자동 추가
apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// 응답 인터셉터: 에러 처리
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // 토큰 만료 또는 인증 실패
      localStorage.removeItem('token');
      // 로그인 페이지로 리다이렉트
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// 사용 예시
async function login(username: string, password: string) {
  const response = await apiClient.post('/users/login', {
    username,
    password,
  });
  const { body } = response.data;
  localStorage.setItem('token', body.token);
  return body;
}
```

### 2. 타입 정의 예시

```typescript
// types/api.ts
export interface Resp<T> {
  status: number;
  msg: string;
  body: T;
}

export type Role = 'USER' | 'MANAGER' | 'ADMIN';

export interface UserLoginRequest {
  username: string;
  password: string;
}

export interface UserLoginResponse {
  token: string;
  id: number;
  username: string;
  role: Role;
}

// API 함수
export const userApi = {
  login: async (data: UserLoginRequest): Promise<Resp<UserLoginResponse>> => {
    const response = await apiClient.post('/users/login', data);
    return response.data;
  },
};
```

### 3. 날짜 형식
- 백엔드에서 `LocalDate`와 `LocalDateTime`을 사용합니다.
- 요청/응답 시 날짜 형식:
  - `LocalDate`: `"yyyy-MM-dd"` (예: `"2025-01-18"`)
  - `LocalDateTime`: ISO 8601 형식 (예: `"2025-01-18T10:30:00"`)

### 4. NG 비율 계산
- `totalQuantity` = `okQuantity` + `ngQuantity` (자동 계산)
- `ngRate` = `(ngQuantity / totalQuantity) * 100` (자동 계산)
- 백엔드에서 `@PrePersist`, `@PreUpdate`로 자동 계산됩니다.

### 5. 평가 필요 자동 계산
`evaluationRequired`는 다음 조건 중 하나라도 만족하면 `true`가 됩니다:
- NG 비율이 5%를 초과하는 경우
- 전일 대비 NG 비율이 10% 이상 증가한 경우

### 6. CORS 설정
개발 환경에서는 React 앱이 다른 포트에서 실행될 수 있으므로, 필요 시 백엔드에 CORS 설정을 추가해야 합니다.

### 7. API 문서 확인
개발 중 API 문서를 참고하려면:
- 브라우저에서 `http://localhost:8080/api.html` 접속
- 상세한 API 명세, 요청/응답 예시 확인 가능

---

## 참고사항

- 모든 숫자 필드는 JSON에서 숫자 타입으로 전송됩니다 (문자열 아님).
- `BigDecimal` 타입은 JSON에서 숫자로 직렬화됩니다 (예: `10.5`).
- `null` 값은 JSON에서 `null`로 표현됩니다.
- 빈 문자열 `""`과 `null`은 다릅니다 (백엔드 유효성 검사 참고).
- 배열 응답은 `body`가 배열 타입입니다 (예: `body: []`).

---

## 문제 해결

### 401 Unauthorized
- 토큰이 없거나 만료된 경우
- 헤더 형식이 올바른지 확인: `Authorization: Bearer {token}`
- 토큰 재발급 필요

### 403 Forbidden
- 권한이 부족한 경우
- 사용자 Role 확인 필요

### 400 Bad Request
- 요청 데이터 유효성 검사 실패
- `msg` 필드에서 상세 에러 메시지 확인

---

**마지막 업데이트:** 2025-01-18
