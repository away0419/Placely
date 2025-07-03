# 로그인 기능 테스트 가이드 (Kotlin 스타일)

## 테스트 개요

Placely 인증 서비스의 로그인 기능에 대한 **Kotlin 스타일** 테스트 코드입니다.
**Kotest**와 **MockK** 라이브러리를 사용하여 더 Kotlin다운 테스트를 작성했습니다.

## 사용된 Kotlin 테스트 라이브러리

- **Kotest 5.8.0**: Kotlin 전용 테스트 프레임워크
- **MockK 1.13.8**: Kotlin 전용 모킹 라이브러리
- **SpringMockK 4.0.2**: Spring Boot와 MockK 통합

## 테스트 구조

### 1. 컨트롤러 테스트 (AuthControllerTest)

- **위치**: `src/test/kotlin/com/placely/auth/controller/AuthControllerTest.kt`
- **스타일**: Kotest DescribeSpec (BDD 스타일)
- **모킹**: MockK의 `@MockkBean` 사용
- **목적**: REST API 엔드포인트의 동작 검증

```kotlin
@WebMvcTest(AuthController::class)
class AuthControllerTest : DescribeSpec() {

    @MockkBean
    private lateinit var authService: AuthService

    init {
        describe("로그인 API 테스트") {
            context("유효한 사용자 정보로 로그인할 때") {
                it("200 OK와 토큰 정보를 반환해야 한다") {
                    every { authService.login(any()) } returns expectedResponse
                    // 테스트 검증 로직
                }
            }
        }
    }
}
```

### 2. 서비스 테스트 (AuthServiceTest)

- **위치**: `src/test/kotlin/com/placely/auth/service/AuthServiceTest.kt`
- **스타일**: Kotest DescribeSpec 함수형 스타일
- **모킹**: MockK의 `mockk<>()` 사용
- **검증**: Kotest matchers (`shouldBe`, `shouldNotBe`)
- **목적**: 비즈니스 로직의 정확성 검증

```kotlin
class AuthServiceTest : DescribeSpec({
    val userRepository = mockk<UserRepository>()
    val authService = AuthService(userRepository, ...)

    describe("로그인 기능 테스트") {
        context("유효한 사용자 정보로 로그인을 시도할 때") {
            it("성공적으로 토큰과 사용자 정보를 반환해야 한다") {
                every { userRepository.findByUsernameOrEmailForLogin(any()) } returns Optional.of(testUser)

                val result = authService.login(loginRequest)

                result shouldNotBe null
                result.accessToken shouldBe mockAccessToken
                verify(exactly = 1) { userRepository.findByUsernameOrEmailForLogin(any()) }
            }
        }
    }
})
```

### 3. 통합 테스트 (AuthIntegrationTest)

- **위치**: `src/test/kotlin/com/placely/auth/integration/AuthIntegrationTest.kt`
- **스타일**: Kotest DescribeSpec + Spring Boot Test
- **목적**: 실제 데이터베이스와 연동한 통합 테스트

## Java vs Kotlin 테스트 스타일 비교

### Mock 객체 생성

```kotlin
// Java/Mockito 방식
@Mock
private lateinit var authService: AuthService

// Kotlin/MockK 방식
val authService = mockk<AuthService>()
```

### Mock 동작 설정

```kotlin
// Java/Mockito 방식
given(authService.login(any())).willReturn(response)

// Kotlin/MockK 방식
every { authService.login(any()) } returns response
```

### 검증 (Assertion)

```kotlin
// Java/AssertJ 방식
assertThat(result).isNotNull()
assertThat(result.accessToken).isEqualTo(expectedToken)

// Kotlin/Kotest 방식
result shouldNotBe null
result.accessToken shouldBe expectedToken
```

### 예외 테스트

```kotlin
// Java/JUnit 방식
assertThrows<IllegalArgumentException> { authService.login(request) }

// Kotlin/Kotest 방식
shouldThrow<IllegalArgumentException> { authService.login(request) }
```

## 테스트 실행 방법

### 전체 테스트 실행

```bash
./gradlew test
```

### 특정 테스트 클래스 실행

```bash
# 컨트롤러 테스트만 실행
./gradlew test --tests "com.placely.auth.controller.AuthControllerTest"

# 서비스 테스트만 실행
./gradlew test --tests "com.placely.auth.service.AuthServiceTest"
```

## 테스트 설정

### 테스트 환경 설정

- **설정 파일**: `src/test/resources/application-test.yml`
- **데이터베이스**: H2 인메모리 데이터베이스 사용
- **프로파일**: `test` 프로파일 활성화

### Kotlin 테스트 의존성 (build.gradle)

```kotlin
// Kotlin 테스트 라이브러리
testImplementation 'io.kotest:kotest-runner-junit5:5.8.0'
testImplementation 'io.kotest:kotest-assertions-core:5.8.0'
testImplementation 'io.kotest:kotest-extensions-spring:1.1.3'
testImplementation 'io.mockk:mockk:1.13.8'
testImplementation 'com.ninja-squad:springmockk:4.0.2'
```

## 주요 검증 항목

### API 응답 검증

- HTTP 상태 코드 확인
- JSON 응답 구조 검증
- 필수 필드 존재 여부 확인

### 비즈니스 로직 검증

- 토큰 생성 로직
- 사용자 조회 로직
- 예외 처리 로직

### 데이터베이스 상호작용 검증

- Repository 메서드 호출 확인 (`verify`)
- 데이터 저장/조회 검증

## Kotlin 테스트의 장점

### 1. 더 간결한 문법

```kotlin
// 함수형 스타일의 테스트 작성
class AuthServiceTest : DescribeSpec({
    // 테스트 코드가 init 블록 안에 함수형으로 작성됨
})
```

### 2. 자연스러운 한글 테스트명

```kotlin
it("유효한 사용자 정보로 로그인하면 토큰을 반환해야 한다") {
    // 테스트 코드
}
```

### 3. 강력한 Assertion

```kotlin
result shouldNotBe null
result.accessToken shouldBe expectedToken
result.user.username shouldBe "testuser"
```

### 4. Mock 검증의 편리함

```kotlin
verify(exactly = 1) { userRepository.findByUsernameOrEmailForLogin(any()) }
verify(exactly = 2) { tokenRepository.save(any()) }
```

## 참고 사항

### 테스트 실행 전 확인사항

1. Gradle 빌드 환경 확인
2. Kotlin 테스트 의존성 설치 확인
3. 테스트 데이터베이스 설정 확인

### 테스트 결과 확인

- 테스트 성공 시: 모든 검증 통과
- 테스트 실패 시: 실패 원인 로그 확인
