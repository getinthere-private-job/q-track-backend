# Q-Track 프로젝트

## 📦 프로젝트 레포지토리 구조

Q-Track 프로젝트는 다음과 같은 레포지토리들로 구성됩니다:

| 레포지토리 이름 | 설명 | 기술 스택 | 개발 단계 |
|---------------|------|----------|----------|
| **q-track-backend** | Spring Boot 메인 API 서버 | Java, Spring Boot, MySQL | Phase 1 |
| **q-track-frontend** | React 웹 대시보드 | React, TypeScript, Chart.js | Phase 1 |
| **q-track-ai** | FastAPI AI 서버 | Python, FastAPI, Ollama, RAG | Phase 3 |
| **q-track-mobile** | Flutter 모바일 앱 | Flutter, Dart | Phase 4 |
| **q-track-infra** | Docker 및 인프라 설정 | Docker, Docker Compose | Phase 1 |

### 레포지토리 설명

#### q-track-backend
- **역할**: 부품, 공정, 일별 생산 데이터 CRUD 및 통계 API 제공
- **주요 기능**: 
  - Item, Process, DailyProduction, QualityRecord CRUD
  - 공정별/부품별 NG 비율 통계
  - 전문가 평가 필요 여부 계산
- **기술**: Spring Boot, MySQL

#### q-track-frontend
- **역할**: 웹 대시보드 및 데이터 입력 화면
- **주요 기능**:
  - 일별 OK/NG 데이터 입력
  - 공정별/부품별 NG 비율 그래프
  - 실시간 대시보드
- **기술**: React, Chart.js/Recharts

#### q-track-ai
- **역할**: AI 전용 서버 (RAG, LLM, MCP)
- **주요 기능**:
  - RAG 시스템 (도메인 지식 검색)
  - LLM 통합 (Ollama 또는 OpenAI API)
  - MCP 그래프 자동 생성
  - 음성 처리 API
- **기술**: FastAPI, Ollama, LangChain/LlamaIndex

#### q-track-mobile
- **역할**: 모바일 앱 (음성 인식/합성)
- **주요 기능**:
  - 음성 인식/합성
  - 모바일 대시보드
  - 그래프 시각화
- **기술**: Flutter, speech_to_text, flutter_tts

#### q-track-infra
- **역할**: Docker 및 인프라 설정
- **주요 기능**:
  - docker-compose.yml
  - 개발/프로덕션 환경 설정
  - 배포 스크립트
- **기술**: Docker, Docker Compose

---

# Phase 1 개발 필수 정보 요약

## 📌 목차
1. [Phase 1 개발 일정](#1-phase-1-개발-일정)
2. [기술 스택](#2-기술-스택)
3. [비즈니스 이해](#3-비즈니스-이해)
4. [도메인 정보](#4-도메인-정보)
5. [엔티티 설계](#5-엔티티-설계)

---

## 1. Phase 1 개발 일정

### 목표
기본적인 데이터 입력, 조회, 시각화 기능을 가진 프로토타입 완성 (1-2주)

### 주차별 작업

#### 1주차: 인프라 구축 및 기본 CRUD

**Day 1-2: 개발 환경 설정**
- [ ] Spring Boot 프로젝트 초기화
- [ ] MySQL 데이터베이스 설계 (스키마 작성)
- [ ] React 프로젝트 초기화
- [ ] Docker Compose 로컬 개발 환경 구성

**Day 3-5: Spring Boot API 개발**
- [ ] 엔티티 클래스 작성 (Item, Process, DailyProduction, QualityRecord, SystemCode)
- [ ] Repository 레이어 구현
- [ ] Service 레이어 구현
- [ ] Controller 레이어 구현 (REST API)
- [ ] 기본 CRUD API 테스트
  - 부품(Item) CRUD
  - 공정(Process) CRUD
  - 일별 생산 데이터 입력/조회
  - 품질 기록 입력/조회

**Day 6-7: React 기본 화면 구성**
- [ ] 라우팅 설정
- [ ] 데이터 입력 화면 (일별 OK/NG 기록)
- [ ] 기본 대시보드 레이아웃

#### 2주차: 기본 통계 및 시각화

**Day 8-10: 기본 통계 API 및 전문가 평가 로직 개발**
- [ ] 공정별 NG 비율 계산 API
- [ ] 부품별 NG 비율 계산 API
- [ ] 기간별 집계 API (일/월)
- [ ] 전문가 평가 필요 여부 자동 계산 로직:
  - NG 비율 임계값 초과 감지 (업계 평균 0.5% 기준)
  - 전일 대비 NG 비율 급증 감지 (2배 이상)
  - `evaluationRequired` 플래그 자동 설정

**Day 11-14: 기본 시각화 구현**
- [ ] React에서 Chart.js 또는 Recharts 연동
- [ ] 공정별 NG 비율 그래프 (막대 그래프)
- [ ] 부품별 NG 비율 그래프 (막대 그래프)
- [ ] 대시보드에 그래프 표시

### Phase 1 완료 기준
- ✅ 일별 생산 데이터 입력 가능
- ✅ 기본 통계 (공정별/부품별 NG 비율) 조회 가능
- ✅ 기본 그래프 시각화 가능
- ✅ 프로토타입 동작 확인

---

## 2. 기술 스택

### Phase 1에서 사용하는 기술

#### 백엔드: Spring Boot
- **역할**: 
  - 데이터 CRUD (부품, 공정, 일별 생산 데이터)
  - 통계 계산 및 집계 (공정별/부품별 NG 비율)
  - 대시보드 데이터 제공 (MySQL 쿼리로 모든 통계 처리)
- **기술**: Java, Spring Boot Framework
- **⚠️ 중요**: Phase 1에서는 FastAPI 불필요 (Phase 3에서 AI 기능 전용으로 추가)

#### 프론트엔드: React
- **역할**: 
  - 웹 대시보드 (Spring Boot API 사용)
  - 그래프 시각화 (Chart.js 또는 Recharts)
- **기술**: React, Chart.js 또는 Recharts
- **데이터 흐름**: React → Spring Boot → MySQL

#### 데이터베이스: MySQL
- **역할**: 생산 데이터 저장 (부품 정보, 공정 데이터, 일별 OK/NG 기록 등)
- **라이센스**: GPL - 무료
- **배포**: AWS RDS MySQL (Phase 1에서는 로컬 개발용)

---

## 3. 비즈니스 이해

### 관리 대상
- **생산 부품**: 총 24종의 자동차 부품 (엔진/변속기/유체 제어/공압/센서 계열)
  - 예: 8속, P2, DCT, 공압솔, 람다3, 3WAY 등
- **공정 단계**: 부품 하나당 동일한 3단계 흐름
  1. **작업(W) 공정** - 초기 가공 작업
  2. **제조(P) 공정** - 특정 제조 공정
  3. **검사(검) 공정** - 품질 검사/검증
- **품질 결과**: 각 단계마다 OK(합격) 또는 NG(불량) 기록

### 비즈니스 목적
**"감"이 아니라 "수치"로 판단하고 싶음**

관리자가 답하고 싶은 질문:
- 어느 공정에서 불량이 많이 발생하는가?
- 어떤 부품이 문제를 많이 일으키는가?
- 이번 달 불량률은 지난달보다 좋아졌는가?
- 특정 부품의 NG 비율이 기준을 초과했는가?

### 핵심 비즈니스 지표 (KPI)
1. 공정별 NG 비율
2. 부품별 NG 비율
3. 기간별 변화 (일/월/연)
4. 평균 대비 이상치
5. 추세 (증가/감소)

**📌 불량률(NG)은 곧 비용** (재작업 비용, 폐기 비용, 품질 신뢰도)

---

## 4. 도메인 정보

### 공정 코드 의미

#### W (작업/가공 공정)
- **Work** (작업) - 일반적인 작업/가공 단계
- 부품이 초기 제조 또는 가공 작업을 거치는 **작업/가공 단계**

#### P (공정/생산 공정)
- **Process** (공정) - 생산 공정 단계
- 부품이 특정 제조 공정을 거치는 **공정/생산 단계**

#### 검 (검사 공정)
- **검사** (Inspection) - 품질 검사/확인 단계
- 최종 승인 전에 품질 관리를 위해 부품을 검사하는 **검사/품질 확인 단계**

**공정 흐름**: `W (작업/가공) → P (공정/생산) → 검 (검사/검증)`

### OK, NG 의미

#### OK (합격/정상)
- 해당 공정에서 품질 기준을 통과한 제품/부품
- 정상적으로 처리되어 다음 공정으로 진행 가능한 상태

#### NG (불합격/불량)
- 해당 공정에서 품질 기준을 통과하지 못한 제품/부품
- 불량품으로 판정되어 재작업 또는 폐기 대상

### 업계 평균 불량률
- **자동차 부품 업계 평균**: 0.1-0.5%
- **임계값**: 0.5% 이상 시 평가 필요

---

## 5. 엔티티 설계

### 엔티티 관계도

```
Item (부품) - 24개 자동차 부품
  └── DailyProduction (일별 생산) - 부품별/일별 집계
        └── QualityRecord (품질 기록) - 공정별 상세 OK/NG
              └── Process (공정) - W, P, 검 마스터 데이터

SystemCode (시스템 코드) - 업계 평균, 임계값 등 시스템 설정
```

### 테이블 구조

#### 1. Item (부품)

| 필드명 | 타입 | 설명 | 예시 |
|--------|------|------|------|
| id | BIGINT | 부품 고유 ID | 1, 2, 3 |
| code | VARCHAR(50) | 부품 코드 (UNIQUE) | "P2", "8속", "DCT" |
| name | VARCHAR(100) | 부품 이름 | "P2 부품" |
| description | TEXT | 부품 설명 | "엔진 제어 부품" |
| category | VARCHAR(50) | 부품 카테고리 | "엔진", "변속기" |

#### 2. Process (공정)

| 필드명 | 타입 | 설명 | 예시 |
|--------|------|------|------|
| id | BIGINT | 공정 고유 ID | 1, 2, 3 |
| code | VARCHAR(10) | 공정 코드 (UNIQUE) | "W", "P", "검" |
| name | VARCHAR(50) | 공정 이름 | "작업", "제조", "검사" |
| `order` | INT | 공정 순서 (UNIQUE) | 1, 2, 3 |

#### 3. DailyProduction (일별 생산)

| 필드명 | 타입 | 설명 | 예시 |
|--------|------|------|------|
| id | BIGINT | 일별 생산 고유 ID | 1, 2, 3 |
| itemId | BIGINT | 부품 ID (FK → Item.id) | 1, 2, 3 |
| productionDate | DATE | 생산 일자 | 2025-01-15 |
| totalQuantity | INT | 총 생산 수량 | 89342 |
| **UNIQUE**: (itemId, productionDate) | | 하루에 같은 부품은 하나만 | |

#### 4. QualityRecord (품질 기록)

| 필드명 | 타입 | 설명 | 예시 |
|--------|------|------|------|
| id | BIGINT | 품질 기록 고유 ID | 1, 2, 3 |
| dailyProductionId | BIGINT | 일별 생산 ID (FK → DailyProduction.id) | 1, 2, 3 |
| processId | BIGINT | 공정 ID (FK → Process.id) | 1 (W), 2 (P), 3 (검) |
| okQuantity | INT | OK 수량 | 88567 |
| ngQuantity | INT | NG 수량 | 775 |
| totalQuantity | INT | 총 수량 (okQuantity + ngQuantity) | 89342 |
| ngRate | DECIMAL(5,2) | NG 비율 (%) - **트리거로 자동 계산** | 0.87 |
| expertEvaluation | TEXT | 전문가 평가/분석 내용 (NULL 가능) | "재료 품질 이슈로 판단됨" |
| evaluationRequired | BOOLEAN | 평가 필요 여부 - **애플리케이션 로직으로 계산** | TRUE, FALSE |
| evaluationReason | VARCHAR(200) | 평가 필요 이유 | "임계값 초과" |
| evaluatedAt | TIMESTAMP | 평가 일시 | 2025-01-15 14:30:00 |
| evaluatedBy | VARCHAR(50) | 평가자 | "expert001" |
| **UNIQUE**: (dailyProductionId, processId) | | 하루에 같은 공정은 하나만 | |

#### 5. SystemCode (시스템 코드)

| 필드명 | 타입 | 설명 | 예시 |
|--------|------|------|------|
| id | BIGINT | 코드 고유 ID | 1, 2, 3 |
| codeGroup | VARCHAR(50) | 코드 그룹 | "INDUSTRY_AVERAGE", "EVALUATION" |
| codeKey | VARCHAR(50) | 코드 키 | "NG_RATE_THRESHOLD" |
| codeValue | VARCHAR(200) | 코드 값 | "0.5" |
| description | TEXT | 코드 설명 | "업계 평균 NG 비율 (%)" |
| isActive | BOOLEAN | 사용 여부 | TRUE |
| **UNIQUE**: (codeGroup, codeKey) | | 코드 그룹+키 조합은 유일 | |

### 주요 DDL

```sql
-- Item (부품) 테이블
CREATE TABLE item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    category VARCHAR(50),
    createdAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updatedAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_item_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Process (공정) 테이블
CREATE TABLE process (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(10) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    description TEXT,
    `order` INT NOT NULL UNIQUE,
    createdAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updatedAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- DailyProduction (일별 생산) 테이블
CREATE TABLE daily_production (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    itemId BIGINT NOT NULL,
    productionDate DATE NOT NULL,
    totalQuantity INT NOT NULL DEFAULT 0,
    createdAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updatedAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX idx_daily_production_item_date (itemId, productionDate),
    CONSTRAINT fk_daily_production_item
        FOREIGN KEY (itemId) REFERENCES item(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- QualityRecord (품질 기록) 테이블
CREATE TABLE quality_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    dailyProductionId BIGINT NOT NULL,
    processId BIGINT NOT NULL,
    okQuantity INT NOT NULL DEFAULT 0,
    ngQuantity INT NOT NULL DEFAULT 0,
    totalQuantity INT NOT NULL DEFAULT 0,
    ngRate DECIMAL(5,2),
    expertEvaluation TEXT,
    evaluationRequired BOOLEAN NOT NULL DEFAULT FALSE,
    evaluationReason VARCHAR(200),
    evaluatedAt TIMESTAMP NULL,
    evaluatedBy VARCHAR(50),
    createdAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updatedAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX idx_quality_record_daily_process (dailyProductionId, processId),
    CONSTRAINT fk_quality_record_daily_production
        FOREIGN KEY (dailyProductionId) REFERENCES daily_production(id) ON DELETE CASCADE,
    CONSTRAINT fk_quality_record_process
        FOREIGN KEY (processId) REFERENCES process(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- SystemCode (시스템 코드) 테이블
CREATE TABLE system_code (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    codeGroup VARCHAR(50) NOT NULL,
    codeKey VARCHAR(50) NOT NULL,
    codeValue VARCHAR(200) NOT NULL,
    description TEXT,
    isActive BOOLEAN NOT NULL DEFAULT TRUE,
    createdAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updatedAt TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX idx_system_code_group_key (codeGroup, codeKey)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### 트리거 (NG 비율 자동 계산)

```sql
-- NG 비율 자동 계산 트리거
DELIMITER //
CREATE TRIGGER trg_quality_record_ng_rate
BEFORE INSERT ON quality_record
FOR EACH ROW
BEGIN
    IF NEW.totalQuantity > 0 THEN
        SET NEW.ngRate = (NEW.ngQuantity / NEW.totalQuantity) * 100;
    END IF;
END//

CREATE TRIGGER trg_quality_record_ng_rate_update
BEFORE UPDATE ON quality_record
FOR EACH ROW
BEGIN
    IF NEW.totalQuantity > 0 THEN
        SET NEW.ngRate = (NEW.ngQuantity / NEW.totalQuantity) * 100;
    END IF;
END//
DELIMITER ;
```

### 초기 데이터 삽입

```sql
-- Process (공정) 초기 데이터
INSERT INTO process (code, name, description, `order`) VALUES
('W', '작업', '초기 가공 작업 공정', 1),
('P', '제조', '특정 제조 공정', 2),
('검', '검사', '품질 검사 및 검증 공정', 3);

-- SystemCode (시스템 코드) 초기 데이터
INSERT INTO system_code (codeGroup, codeKey, codeValue, description) VALUES
('INDUSTRY_AVERAGE', 'NG_RATE_THRESHOLD', '0.5', '업계 평균 NG 비율 (%)'),
('INDUSTRY_AVERAGE', 'NG_RATE_ALERT', '1.0', 'NG 비율 알림 임계값 (%)'),
('EVALUATION', 'INCREASE_RATE_THRESHOLD', '2.0', '전일 대비 NG 비율 급증 기준 (배)');

-- Item (부품) 초기 데이터 예시 (24개 부품 중 일부)
INSERT INTO item (code, name, category) VALUES
('P2', 'P2 부품', '엔진'),
('8속', '8속 부품', '변속기'),
('DCT', '듀얼 클러치 트랜스미션', '변속기'),
('공압솔', '공압 솔레노이드', '공압'),
('람다3', '람다 센서 3', '센서'),
('3WAY', '3-Way 밸브', '유체 제어');
-- 나머지 18개 부품 추가...
```

### 평가 필요 여부 계산 로직 (Java 예시)

```java
// 평가 필요 여부 계산 (애플리케이션 로직)
public void calculateEvaluationRequired(QualityRecord record) {
    double ngRate = record.getNgRate();
    boolean evaluationRequired = false;
    String evaluationReason = null;
    
    // SystemCode에서 기준값 조회
    double industryAverage = Double.parseDouble(
        systemCodeService.getCodeValue("INDUSTRY_AVERAGE", "NG_RATE_THRESHOLD")
    ); // 0.5
    
    double alertThreshold = Double.parseDouble(
        systemCodeService.getCodeValue("INDUSTRY_AVERAGE", "NG_RATE_ALERT")
    ); // 1.0
    
    double increaseThreshold = Double.parseDouble(
        systemCodeService.getCodeValue("EVALUATION", "INCREASE_RATE_THRESHOLD")
    ); // 2.0
    
    // 1. 필수 평가: 임계값 초과
    if (ngRate > industryAverage || ngRate > alertThreshold) {
        evaluationRequired = true;
        evaluationReason = "임계값 초과";
    }
    
    // 2. 권장 평가: 전일 대비 NG 비율 급증
    double previousNgRate = getPreviousDayNgRate(record);
    if (previousNgRate > 0 && ngRate / previousNgRate >= increaseThreshold) {
        evaluationRequired = true;
        evaluationReason = "이상 패턴 감지";
    }
    
    record.setEvaluationRequired(evaluationRequired);
    record.setEvaluationReason(evaluationReason);
}
```

### 핵심 비즈니스 로직

#### NG 비율 계산
- **방식**: DB 트리거로 자동 계산
- **공식**: `ngRate = (ngQuantity / totalQuantity) * 100`
- **이유**: 계산식이 고정되어 있어 트리거로 처리

#### 평가 필요 여부 계산
- **방식**: 애플리케이션 로직으로 계산
- **이유**: 업계 평균 등 기준값이 변경될 수 있어 코드 테이블에서 동적으로 조회

#### 공정별 NG 비율 계산
```sql
SELECT 
    p.code as processCode,
    SUM(qr.ngQuantity) / SUM(qr.totalQuantity) * 100 as ngRate
FROM quality_record qr
JOIN process p ON qr.processId = p.id
GROUP BY p.code
```

#### 부품별 NG 비율 계산
```sql
SELECT 
    i.code as itemCode,
    SUM(qr.ngQuantity) / SUM(qr.totalQuantity) * 100 as ngRate
FROM quality_record qr
JOIN daily_production dp ON qr.dailyProductionId = dp.id
JOIN item i ON dp.itemId = i.id
GROUP BY i.code
```

---

## 📌 개발 시 주의사항

### 데이터 무결성
- `totalQuantity = okQuantity + ngQuantity` 검증 필요
- `quality_record`의 `dailyProductionId + processId`는 UNIQUE (하루에 같은 공정은 하나만)
- `daily_production`의 `itemId + productionDate`는 UNIQUE (하루에 같은 부품은 하나만)

### 성능 최적화
- 인덱스 적절히 활용 (itemId, productionDate, processId, evaluationRequired, ngRate)
- 통계 계산 시 집계 쿼리 최적화

### 전문가 평가
- Phase 1에서는 전문가 평가 입력 기능은 구현하지 않아도 됨
- 평가 필요 여부(`evaluationRequired`) 계산 로직만 구현
- 전문가 평가 입력/조회는 Phase 2에서 구현

---

## ✅ Phase 1 체크리스트

### Week 1
- [ ] Spring Boot 프로젝트 초기화
- [ ] MySQL 스키마 작성 및 초기 데이터 삽입
- [ ] 엔티티 클래스 작성 (5개 테이블)
- [ ] Repository/Service/Controller 레이어 구현
- [ ] CRUD API 테스트
- [ ] React 프로젝트 초기화
- [ ] 데이터 입력 화면 구현

### Week 2
- [ ] 공정별/부품별 NG 비율 계산 API
- [ ] 평가 필요 여부 계산 로직
- [ ] Chart.js 또는 Recharts 연동
- [ ] 공정별/부품별 NG 비율 그래프
- [ ] 대시보드 구현

---

**이 문서만으로 Phase 1 개발이 가능합니다!** 🚀
