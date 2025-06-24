# Placely Backend Services

Placely 백엔드는 마이크로서비스 아키텍처로 구성된 Spring Boot 기반의 서비스들입니다.

## 🏗️ 프로젝트 구조

```
back-end/
├── common/          # 공통 모듈 (JWT, 암호화, 유틸리티)
├── auth/           # 인증/인가 서비스
├── pos/            # POS 시스템 서비스
├── ai/             # AI 추천/분석 서비스
├── gateway/        # API 게이트웨이
└── database/       # 데이터베이스 스키마
```

## 🚀 빠른 시작

### 1. 환경 요구사항

- Java 17+
- Kotlin 1.9+
- Oracle Database (Cloud)
- Redis
- Gradle 8.0+

### 2. 데이터베이스 설정

```bash
# Oracle 데이터베이스 스키마 생성
cd database/oracle
sqlplus username/password@host:port/service_name @run_all_tables.sql
```

### 3. 환경변수 설정

```bash
# 필수 환경변수
export DB_USERNAME=your_db_username
export DB_PASSWORD=your_db_password
export JWT_SECRET=your_jwt_secret_key
export CRYPTO_KEY=your_aes_encryption_key
export REDIS_HOST=your_redis_host
export REDIS_PASSWORD=your_redis_password
```

### 4. 서비스 실행

```bash
# 모든 서비스 빌드
./gradlew build

# Auth 서비스 실행
./gradlew :auth:bootRun

# 다른 터미널에서 다른 서비스들 실행
./gradlew :pos:bootRun
./gradlew :ai:bootRun
./gradlew :gateway:bootRun
```

## 📋 서비스별 상세 정보

### 🔐 Auth Service (포트: 8081)

인증/인가를 담당하는 서비스

**주요 기능:**

- 사용자 회원가입/로그인
- JWT 토큰 발급/갱신
- 비밀번호 변경
- 사용자 정보 관리
- 계정 잠금/해제

**API 엔드포인트:**

```
POST /auth/api/auth/login          # 로그인
POST /auth/api/auth/signup         # 회원가입
POST /auth/api/auth/refresh        # 토큰 갱신
PUT  /auth/api/auth/password       # 비밀번호 변경
POST /auth/api/auth/logout         # 로그아웃
GET  /auth/api/auth/me            # 사용자 정보 조회
GET  /auth/api/auth/check/username # 사용자명 중복 확인
GET  /auth/api/auth/check/email    # 이메일 중복 확인
POST /auth/api/auth/validate/password # 비밀번호 강도 검증
```

### 🛍️ POS Service (포트: 8082)

POS 시스템을 담당하는 서비스 (구현 예정)

### 🤖 AI Service (포트: 8083)

AI 추천/분석을 담당하는 서비스 (구현 예정)

### 🌐 Gateway Service (포트: 8080)

API 게이트웨이 서비스 (구현 예정)

## 🔧 공통 모듈 (Common)

### JWT 유틸리티

```kotlin
// JWT 토큰 생성
val accessToken = jwtTokenProvider.generateAccessToken(userId, userRole)
val refreshToken = jwtTokenProvider.generateRefreshToken(userId)

// JWT 토큰 검증
val isValid = jwtTokenProvider.validateToken(token)
val userId = jwtTokenProvider.getUserIdFromToken(token)
```

### 비밀번호 암호화

```kotlin
// 비밀번호 해싱
val hashedPassword = passwordEncoder.encode(rawPassword)

// 비밀번호 검증
val isMatched = passwordEncoder.matches(rawPassword, hashedPassword)

// 비밀번호 강도 검증
val isStrong = passwordEncoder.validatePasswordStrength(password)
```

### AES 암복호화

```kotlin
// 데이터 암호화
val encryptedData = cryptoUtil.encrypt(plainText, secretKey)

// 데이터 복호화
val decryptedData = cryptoUtil.decrypt(encryptedData, secretKey)

// 개인정보 암호화 (환경변수 키 사용)
val encryptedPersonalInfo = cryptoUtil.encryptPersonalInfo(data, keyString)
```

## 🗄️ 데이터베이스 스키마

### 주요 테이블

- **AUTH_USERS**: 사용자 인증 정보
- **AUTH_TOKENS**: JWT 토큰 관리
- **POS_STORES**: 매장 정보
- **POS_PRODUCTS**: 상품 정보
- **POS_SALES**: 판매 기록
- **AI_RECOMMENDATIONS**: AI 추천 이력

자세한 스키마 정보는 [데이터 모델 ERD 문서](../Placely.wiki/📊-데이터-모델-ERD.md)를 참조하세요.

## 🧪 테스트

```bash
# 전체 테스트 실행
./gradlew test

# 특정 서비스 테스트
./gradlew :auth:test
./gradlew :pos:test
```

## 📊 모니터링

### 헬스 체크

```bash
# Auth 서비스 헬스 체크
curl http://localhost:8081/auth/actuator/health

# 메트릭 확인
curl http://localhost:8081/auth/actuator/metrics
```

## 🔒 보안 설정

### JWT 토큰 보안

- HS512 알고리즘 사용
- Access Token: 30분 만료
- Refresh Token: 7일 만료
- 토큰 해시를 DB에 저장하여 보안 강화

### 비밀번호 보안

- BCrypt 암호화 (강도 12)
- 비밀번호 강도 검증 (8자 이상, 대소문자/숫자/특수문자 포함)
- 로그인 실패 5회 시 30분 계정 잠금

### 개인정보 암호화

- AES-GCM 256비트 암호화
- 환경변수로 암호화 키 관리
- 개인정보(전화번호, 주소 등) 암호화 저장

## 🚀 배포

### 개발 환경

```bash
./gradlew :auth:bootRun --args='--spring.profiles.active=dev'
```

### 운영 환경

```bash
./gradlew build
java -jar auth/build/libs/auth-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

## 📝 API 문서

각 서비스의 API 문서는 Swagger UI에서 확인할 수 있습니다:

- Auth Service: http://localhost:8081/auth/swagger-ui.html

## 🤝 기여 가이드

1. 브랜치 생성: `git checkout -b feature/new-feature`
2. 변경사항 커밋: `git commit -am 'Add new feature'`
3. 브랜치 푸시: `git push origin feature/new-feature`
4. Pull Request 생성

## 📄 라이선스

이 프로젝트는 MIT 라이선스를 따릅니다.
