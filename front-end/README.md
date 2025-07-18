# Placely Frontend

Placely 프론트엔드는 React + TypeScript + Vite + Tailwind CSS로 구축된 모던 웹 애플리케이션입니다.

## 주요 기능

### 🔐 인증 시스템

- **로그인/로그아웃**: Toss 스타일의 깔끔한 UI
- **사용자 정보 관리**: 개인정보 수정 및 비밀번호 변경
- **인증 상태 관리**: React Context API 기반
- **보호된 라우트**: 인증이 필요한 페이지 자동 보호

### 🎨 디자인 시스템

- **Toss 스타일**: 깔끔하고 직관적인 디자인
- **반응형 디자인**: 모바일부터 데스크톱까지 완벽 대응
- **다크/라이트 모드**: 시스템 설정에 따른 자동 테마 적용
- **일관된 색상 팔레트**: 프로젝트 전용 컬러 시스템

## 기술 스택

- **Frontend**: React 19, TypeScript
- **Build Tool**: Vite
- **Styling**: Tailwind CSS
- **Routing**: React Router v6
- **State Management**: React Context API

## 개발 시작하기

### 1. 의존성 설치

```bash
npm install
```

### 2. 개발 서버 실행

```bash
npm run dev
```

### 3. 백엔드 연동 설정

`src/common/util/authAPI.ts`와 `src/common/util/userAPI.ts`에서 API_BASE_URL을 백엔드 서버 주소로 설정하세요.

```typescript
const API_BASE_URL = "http://localhost:8080"; // 백엔드 서버 주소
```

## 페이지 구조

### 🏠 주요 페이지

- `/` - 메인 페이지
- `/login` - 로그인 페이지 (Toss 스타일)
- `/profile` - 사용자 프로필 관리 (인증 필요)
- `/dashboard` - 대시보드 (인증 필요)
- `/store` - 매장 관리 (인증 필요)
- `/product` - 상품 관리 (인증 필요)

### 🔐 인증 플로우

1. **로그인 페이지** (`/login`)

   - 사용자 아이디/비밀번호 입력
   - JWT 토큰 받아서 로컬 스토리지에 저장
   - 인증 성공 시 대시보드로 리다이렉트

2. **사용자 프로필** (`/profile`)

   - 개인정보 수정 (이메일, 전화번호, 이름, 생년월일, 성별)
   - 비밀번호 변경
   - 탭 형태의 깔끔한 UI

3. **보호된 라우트**
   - 인증되지 않은 사용자는 자동으로 로그인 페이지로 리다이렉트
   - 토큰 만료 시 자동 로그아웃 및 로그인 페이지로 이동

## 컴포넌트 구조

### 📁 폴더 구조

```
src/
├── pages/
│   ├── Auth/
│   │   └── Login.tsx          # 로그인 페이지
│   └── UserProfile/
│       └── UserProfile.tsx    # 사용자 프로필 페이지
├── common/
│   ├── component/
│   │   ├── Header.tsx         # 상단 네비게이션
│   │   ├── LoadingSpinner.tsx # 로딩 스피너
│   │   └── TossInput.tsx      # Toss 스타일 입력 필드
│   ├── context/
│   │   └── AuthContext.tsx    # 인증 상태 관리
│   └── util/
│       ├── authAPI.ts         # 인증 관련 API
│       └── userAPI.ts         # 사용자 관련 API
└── router/
    └── index.tsx              # 라우터 설정
```

### 🎨 디자인 토큰

프로젝트는 일관된 색상 시스템을 사용합니다:

```css
:root {
  --color-primary: #2d6a4f; /* 딥 그린 - 메인 컬러 */
  --color-secondary: #40916c; /* 밝은 그린 - 보조 컬러 */
  --color-accent: #ffb703; /* 옐로우 - 강조 컬러 */
  --color-error: #e63946; /* 레드 - 에러 컬러 */
}
```

## API 연동

### 🔌 백엔드 엔드포인트

- `POST /login` - 로그인
- `POST /logout` - 로그아웃
- `GET /health` - 서비스 상태 확인
- `PUT /user` - 사용자 정보 수정
- `PUT /user/password` - 비밀번호 변경

### 🛡️ 인증 방식

- JWT Bearer Token 방식 사용
- Authorization 헤더에 토큰 포함
- 401 에러 시 자동 로그아웃 및 로그인 페이지 리다이렉트

## Vite 플러그인

Currently, two official plugins are available:

- [@vitejs/plugin-react](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react) uses [Babel](https://babeljs.io/) for Fast Refresh
- [@vitejs/plugin-react-swc](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react-swc) uses [SWC](https://swc.rs/) for Fast Refresh

## Expanding the ESLint configuration

If you are developing a production application, we recommend updating the configuration to enable type-aware lint rules:

```js
export default tseslint.config({
  extends: [
    // Remove ...tseslint.configs.recommended and replace with this
    ...tseslint.configs.recommendedTypeChecked,
    // Alternatively, use this for stricter rules
    ...tseslint.configs.strictTypeChecked,
    // Optionally, add this for stylistic rules
    ...tseslint.configs.stylisticTypeChecked,
  ],
  languageOptions: {
    // other options...
    parserOptions: {
      project: ["./tsconfig.node.json", "./tsconfig.app.json"],
      tsconfigRootDir: import.meta.dirname,
    },
  },
});
```

You can also install [eslint-plugin-react-x](https://github.com/Rel1cx/eslint-react/tree/main/packages/plugins/eslint-plugin-react-x) and [eslint-plugin-react-dom](https://github.com/Rel1cx/eslint-react/tree/main/packages/plugins/eslint-plugin-react-dom) for React-specific lint rules:

```js
// eslint.config.js
import reactX from "eslint-plugin-react-x";
import reactDom from "eslint-plugin-react-dom";

export default tseslint.config({
  plugins: {
    // Add the react-x and react-dom plugins
    "react-x": reactX,
    "react-dom": reactDom,
  },
  rules: {
    // other rules...
    // Enable its recommended typescript rules
    ...reactX.configs["recommended-typescript"].rules,
    ...reactDom.configs.recommended.rules,
  },
});
```
