# Phase 1 → Phase 2 진행 상황 정리

## 📌 Phase 1에서 완료한 작업

### 1. 기본 인프라
- ✅ Spring Boot 프로젝트 설정
- ✅ JWT 인증/인가 시스템
- ✅ 전역 예외 처리 및 공통 응답 형식
- ✅ CORS 설정

### 2. 마스터 데이터 관리
- ✅ Process (공정) CRUD
- ✅ SystemCode (시스템 코드) 조회
- ✅ Item (부품) CRUD (권한별 접근 제어)

### 3. 비즈니스 데이터 관리
- ✅ DailyProduction (일별 생산 데이터) CRUD
- ✅ QualityRecord (품질 기록) CRUD

### 4. 기본 통계 API
- ✅ 공정별 NG 비율 통계 (`GET /api/quality-records/statistics/by-process`)
  - 기간별 필터링 지원 (startDate, endDate)
- ✅ 부품별 NG 비율 통계 (`GET /api/quality-records/statistics/by-item`)
  - 기간별 필터링 지원 (startDate, endDate)

### 5. 전문가 평가 시스템 (Phase 2 일정이었지만 Phase 1에서 완료)
- ✅ 평가 필요 여부 자동 계산 로직 (NG 비율 임계값, 전일 대비 급증)
- ✅ 평가 필요 목록 조회 API (`GET /api/quality-records/evaluation-required`)
- ✅ 전문가 평가 입력 API (`PUT /api/quality-records/{id}/evaluate`)
- ✅ 평가 이력 정보 포함 (evaluatedBy, evaluatedAt)

### 6. API 문서
- ✅ REST Docs를 통한 API 문서 자동 생성 (`/api.html`)

---

## 🔄 Phase 2에서 해야 할 작업 (중복 제거)

### ❌ Phase 2에서 제외 (이미 Phase 1에서 완료)
- ~~전문가 평가 입력/조회 API~~ ✅ **완료**
- ~~평가 필요 항목 조회 API~~ ✅ **완료**

### ✅ Phase 2에서 해야 할 작업

#### 1. 페이징 기능 추가 (우선순위 높음)
- [ ] 일별 생산 데이터 목록 페이징 (`GET /api/daily-productions`)
  - Pageable 인터페이스 사용
  - 기본 페이지 크기: 20
- [ ] 품질 기록 목록 페이징 (`GET /api/quality-records`)
  - Pageable 인터페이스 사용
  - 기본 페이지 크기: 20
  - 일별 필터링 지원 (`productionDate` 파라미터)
  - 월별 필터링 지원 (`year`, `month` 파라미터)

#### 2. 고급 통계 API (집계)
- [ ] 일별 → 월별 자동 집계 API
- [ ] 월별 → 연별 자동 집계 API
- [ ] 집계 데이터 캐싱 (선택사항, Redis)

#### 3. 고급 통계 API (추세 분석)
- [ ] 기간별 추세 분석 API
  - `GET /api/statistics/trend?itemId=1&processId=1&startDate=2025-01-01&endDate=2025-01-31&period=day`
  - 추세 방향 반환 (increasing/decreasing/stable)

#### 4. 고급 통계 API (비교 분석)
- [ ] 전월 대비 비교 API
  - `GET /api/statistics/compare-monthly?itemId=1&processId=1&currentMonth=2025-01`
- [ ] 전년 동월 대비 비교 API
  - `GET /api/statistics/compare-yearly?itemId=1&processId=1&currentDate=2025-01`

#### 5. 이상치 탐지
- [ ] 이상치 탐지 로직 (Java Service Layer)
  - 평균 ± 2 표준편차 기준

#### 6. 성능 최적화
- [ ] SQL 쿼리 최적화
- [ ] 인덱스 최적화
- [ ] 캐싱 전략 구현 (선택사항)

---

## 📝 참고사항

### 현재 API 상태 (api.html 기준)
- 모든 기본 CRUD API 정상 작동
- 통계 API 정상 작동 (기간별 필터링 지원)
- 전문가 평가 시스템 완전 구현
- **페이징 기능 미구현** (추가 필요)

### 페이징 구현 시 주의사항
- Spring Data JPA의 `Pageable` 인터페이스 사용
- 응답 형식: `Page<T>` 또는 커스텀 응답 DTO
- 기본값: page=0, size=20
- 정렬 옵션 지원 (sort 파라미터)

### Phase 2 우선순위
1. **페이징 기능 추가** (13, 14번 작업)
2. 고급 통계 API (집계, 추세, 비교)
3. 성능 최적화

---

**마지막 업데이트**: 2025-01-18