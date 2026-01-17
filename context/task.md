# 개발 작업 목록 (Development Tasks)

## 📋 작업 순서

### Phase 1: 환경 설정 및 기본 인프라 구축

#### 1. 전체 환경 설정

##### 1.1 프로젝트 기본 설정
- [x] `application-dev.properties`에 Open Session In View 비활성화 설정
  - `spring.jpa.open-in-view=false`
- [x] 개발 환경 확인 (H2, JPA, 로깅 설정 확인)

##### 1.2 _core 패키지 구조 생성
- [x] `_core/errors/ex/` 패키지 생성
  - [x] `Exception400.java` - Bad Request 예외
  - [x] `Exception401.java` - Unauthorized 예외
  - [x] `Exception403.java` - Forbidden 예외
  - [x] `Exception500.java` - Internal Server Error 예외
- [x] `_core/errors/` 패키지
  - [x] `CustomValidationHandler.java` - 유효성 검사 예외 핸들러
  - [x] `GlobalExceptionHandler.java` - 전역 예외 핸들러
- [x] `_core/security/` 패키지 생성 (구조만)
- [x] `_core/util/` 패키지 생성 (구조만) - `Resp.java` 공통 응답 DTO 포함
- [x] `_core/validation/` 패키지 생성 (구조만)
- [x] `_core/filter/` 패키지 생성 (구조만)

---

### Phase 2: 인증 및 사용자 관리

#### 2. User (사용자) 기능

##### 2.1 User 엔티티 및 테이블
- [x] `user/User.java` 엔티티 생성
  - 필드: `id`, `username`, `password`, `role`, `createdAt`, `updatedAt`
  - 테이블명: `user_tb`
  - Role Enum 생성 (USER, MANAGER, ADMIN)
- [x] `user/UserRepository.java` 생성
- [ ] 초기 관리자 계정 생성 로직 (선택사항)

##### 2.2 User Service 및 Controller
- [x] `user/UserService.java` 생성
  - `@Transactional(readOnly = true)` 기본 적용
- [x] `user/UserController.java` 생성
  - 회원가입 API (POST /api/users/signup)
  - 사용자 조회 API (GET /api/users/{id})

##### 2.3 User 통합 테스트
- [x] `user/UserControllerTest.java` 생성
  - [x] 회원가입 API 테스트 (POST /api/users/signup)
  - [x] 사용자 조회 API 테스트 (GET /api/users/{id})
  - [x] 중복 username 회원가입 실패 테스트

---

### Phase 3: Spring Security 및 JWT 인증

#### 3.1 JWT 유틸리티
- [x] `_core/security/JwtUtil.java` 생성
  - Access Token 생성 (5일 유효기간)
  - Token 검증
  - Token에서 User ID, Role 추출

#### 3.2 Security 설정
- [x] `_core/security/SecurityConfig.java` 생성
  - Security Filter Chain 설정
  - `/api/users/signup`, `/api/users/login`는 permitAll
  - 나머지 `/api/**`는 인증 필요
  - PasswordEncoder 설정 (BCrypt)

#### 3.3 JWT 필터
- [x] `_core/security/JwtFilter.java` 생성
  - 요청 Header에서 JWT 추출
  - JWT 검증 후 Security Context에 User ID, Role 저장
  - `UsernamePasswordAuthenticationToken` 생성

#### 3.4 인증 API
- [x] `user/UserController.java`에 로그인 API 추가
  - POST `/api/users/login`
  - RequestBody: `username`, `password`
  - Response: JWT Access Token 반환

#### 3.5 Security Context 테스트
- [x] Controller에서 `@AuthenticationPrincipal` 또는 Security Context에서 User ID, Role 추출 확인

#### 3.6 인증 통합 테스트
- [x] `user/UserControllerTest.java`에 로그인 테스트 추가
  - [x] 로그인 성공 시 JWT 토큰 반환 테스트
  - [x] 잘못된 비밀번호 로그인 실패 테스트
  - [x] 존재하지 않는 사용자 로그인 실패 테스트
- [x] `_core/security/SecurityConfigTest.java` 생성
  - [x] 인증 필요한 API 접근 시 403 에러 테스트
  - [x] 인증된 사용자의 API 접근 성공 테스트

---

### Phase 4: 마스터 데이터 (코드 관리)

#### 4.1 Process (공정) 기능
- [x] `process/Process.java` 엔티티 생성
  - 필드: `id`, `code`, `name`, `description`, `sequence`, `createdAt`, `updatedAt`
  - 테이블명: `process_tb`
- [x] `process/ProcessRepository.java` 생성
- [x] `process/ProcessService.java` 생성
  - `@Transactional(readOnly = true)` 기본 적용
- [x] `process/ProcessController.java` 생성
  - 공정 목록 조회 API (GET /api/processes)
  - 공정 상세 조회 API (GET /api/processes/{id})
- [ ] 초기 데이터: W(작업), P(제조), 검(검사) 공정 3개 삽입

##### 4.1.1 Process 통합 테스트
- [x] `process/ProcessControllerTest.java` 생성
  - [x] 공정 목록 조회 API 테스트 (GET /api/processes)
  - [x] 공정 상세 조회 API 테스트 (GET /api/processes/{id})
  - [x] 존재하지 않는 공정 조회 시 400 에러 테스트

#### 4.2 SystemCode (시스템 코드) 기능
- [x] `systemcode/SystemCode.java` 엔티티 생성
  - 필드: `id`, `codeGroup`, `codeKey`, `codeValue`, `description`, `isActive`, `createdAt`, `updatedAt`
  - 테이블명: `system_code_tb`
  - Unique: `(codeGroup, codeKey)`
- [x] `systemcode/SystemCodeRepository.java` 생성
- [x] `systemcode/SystemCodeService.java` 생성
  - `@Transactional(readOnly = true)` 기본 적용
  - `getCodeValue(codeGroup, codeKey)` 메서드
- [x] `systemcode/SystemCodeController.java` 생성
  - 시스템 코드 조회 API (GET /api/system-codes)
- [ ] 초기 데이터 삽입:
  - `INDUSTRY_AVERAGE.NG_RATE_THRESHOLD = 0.5`
  - `INDUSTRY_AVERAGE.NG_RATE_ALERT = 1.0`
  - `EVALUATION.INCREASE_RATE_THRESHOLD = 2.0`

##### 4.2.1 SystemCode 통합 테스트
- [x] `systemcode/SystemCodeControllerTest.java` 생성
  - [x] 시스템 코드 조회 API 테스트 (GET /api/system-codes)
  - [x] 코드 그룹별 조회 테스트
  - [x] `getCodeValue()` 메서드 테스트

---

### Phase 5: 부품 관리

#### 5.1 Item (부품) 기능
- [x] `item/Item.java` 엔티티 생성
  - 필드: `id`, `code`, `name`, `description`, `category`, `createdAt`, `updatedAt`
  - 테이블명: `item_tb`
  - Unique: `code`
- [x] `item/ItemRepository.java` 생성
- [x] `item/ItemService.java` 생성
  - `@Transactional(readOnly = true)` 기본 적용
- [x] `item/ItemController.java` 생성
  - 부품 목록 조회 API (GET /api/items)
  - 부품 상세 조회 API (GET /api/items/{id})
  - 부품 생성 API (POST /api/items) - 권한: MANAGER, ADMIN
  - 부품 수정 API (PUT /api/items/{id}) - 권한: MANAGER, ADMIN
  - 부품 삭제 API (DELETE /api/items/{id}) - 권한: ADMIN

##### 5.1.1 Item 통합 테스트
- [x] `item/ItemControllerTest.java` 생성
  - [x] 부품 목록 조회 API 테스트 (GET /api/items)
  - [x] 부품 상세 조회 API 테스트 (GET /api/items/{id})
  - [x] 부품 생성 API 테스트 (POST /api/items) - 권한 검증 포함
  - [x] 부품 수정 API 테스트 (PUT /api/items/{id}) - 권한 검증 포함
  - [x] 부품 삭제 API 테스트 (DELETE /api/items/{id}) - 권한 검증 포함
  - [x] 중복 code 생성 실패 테스트
  - [x] USER 권한으로 생성/수정/삭제 시 403 에러 테스트

---

### Phase 6: 일별 생산 데이터 관리

#### 6.1 DailyProduction (일별 생산) 기능
- [x] `dailyproduction/DailyProduction.java` 엔티티 생성
  - 필드: `id`, `itemId`, `productionDate`, `totalQuantity`, `createdAt`, `updatedAt`
  - 테이블명: `daily_production_tb`
  - Unique: `(itemId, productionDate)`
  - ForeignKey: `itemId → Item.id`
- [x] `dailyproduction/DailyProductionRepository.java` 생성
- [x] `dailyproduction/DailyProductionService.java` 생성
  - `@Transactional(readOnly = true)` 기본 적용 (필요 시 일부 메서드에 `readOnly = false`)
  - 일별 생산 데이터 생성/수정 시 `totalQuantity` 검증
- [x] `dailyproduction/DailyProductionController.java` 생성
  - 일별 생산 목록 조회 API (GET /api/daily-productions)
  - 일별 생산 상세 조회 API (GET /api/daily-productions/{id})
  - 일별 생산 생성 API (POST /api/daily-productions) - 권한: USER, MANAGER, ADMIN
  - 일별 생산 수정 API (PUT /api/daily-productions/{id}) - 권한: USER, MANAGER, ADMIN
  - 일별 생산 삭제 API (DELETE /api/daily-productions/{id}) - 권한: MANAGER, ADMIN

##### 6.1.1 DailyProduction 통합 테스트
- [x] `dailyproduction/DailyProductionControllerTest.java` 생성
  - [x] 일별 생산 목록 조회 API 테스트 (GET /api/daily-productions)
  - [x] 일별 생산 상세 조회 API 테스트 (GET /api/daily-productions/{id})
  - [x] 일별 생산 생성 API 테스트 (POST /api/daily-productions)
  - [x] 일별 생산 수정 API 테스트 (PUT /api/daily-productions/{id})
  - [x] 일별 생산 삭제 API 테스트 (DELETE /api/daily-productions/{id}) - 권한 검증
  - [x] 같은 부품+날짜 중복 생성 실패 테스트
  - [x] 존재하지 않는 Item 참조 시 에러 테스트

---

### Phase 7: 품질 기록 관리

#### 7.1 QualityRecord (품질 기록) 기능
- [x] `qualityrecord/QualityRecord.java` 엔티티 생성
  - 필드: `id`, `dailyProductionId`, `processId`, `okQuantity`, `ngQuantity`, `totalQuantity`, `ngRate`, `expertEvaluation`, `evaluationRequired`, `evaluationReason`, `evaluatedAt`, `evaluatedBy`, `createdAt`, `updatedAt`
  - 테이블명: `quality_record_tb`
  - Unique: `(dailyProductionId, processId)`
  - ForeignKey: `dailyProductionId → DailyProduction.id`, `processId → Process.id`
  - `ngRate`는 JPA `@PrePersist`, `@PreUpdate` 이벤트 리스너로 자동 계산
- [x] `qualityrecord/QualityRecordRepository.java` 생성
- [x] `qualityrecord/QualityRecordService.java` 생성
  - `@Transactional(readOnly = true)` 기본 적용 (필요 시 일부 메서드에 `readOnly = false`)
  - `totalQuantity = okQuantity + ngQuantity` 자동 계산 (엔티티 생성자/update 메서드에서 처리)
  - `calculateEvaluationRequired()` 메서드 구현
    - NG 비율이 임계값(0.5%) 초과 시 평가 필요
    - 전일 대비 NG 비율 2배 이상 급증 시 평가 필요
- [x] `qualityrecord/QualityRecordController.java` 생성
  - 품질 기록 목록 조회 API (GET /api/quality-records)
  - 품질 기록 상세 조회 API (GET /api/quality-records/{id})
  - 품질 기록 생성 API (POST /api/quality-records) - 권한: USER, MANAGER, ADMIN
  - 품질 기록 수정 API (PUT /api/quality-records/{id}) - 권한: USER, MANAGER, ADMIN
  - 품질 기록 삭제 API (DELETE /api/quality-records/{id}) - 권한: MANAGER, ADMIN
  - 품질 기록 평가 API (PUT /api/quality-records/{id}/evaluate) - 권한: USER, MANAGER, ADMIN
    - 로그인한 사용자가 전문가 평가를 남김
    - `evaluatedBy`는 User 외래키로 연결 (로그인한 사용자 ID)
  - 평가 필요 목록 조회 API (GET /api/quality-records/evaluation-required)

#### 7.2 NG 비율 자동 계산 (DB 트리거 또는 JPA 이벤트)
- [x] JPA `@PrePersist`, `@PreUpdate` 이벤트 리스너로 `ngRate` 자동 계산

##### 7.3 QualityRecord 통합 테스트
- [x] `qualityrecord/QualityRecordControllerTest.java` 생성
  - [x] 품질 기록 목록 조회 API 테스트 (GET /api/quality-records)
  - [x] 품질 기록 상세 조회 API 테스트 (GET /api/quality-records/{id})
  - [x] 품질 기록 생성 API 테스트 (POST /api/quality-records)
  - [x] 품질 기록 수정 API 테스트 (PUT /api/quality-records/{id})
  - [x] 품질 기록 삭제 API 테스트 (DELETE /api/quality-records/{id}) - 권한 검증
  - [x] `totalQuantity = okQuantity + ngQuantity` 자동 계산 검증 (엔티티 생성자에서 자동 계산되므로 별도 검증 실패 테스트 불필요)
  - [x] 같은 일별생산+공정 중복 생성 실패 테스트
  - [x] `ngRate` 자동 계산 검증 테스트
  - [x] `evaluationRequired` 계산 로직 테스트
    - [x] NG 비율 임계값 초과 시 평가 필요 플래그 설정 테스트
    - [x] 전일 대비 NG 비율 급증 시 평가 필요 플래그 설정 테스트
  - [x] 품질 기록 평가 API 테스트 (PUT /api/quality-records/{id}/evaluate)
    - [x] 로그인한 사용자가 평가 남기기 테스트
    - [x] 평가자 정보(evaluatedBy)가 User 외래키로 저장되는지 테스트
  - [x] 평가 필요 목록 조회 API 테스트 (GET /api/quality-records/evaluation-required)

---

### Phase 8: 통계 및 집계 기능

#### 8.1 공정별 NG 비율 통계
- [x] `qualityrecord/QualityRecordService.java`에 통계 메서드 추가
  - `getNgRateByProcess()` - 공정별 NG 비율 집계
  - 기간별 필터링 지원 (시작일, 종료일)
- [x] `qualityrecord/QualityRecordController.java`에 API 추가
  - GET `/api/quality-records/statistics/by-process`
  - Query Parameter: `startDate`, `endDate` (선택)

#### 8.2 부품별 NG 비율 통계
- [x] `qualityrecord/QualityRecordService.java`에 통계 메서드 추가
  - `getNgRateByItem()` - 부품별 NG 비율 집계
  - 기간별 필터링 지원
- [x] `qualityrecord/QualityRecordController.java`에 API 추가
  - GET `/api/quality-records/statistics/by-item`
  - Query Parameter: `startDate`, `endDate` (선택)

#### 8.3 평가 필요 목록 조회
- [x] `qualityrecord/QualityRecordService.java`에 메서드 추가
  - `getEvaluationRequiredList()` - `evaluationRequired = true`인 기록 조회
- [x] `qualityrecord/QualityRecordController.java`에 API 추가
  - GET `/api/quality-records/evaluation-required`

##### 8.4 통계 기능 통합 테스트
- [x] `qualityrecord/QualityRecordControllerTest.java`에 통계 테스트 추가
  - [x] 공정별 NG 비율 통계 API 테스트 (GET /api/quality-records/statistics/by-process)
  - [x] 부품별 NG 비율 통계 API 테스트 (GET /api/quality-records/statistics/by-item)
  - [x] 기간별 필터링 테스트 (startDate, endDate)
  - [x] 평가 필요 목록 조회 API 테스트 (GET /api/quality-records/evaluation-required)
  - [x] 통계 계산 정확성 검증 (수동 계산값과 비교)

---

### Phase 9: 유틸리티 및 개선

#### 9.1 API 응답 통일
- [x] 공통 응답 DTO 생성
  - `_core/util/Resp.java` (이미 구현 완료, 모든 API에서 사용 중)

#### 9.2 필터 (선택사항)
- [ ] `_core/filter/LoggingFilter.java` (요청/응답 로깅 - 개발 환경에서 유용할 수 있음)


### 10. API 문서작성
static/api.html 파일 만들어서 확인할 수 있게 해줘!!
restdoc 사용해줘!!

## 📝 참고사항

### 권한별 API 접근 제어
- **USER**: 조회, 생성, 수정 (일반 작업)
- **MANAGER**: USER 권한 + 부품/일별생산/품질기록 삭제
- **ADMIN**: 모든 권한

### 테이블명 규칙
- 엔티티명: `User`, `Item`, `Process` (단수형, `_tb` 없음)
- 테이블명: `user_tb`, `item_tb`, `process_tb` (단수형 + `_tb` 접미사)

### 트랜잭션 관리
- Service 클래스에 `@Transactional(readOnly = true)` 기본 적용
- 쓰기 작업이 있는 메서드는 `@Transactional` (readOnly 없음) 개별 지정

### 예외 처리
- `Exception400`: 잘못된 요청 (유효성 검사 실패 등)
- `Exception401`: 인증 실패 (로그인 필요)
- `Exception403`: 권한 없음
- `Exception500`: 서버 내부 오류

### 통합 테스트 작성 가이드
- **테스트 프레임워크**: Spring Boot Test, `@SpringBootTest`, `@AutoConfigureMockMvc`
- **데이터베이스**: 테스트용 H2 인메모리 DB 사용
- **인증**: `@WithMockUser` 또는 실제 JWT 토큰 생성하여 테스트
- **권한 검증**: 각 Role(USER, MANAGER, ADMIN)별 접근 권한 테스트 필수
- **테스트 데이터**: `@Sql` 또는 `@BeforeEach`로 초기 데이터 설정
- **트랜잭션**: `@Transactional`로 테스트 후 롤백하여 데이터 격리

---


