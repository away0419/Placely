# 📡 Placely Frontend API 구조

## 🎯 개요

Placely 프론트엔드는 **axios 기반의 체계적인 API 클라이언트 구조**를 사용합니다.

- **자동 토큰 관리**: 인터셉터를 통한 토큰 자동 포함
- **에러 처리 중앙화**: 401, 403 등 공통 에러 자동 처리
- **타입 안전성**: TypeScript로 완전 타입화
- **확장성**: 여러 마이크로서비스 지원

## 🏗️ 구조 개요

```
src/common/util/
├── apiClient.ts       # 🔧 공통 API 클라이언트 & 인터셉터
├── authAPI.ts         # 🔐 인증 관련 API
├── userAPI.ts         # 👤 사용자 관련 API
└── apiExamples.ts     # 📚 사용 예시
```

## 🔧 1. 공통 API 클라이언트 (apiClient.ts)

### 특징

- **서비스별 클라이언트**: Auth, POS, AI 서비스 분리
- **자동 토큰 포함**: 요청 인터셉터로 Authorization 헤더 자동 추가
- **스마트 에러 처리**: 응답 인터셉터로 상태 코드별 자동 처리
- **개발자 친화적**: 개발환경에서 요청/응답 로깅

### 설정

```typescript
const API_CONFIGS = {
  auth: { baseURL: "http://localhost:8081/auth", timeout: 10000 },
  pos: { baseURL: "http://localhost:8082/pos", timeout: 15000 },
  ai: { baseURL: "http://localhost:8083/ai", timeout: 30000 },
};
```

### 인터셉터 기능

```typescript
// 요청 인터셉터: 토큰 자동 추가
if (token) {
  config.headers.Authorization = `Bearer ${token}`;
}

// 응답 인터셉터: 에러 자동 처리
switch (status) {
  case 401: // 자동 로그아웃 + 로그인 페이지 리다이렉트
  case 403: // 권한 없음 메시지
  case 404: // 리소스 없음 메시지
  case 500: // 서버 오류 메시지
}
```

## 🔐 2. 인증 API (authAPI.ts)

### 주요 기능

```typescript
export const authAPI = {
  // 기본 인증
  login(loginData)           // 로그인 + 토큰 자동 저장
  logout()                   // 로그아웃 + 토큰 자동 삭제

  // 토큰 관리
  isAuthenticated()          // 토큰 존재 여부 확인
  validateToken()            // 서버와 토큰 유효성 검증

  // 사용자 정보
  getCurrentUser()           // 로컬 사용자 정보 조회
  refreshUserInfo()          // 서버에서 최신 정보 갱신

  // 기타
  health()                   // 서비스 상태 확인
};
```

### 사용 예시

```typescript
// 로그인
const response = await authAPI.login({ username, password });
console.log("사용자:", response.user); // 토큰은 자동 저장됨

// 로그아웃
await authAPI.logout(); // 토큰 자동 삭제됨

// 토큰 확인
if (authAPI.isAuthenticated()) {
  const isValid = await authAPI.validateToken();
}
```

## 👤 3. 사용자 API (userAPI.ts)

### 주요 기능

```typescript
export const userAPI = {
  // 프로필 관리
  getMyProfile()             // 내 프로필 정보 조회
  updateUserInfo(userInfo)   // 사용자 정보 수정
  updatePassword(passwords)  // 비밀번호 변경
  deleteAccount(password)    // 계정 삭제

  // 검증
  checkEmailDuplicate(email)     // 이메일 중복 검사
  checkUsernameDuplicate(username) // 사용자명 중복 검사

  // 비밀번호 재설정
  requestPasswordReset(email)    // 재설정 이메일 발송
  resetPassword(token, newPwd)   // 비밀번호 재설정

  // 이메일 인증
  requestEmailVerification()     // 인증 이메일 발송
  verifyEmail(token)            // 이메일 인증 확인
};
```

### 사용 예시

```typescript
// 프로필 조회 (토큰 자동 포함됨)
const profile = await userAPI.getMyProfile();

// 정보 수정
const updated = await userAPI.updateUserInfo({
  email: "new@email.com",
  fullName: "새로운 이름",
  // ...
});

// 비밀번호 변경
await userAPI.updatePassword({
  currentPassword: "현재비밀번호",
  newPassword: "새비밀번호",
});
```

## 🔄 4. AuthContext 통합

### 개선사항

- **새 API 구조 활용**: authAPI를 통한 일관된 인증 관리
- **자동 토큰 검증**: 앱 시작 시 토큰 유효성 자동 확인
- **사용자 정보 새로고침**: refreshUser() 메서드 추가

### 사용법

```typescript
const { isAuthenticated, user, login, logout, refreshUser } = useAuth();

// 로그인
await login("username", "password");

// 사용자 정보 갱신
await refreshUser();

// 로그아웃
await logout();
```

## 🎨 5. LoadingSpinner 개선

### 새로운 옵션

```typescript
<LoadingSpinner
  size="large" // small | medium | large
  color="primary" // primary | secondary | white | gray
  fullScreen={true} // 전체 화면 오버레이
  message="데이터를 불러오는 중..." // 로딩 메시지
/>
```

## 🚀 6. 실제 사용 패턴

### 컴포넌트에서 API 호출

```typescript
const ProfilePage: React.FC = () => {
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const loadProfile = async () => {
      try {
        setLoading(true);
        const data = await userAPI.getMyProfile(); // 토큰 자동 포함
        setProfile(data);
      } catch (err) {
        setError("프로필을 불러올 수 없습니다.");
      } finally {
        setLoading(false);
      }
    };

    loadProfile();
  }, []);

  if (loading) return <LoadingSpinner fullScreen />;
  if (error) return <div className="text-red-600">{error}</div>;

  return <div>{/* 프로필 UI */}</div>;
};
```

### 에러 처리 패턴

```typescript
try {
  const result = await userAPI.updateUserInfo(formData);
  // 성공 처리
} catch (error) {
  if (error.message.includes("인증")) {
    // 자동으로 로그인 페이지로 리다이렉트됨
  } else if (error.message.includes("네트워크")) {
    // 네트워크 오류 처리
  } else {
    // 기타 오류 처리
  }
}
```

## 🔧 7. 환경설정

### 환경변수 (.env 파일)

```bash
# 개발환경
VITE_AUTH_API_URL=http://localhost:8081/auth
VITE_POS_API_URL=http://localhost:8082/pos
VITE_AI_API_URL=http://localhost:8083/ai

# 운영환경
VITE_AUTH_API_URL=https://api.placely.com/auth
VITE_POS_API_URL=https://api.placely.com/pos
VITE_AI_API_URL=https://api.placely.com/ai
```

## 🎯 8. 장점 및 특징

### ✅ 기존 구조 대비 개선점

1. **자동 토큰 관리**: 매번 헤더 설정할 필요 없음
2. **중앙화된 에러 처리**: 401/403 등 자동 처리
3. **타입 안전성**: 완전한 TypeScript 지원
4. **확장성**: 새로운 서비스 API 쉽게 추가
5. **개발자 경험**: 디버깅을 위한 자동 로깅
6. **코드 중복 제거**: 공통 로직 재사용

### 🚀 성능 최적화

- **인터셉터**: 토큰 검증 및 에러 처리 자동화
- **타임아웃 설정**: 서비스별 적절한 타임아웃
- **요청 로깅**: 개발환경에서만 활성화

### 🛡️ 보안 강화

- **자동 토큰 검증**: 만료된 토큰 자동 처리
- **안전한 저장소**: localStorage 사용
- **HTTPS 지원**: 운영환경 SSL 통신

## 📚 9. 마이그레이션 가이드

### 기존 코드에서 새 구조로 변경

```typescript
// Before: fetch 기반
const response = await fetch("/api/user", {
  headers: {
    Authorization: `Bearer ${token}`,
    "Content-Type": "application/json",
  },
});

// After: 새 API 구조
const profile = await userAPI.getMyProfile(); // 토큰 자동 포함
```

### AuthContext 사용법 변경

```typescript
// Before: 직접 localStorage 접근
const user = JSON.parse(localStorage.getItem("user"));

// After: AuthContext 사용
const { user } = useAuth();
```

## 🔮 10. 향후 확장 계획

1. **캐시 레이어**: React Query/SWR 통합
2. **오프라인 지원**: 서비스 워커 연동
3. **실시간 통신**: WebSocket 클라이언트 추가
4. **에러 리포팅**: Sentry 등 모니터링 도구 연동
5. **성능 모니터링**: API 응답 시간 추적

---

이 구조를 통해 **더 안전하고 확장 가능한 API 통신**이 가능해졌습니다! 🎉
