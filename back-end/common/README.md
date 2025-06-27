# 🔐 Placely Common 모듈

Spring Boot 멀티모듈 프로젝트에서 공통으로 사용되는 기능들을 제공합니다.

## 📦 **제공 기능**

### 1. **JPA Entity Listener 기반 자동 암복호화**

- 개발자가 깜빡할 수 없는 완전 자동화된 암복호화 시스템
- Entity에 `@EntityListeners`만 추가하면 모든 DB 작업에 자동 적용

### 2. **JWT 토큰 관리**

- JWT 생성, 검증, 파싱 기능
- 설정 기반 토큰 관리

### 3. **공통 설정 관리**

- 암호화 키, Salt 등 보안 설정
- JWT 시크릿 키 등 인증 설정

## 🏗️ **모듈 구조**

```
common/
├── config/                           # 설정 관리
│   ├── CommonAutoConfiguration.kt    # 자동 설정
│   ├── CryptoProperties.kt          # 암호화 설정
│   └── JwtProperties.kt             # JWT 설정
│
├── security/
│   ├── annotation/                  # 암복호화 어노테이션
│   │   ├── EncryptField.kt         # 암호화 필드 어노테이션
│   │   └── HashField.kt            # 해싱 필드 어노테이션
│   │
│   ├── crypto/                     # 암복호화 기능
│   │   ├── CryptoConstants.kt      # 암호화 상수
│   │   └── CryptoUtil.kt           # 암복호화 유틸리티
│   │
│   ├── jpa/                        # JPA Entity Listener
│   │   ├── CryptoEntityListener.kt # 자동 암복호화 처리
│   │   └── README.md               # 사용법 가이드
│   │
│   ├── jwt/                        # JWT 관리
│   │   └── JwtUtil.kt              # JWT 유틸리티
│   │
│   ├── exception/                  # 보안 예외
│   │   └── SecurityException.kt    # 보안 관련 예외
│   │
│   └── AuthConstants.kt            # 인증 상수
│
└── resources/
    └── application-common.yml       # 공통 설정
```

## 🚀 **빠른 시작**

### 1. **의존성 추가**

```kotlin
// build.gradle
implementation project(':common')
```

### 2. **설정 파일 적용**

```yaml
# application.yml
spring:
  profiles:
    include: [common, env]
```

### 3. **Entity에 암복호화 적용**

```kotlin
@Entity
@Table(name = "customers")
@EntityListeners(CryptoEntityListener::class)  // 🔥 이것만 추가!
data class Customer(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val customerId: Long = 0,

    // 암호화 + 검색 가능
    @EncryptField(searchable = true, searchHashField = "name_hash")
    @Column(name = "customer_name", length = 500)
    var customerName: String = "",

    // 검색용 해시 필드 (자동 생성됨)
    @Column(name = "name_hash")
    var nameHash: String = "",

    // 해싱 (비밀번호)
    @HashField
    @Column(name = "password")
    var password: String = ""
)
```

### 4. **Service는 일반 JPA와 동일**

```kotlin
@Service
class CustomerService {

    @Autowired
    private lateinit var customerRepository: CustomerRepository

    // ✅ 어노테이션 없이도 자동 암호화!
    fun saveCustomer(customer: Customer): Customer {
        return customerRepository.save(customer)
    }

    // ✅ 어노테이션 없이도 자동 복호화!
    fun getCustomer(id: Long): Customer? {
        return customerRepository.findById(id).orElse(null)
    }
}
```

## 🔧 **핵심 기능**

### ✅ **자동 암복호화**

- **저장 시**: `@PrePersist`, `@PreUpdate`에서 자동 암호화
- **조회 시**: `@PostLoad`에서 자동 복호화
- **실수 방지**: 개발자가 깜빡할 수 없는 구조

### ✅ **검색 가능한 암호화**

- 암호화된 데이터도 해시 기반으로 검색 가능
- `@EncryptField(searchable = true)`로 검색 해시 자동 생성

### ✅ **안전한 비밀번호 저장**

- `@HashField`로 비가역적 해싱
- Salt 기반 보안 강화

## 📊 **처리 플로우**

### 저장 시

```
평문 입력 → Entity Listener → 암호화/해싱 → 검색 해시 생성 → DB 저장
```

### 조회 시

```
DB 조회 → Entity Listener → 복호화 → 평문 반환
```

### 검색 시

```
검색어 → 해싱 → 해시로 DB 검색 → 복호화된 결과 반환
```

## ⚙️ **설정**

### application-common.yml

```yaml
placely:
  security:
    crypto:
      # 암호화 키 (Base64, 32바이트)
      encryption-key: UGxhY2VseVNlY3JldEtleTIwMjRGb3JEZXZlbG9wbWVudA==
      # 검색용 Salt
      search-salt: PlacelySearchSalt2025Dev
      # 개인정보용 Salt
      personal-salt: PlacelyPersonalSalt2025Dev
    jwt:
      # JWT 시크릿 키
      secret: placely-jwt-secret-key-for-development-2025-change-in-production
      # 토큰 만료 시간 (밀리초)
      expiration: 86400000
```

## 🚨 **주의사항**

### 1. **DB 컬럼 길이 조정**

```sql
-- 암호화된 데이터는 길이가 증가함
ALTER TABLE customers MODIFY customer_name VARCHAR(500);
ALTER TABLE customers MODIFY address VARCHAR(1000);
```

### 2. **해시 필드 추가**

```sql
-- 검색용 해시 필드 추가
ALTER TABLE customers ADD COLUMN name_hash VARCHAR(255);
ALTER TABLE customers ADD COLUMN phone_hash VARCHAR(255);

-- 인덱스 추가 (검색 성능 향상)
CREATE INDEX idx_customer_name_hash ON customers(name_hash);
```

### 3. **프로덕션 환경 설정**

- 암호화 키, JWT 시크릿은 환경변수 사용 권장
- Salt 값은 충분히 복잡하게 설정
- 정기적인 키 로테이션 고려

## 📖 **상세 가이드**

- [JPA Entity Listener 사용법](src/main/kotlin/com/placely/common/security/jpa/README.md)
- [암복호화 설정 가이드](src/main/kotlin/com/placely/common/security/crypto/)
- [JWT 사용법](src/main/kotlin/com/placely/common/security/jwt/)

## 🎉 **장점 요약**

| 특징                   | 설명                                                    |
| ---------------------- | ------------------------------------------------------- |
| **완벽한 실수 방지**   | Entity에 어노테이션만 추가하면 모든 DB 작업에 자동 적용 |
| **투명한 처리**        | 비즈니스 로직에 암복호화 코드 전혀 없음                 |
| **검색 지원**          | 암호화된 데이터도 해시 기반 검색 가능                   |
| **Zero Configuration** | 의존성 추가만으로 모든 기능 자동 활성화                 |
| **Spring Boot 표준**   | Auto Configuration 패턴 준수                            |

**개발자가 절대 깜빡할 수 없는 가장 안전한 암복호화 시스템! 🛡️**
