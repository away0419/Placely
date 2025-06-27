# 🔐 JPA Entity Listener 기반 자동 암복호화


## 🎯 **핵심 장점**

### ✅ **완벽한 실수 방지**

- Entity에 `@EntityListeners`만 추가하면 **모든 DB 작업**에 자동 적용
- Service 메서드에 어노테이션 깜빡할 위험 **제로**
- 신규 개발자도 실수할 수 없는 구조

### ✅ **투명한 처리**

- Repository, Service 코드는 일반 JPA와 **100% 동일**
- 비즈니스 로직에 암복호화 코드 **전혀 없음**
- 기존 코드 수정 **최소화**

## 🚀 **사용법**

### 1. **Entity에 Listener 적용**

```kotlin
import com.placely.common.security.crypto.EncryptField
import com.placely.common.security.crypto.HashField
import com.placely.common.security.jpa.CryptoEntityListener
import jakarta.persistence.*

@Entity
@Table(name = "customers")
@EntityListeners(CryptoEntityListener::class)  // 🔥 이것만 추가하면 끝!
data class Customer(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val customerId: Long = 0,

    // 암호화 + 검색 가능 (기본 해싱 사용)
    @EncryptField(
        searchable = true,
        searchHashField = "name_hash"
    )
    @Column(name = "customer_name", length = 500) // 암호화된 데이터는 길이가 늘어남
    var customerName: String = "",

    // 검색용 해시 필드 (자동 생성됨)
    @Column(name = "name_hash")
    var nameHash: String = "",

    // 암호화 + 검색 가능 (기본 해싱 사용)
    @EncryptField(
        searchable = true,
        searchHashField = "phone_hash"
    )
    @Column(name = "phone_number", length = 500)
    var phoneNumber: String = "",

    // 검색용 해시 필드 (자동 생성됨)
    @Column(name = "phone_hash")
    var phoneHash: String = "",

    // 암호화만 (검색 불가능한 민감정보)
    @EncryptField
    @Column(name = "address", length = 1000)
    var address: String = "",

    // 암호화 + 검색 가능 (기본 해싱 사용)
    @EncryptField(
        searchable = true,
        searchHashField = "email_hash"
    )
    @Column(name = "email", length = 500)
    var email: String = "",

    // 검색용 해시 필드 (자동 생성됨)
    @Column(name = "email_hash")
    var emailHash: String = "",

    // 비밀번호 해싱 (기본 해싱 사용)
    @HashField
    @Column(name = "password")
    var password: String = "",

    // 기본 검사 컬럼들
    @Column(name = "created_at")
    var createdAt: String = "",

    @Column(name = "created_by")
    var createdBy: String = "",

    @Column(name = "updated_at")
    var updatedAt: String = "",

    @Column(name = "updated_by")
    var updatedBy: String = "",

    @Column(name = "is_deleted", length = 1)
    var isDeleted: String = "N",

    @Column(name = "deleted_at")
    var deletedAt: String? = null,

    @Column(name = "deleted_by")
    var deletedBy: String? = null
)
```

### 2. **Repository는 일반 JPA와 동일**

```kotlin
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface CustomerRepository : JpaRepository<Customer, Long> {

    // 해시 필드로 검색 (암호화된 데이터 검색)
    fun findByNameHash(nameHash: String): List<Customer>
    fun findByPhoneHash(phoneHash: String): List<Customer>
    fun findByEmailHash(emailHash: String): Customer?

    // 활성 고객만 검색
    fun findByIsDeleted(isDeleted: String): List<Customer>

    // 복합 조건 검색
    fun findByNameHashAndIsDeleted(nameHash: String, isDeleted: String): List<Customer>
    fun findByPhoneHashAndIsDeleted(phoneHash: String, isDeleted: String): List<Customer>
    fun findByEmailHashAndIsDeleted(emailHash: String, isDeleted: String): Customer?

    // 커스텀 쿼리 - 이름으로 활성 고객 검색
    @Query("SELECT c FROM Customer c WHERE c.nameHash = :nameHash AND c.isDeleted = 'N' ORDER BY c.customerId DESC")
    fun findActiveByNameHash(@Param("nameHash") nameHash: String): List<Customer>

    // 커스텀 쿼리 - 전화번호로 활성 고객 검색
    @Query("SELECT c FROM Customer c WHERE c.phoneHash = :phoneHash AND c.isDeleted = 'N'")
    fun findActiveByPhoneHash(@Param("phoneHash") phoneHash: String): Customer?

    // 커스텀 쿼리 - 이메일로 활성 고객 검색
    @Query("SELECT c FROM Customer c WHERE c.emailHash = :emailHash AND c.isDeleted = 'N'")
    fun findActiveByEmailHash(@Param("emailHash") emailHash: String): Customer?

    // 모든 활성 고객 조회
    @Query("SELECT c FROM Customer c WHERE c.isDeleted = 'N' ORDER BY c.createdAt DESC")
    fun findAllActive(): List<Customer>

    // 생성자별 고객 조회
    @Query("SELECT c FROM Customer c WHERE c.createdBy = :createdBy AND c.isDeleted = 'N'")
    fun findByCreatedBy(@Param("createdBy") createdBy: String): List<Customer>
}
```

### 3. **Service도 일반 JPA와 동일**

```kotlin
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.beans.factory.annotation.Autowired
import com.placely.common.security.crypto.CryptoUtil
import java.time.LocalDateTime

@Service
@Transactional
class CustomerService {

    @Autowired
    private lateinit var customerRepository: CustomerRepository

    @Autowired
    private lateinit var cryptoUtil: CryptoUtil

    // ✅ 고객 생성 (자동 암호화/해싱)
    fun createCustomer(
        name: String,
        phoneNumber: String,
        email: String,
        address: String,
        password: String,
        createdBy: String = "system"
    ): Customer {
        val customer = Customer(
            customerName = name,           // 자동 암호화 + 해시 생성
            phoneNumber = phoneNumber,     // 자동 암호화 + 해시 생성
            email = email,                 // 자동 암호화 + 해시 생성
            address = address,             // 자동 암호화
            password = password,           // 자동 해싱
            createdAt = LocalDateTime.now().toString(),
            createdBy = createdBy,
            updatedAt = LocalDateTime.now().toString(),
            updatedBy = createdBy
        )

        // 저장 시 Entity Listener가 자동으로 암호화/해싱 처리
        return customerRepository.save(customer)
    }

    // ✅ 고객 조회 (자동 복호화)
    @Transactional(readOnly = true)
    fun getCustomer(customerId: Long): Customer? {
        return customerRepository.findById(customerId).orElse(null)
        // Entity Listener가 자동으로 복호화 처리
    }

    // ✅ 모든 고객 조회 (자동 복호화)
    @Transactional(readOnly = true)
    fun getAllCustomers(): List<Customer> {
        return customerRepository.findAll()
        // 각 Entity가 조회 시 자동으로 복호화됨
    }

    // ✅ 활성 고객만 조회 (자동 복호화)
    @Transactional(readOnly = true)
    fun getAllActiveCustomers(): List<Customer> {
        return customerRepository.findAllActive()
    }

    // 🔍 이름으로 검색 (해시 기반)
    @Transactional(readOnly = true)
    fun findByName(name: String): List<Customer> {
        val nameHash = cryptoUtil.hashing(name)
        return customerRepository.findActiveByNameHash(nameHash)
    }

    // 🔍 전화번호로 검색 (해시 기반)
    @Transactional(readOnly = true)
    fun findByPhoneNumber(phoneNumber: String): Customer? {
        val phoneHash = cryptoUtil.hashing(phoneNumber)
        return customerRepository.findActiveByPhoneHash(phoneHash)
    }

    // 🔍 이메일로 검색 (해시 기반)
    @Transactional(readOnly = true)
    fun findByEmail(email: String): Customer? {
        val emailHash = cryptoUtil.hashing(email)
        return customerRepository.findActiveByEmailHash(emailHash)
    }

    // 🔍 중복 확인 (회원가입 시 사용)
    @Transactional(readOnly = true)
    fun isDuplicateEmail(email: String): Boolean {
        return findByEmail(email) != null
    }

    @Transactional(readOnly = true)
    fun isDuplicatePhoneNumber(phoneNumber: String): Boolean {
        return findByPhoneNumber(phoneNumber) != null
    }

    // ✏️ 고객 정보 수정 (자동 암호화/해싱)
    fun updateCustomer(
        customerId: Long,
        name: String? = null,
        phoneNumber: String? = null,
        email: String? = null,
        address: String? = null,
        updatedBy: String = "system"
    ): Customer? {
        val customer = getCustomer(customerId) ?: return null

        // 중복 체크 (다른 고객이 이미 사용 중인지)
        phoneNumber?.let {
            val existing = findByPhoneNumber(it)
            if (existing != null && existing.customerId != customerId) {
                throw IllegalArgumentException("이미 사용 중인 전화번호입니다.")
            }
        }

        email?.let {
            val existing = findByEmail(it)
            if (existing != null && existing.customerId != customerId) {
                throw IllegalArgumentException("이미 사용 중인 이메일입니다.")
            }
        }

        // 값이 있는 경우에만 업데이트 (자동 암호화/해싱 처리됨)
        name?.let { customer.customerName = it }
        phoneNumber?.let { customer.phoneNumber = it }
        email?.let { customer.email = it }
        address?.let { customer.address = it }

        customer.updatedAt = LocalDateTime.now().toString()
        customer.updatedBy = updatedBy

        return customerRepository.save(customer) // 자동 암호화/해싱
    }

    // 🔐 비밀번호 변경
    fun changePassword(customerId: Long, newPassword: String, updatedBy: String = "system"): Boolean {
        val customer = getCustomer(customerId) ?: return false

        customer.password = newPassword // 자동 해싱 처리됨
        customer.updatedAt = LocalDateTime.now().toString()
        customer.updatedBy = updatedBy

        customerRepository.save(customer)
        return true
    }

    // 🔐 비밀번호 검증
    @Transactional(readOnly = true)
    fun verifyPassword(customerId: Long, inputPassword: String): Boolean {
        val customer = getCustomer(customerId) ?: return false
        return cryptoUtil.verifyHash(inputPassword, customer.password)
    }

    // 🔐 로그인 (이메일 + 비밀번호)
    @Transactional(readOnly = true)
    fun login(email: String, password: String): Customer? {
        val customer = findByEmail(email) ?: return null

        return if (cryptoUtil.verifyHash(password, customer.password)) {
            customer
        } else {
            null
        }
    }

    // 🗑️ 고객 삭제 (소프트 삭제)
    fun deleteCustomer(customerId: Long, deletedBy: String): Boolean {
        val customer = getCustomer(customerId) ?: return false

        customer.isDeleted = "Y"
        customer.deletedAt = LocalDateTime.now().toString()
        customer.deletedBy = deletedBy
        customer.updatedAt = LocalDateTime.now().toString()
        customer.updatedBy = deletedBy

        customerRepository.save(customer)
        return true
    }

    // 📊 통계 - 생성자별 고객 수
    @Transactional(readOnly = true)
    fun getCustomerCountByCreator(createdBy: String): Int {
        return customerRepository.findByCreatedBy(createdBy).size
    }

    // 📊 통계 - 총 활성 고객 수
    @Transactional(readOnly = true)
    fun getActiveCustomerCount(): Int {
        return getAllActiveCustomers().size
    }
}
```

## 📊 **자동 처리 플로우**

### 저장 시 (@PrePersist, @PreUpdate)

```
평문 데이터 입력 → Entity Listener 감지 → @EncryptField 암호화 → @HashField 해싱 → 검색 해시 생성 → DB 저장
```

### 조회 시 (@PostLoad)

```
DB 조회 → Entity Listener 감지 → @EncryptField 복호화 → 평문 데이터 반환
```

### 검색 시

```
검색어 입력 → Service에서 해싱 → 해시로 DB 검색 → 복호화된 결과 반환
```

## ⚙️ **Entity Listener 생명주기**

| JPA 이벤트    | 처리 내용                     |
| ------------- | ----------------------------- |
| `@PrePersist` | 새 Entity 저장 전 암호화/해싱 |
| `@PreUpdate`  | Entity 수정 전 암호화/해싱    |
| `@PostLoad`   | Entity 조회 후 복호화         |

## 🔧 **고급 기능**

### 1. **중복 처리 방지**

```kotlin
// Entity Listener가 자동으로 체크
- 이미 암호화된 값은 재암호화 안 함
- 이미 해싱된 값은 재해싱 안 함
- Base64 패턴과 길이로 자동 판단
```

### 2. **오류 처리**

```kotlin
// 암호화 실패 시: RuntimeException 발생
// 복호화 실패 시: 원본 값 유지 + 로그
// 해시 필드 없음: 무시 (선택적 필드)
```

### 3. **성능 최적화**

```kotlin
// 필요한 필드만 처리 (어노테이션 기반)
// 리플렉션 캐싱으로 성능 향상
// 조건부 처리로 불필요한 연산 방지
```

## 🚨 **주의사항**

### 1. **컬럼 길이 조정**

```sql
-- 암호화된 데이터는 길이가 증가함
ALTER TABLE customers MODIFY customer_name VARCHAR(500);
ALTER TABLE customers MODIFY phone_number VARCHAR(500);
ALTER TABLE customers MODIFY address VARCHAR(1000);
```

### 2. **해시 필드 추가**

```sql
-- 검색용 해시 필드 추가
ALTER TABLE customers ADD COLUMN name_hash VARCHAR(255);
ALTER TABLE customers ADD COLUMN phone_hash VARCHAR(255);
ALTER TABLE customers ADD COLUMN email_hash VARCHAR(255);

-- 인덱스 추가 (검색 성능 향상)
CREATE INDEX idx_customer_name_hash ON customers(name_hash);
CREATE INDEX idx_customer_phone_hash ON customers(phone_hash);
CREATE INDEX idx_customer_email_hash ON customers(email_hash);
```

### 3. **마이그레이션 전략**

```kotlin
// 기존 데이터가 있는 경우
1. 새 컬럼 추가 (해시 필드들)
2. 기존 데이터 암호화 배치 작업
3. 애플리케이션 배포
4. 검증 후 기존 평문 컬럼 삭제
```

## 🎉 **vs 메서드 AOP 비교**

| 구분          | JPA Entity Listener       | 메서드 AOP             |
| ------------- | ------------------------- | ---------------------- |
| **실수 방지** | ⭐⭐⭐⭐⭐ 불가능         | ⭐⭐⭐ 깜빡할 수 있음  |
| **자동화**    | ⭐⭐⭐⭐⭐ 완전 자동      | ⭐⭐⭐ 어노테이션 필요 |
| **일관성**    | ⭐⭐⭐⭐⭐ 모든 DB 작업   | ⭐⭐⭐ 메서드별 설정   |
| **투명성**    | ⭐⭐⭐⭐⭐ 코드 변경 없음 | ⭐⭐⭐⭐ 어노테이션만  |
| **성능**      | ⭐⭐⭐⭐ Entity 레벨      | ⭐⭐⭐⭐ 메서드 레벨   |

**결론: JPA Entity Listener가 가장 안전하고 실수 방지에 효과적! 🏆**
