# 개발 규칙 (Development Rules)

## 보안 (Security)

### 1. Spring Security 사용
- Spring Security를 사용하여 인증 및 인가 처리

### 2. JWT 토큰 인증
- JWT(JSON Web Token) 기반 인증 사용
- Access Token만 사용 (Refresh Token 사용 안 함)
- Access Token 유효 기간: 5일

### 3. Security Context 정보
- 로그인 후 Spring Security Context에는 JWT에서 파싱한 정보만 저장
- 저장 정보:
  - User 테이블의 `id`
  - User 테이블의 `role`
- Role은 중복 불가 (하나의 Role만 가짐)

### 4. 권한 (Role) 체계
- **ROLE_USER**: 일반 사용자
- **ROLE_MANAGER**: 매니저 (회사 사장님 같은 역할)
- **ROLE_ADMIN**: 관리자 (개발자)

## 데이터베이스 & JPA

### 5. Open Session In View 비활성화
- `spring.jpa.open-in-view=false` 설정
- N+1 문제 방지 및 명시적인 트랜잭션 관리

### 6. 트랜잭션 관리
- 서비스 클래스 위에 기본으로 `@Transactional(readOnly = true)` 적용

## 프로젝트 구조

### 7. 폴더 구조 (테이블 기준)
- MSA 아님 (모놀리식 구조)
- 테이블별로 폴더 구성
- 각 테이블 폴더 내부에 다음 파일들 포함:
  - `Controller`
  - `Service`
  - `Repository`
  - `Entity`
  - `Enum`
- `_core` 패키지 생성하여 공통 설정 및 유틸리티 관리
  - 시큐리티 관련 설정 (Security Config, JWT 필터, 인증/인가 처리)
  - 유틸리티 클래스 (공통 유틸리티 함수)
  - 유효성 검사 (Validation 관련 설정 및 커스텀 Validator)
  - 에러 처리 (`errors/` 패키지)
    - `CustomValidationHandler.java`: 유효성 검사 예외 핸들러
    - `GlobalExceptionHandler.java`: 전역 예외 핸들러
    - `ex/` 패키지: 커스텀 예외 클래스 (RuntimeException 상속)
      - `Exception400.java`, `Exception401.java`, `Exception403.java`, `Exception500.java`
  - 필터 (요청/응답 필터, 인코딩 필터 등)
  - 기타 공통 설정

예시:
```
_core/
  - security/
    - SecurityConfig.java
    - JwtFilter.java
    - JwtUtil.java
  - util/
    - DateUtil.java
    - StringUtil.java
  - validation/
    - CustomValidator.java
  - filter/
    - LoggingFilter.java
  - errors/
    - CustomValidationHandler.java
    - GlobalExceptionHandler.java
    - ex/
      - Exception400.java
      - Exception401.java
      - Exception403.java
      - Exception500.java

user/
  - UserController.java
  - UserService.java
  - UserRepository.java
  - User.java
```

### 8. 네이밍 규칙
- **엔티티명**: 
  - 예: `User`, `Item`, `Process`
- **테이블명**: 테이블명 끝에 `_tb` 접미사 사용복수형 사용 안 함 (단수형 사용)
  - 예: `user_tb` (O), `users` (X)
  - 예: `item_tb` (O), `items` (X)

## 아키텍처 & 설계 원칙

### 9. 레이어 방향
- Controller → Service → Repository 단방향 흐름
- 의존성은 위에서 아래로만 흐름

### 10. 인터페이스 사용 제한
- 불필요한 인터페이스 생성 금지
- 구현체만 있는 인터페이스는 생성하지 않음
- DIP(의존 역전 원칙)를 과도하게 따르지 않음
- 필요할 때만 인터페이스 사용

## 필수 구현 사항

### 11. User 테이블
- 인증을 위한 User 테이블 반드시 구현
- 필수 필드:
  - `id`: 사용자 ID
  - `role`: 사용자 권한 (USER, MANAGER, ADMIN 중 하나)


### 12. 통합테스트 코드 필요

### 13. DTO만드는 규칙
UserRequest, UserResponse 클래스안에 record로 만들기

### 14. 응답 공통 DTO 만드는 규칙
import lombok.Data;
import org.springframework.http.*;

@Data
public class Resp<T> {
    private Integer status;
    private String msg;
    private T body;

    public Resp(Integer status, String msg, T body) {
        this.status = status;
        this.msg = msg;
        this.body = body;
    }

    public static <B> ResponseEntity<Resp<B>> ok(B body) {
        Resp<B> resp = new Resp<>(200, "성공", body);
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    public static ResponseEntity<?> fail(HttpStatus status, String msg) {
        Resp<?> resp = new Resp<>(status.value(), msg, null);
        return new ResponseEntity<>(resp, status);
    }
}