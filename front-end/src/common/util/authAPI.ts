// 인증 관련 API 호출 함수들
interface LoginRequest {
  username: string;
  password: string;
}

interface LoginResponse {
  token: string;
  user: {
    id: number;
    username: string;
    email: string;
    fullName: string;
  };
}

// API 기본 설정
const API_BASE_URL = import.meta.env.VITE_API_URL || "http://localhost:8080";

// HTTP 요청 헬퍼 함수
const request = async (url: string, options: RequestInit = {}) => {
  const config: RequestInit = {
    headers: {
      "Content-Type": "application/json",
      ...options.headers,
    },
    ...options,
  };

  // 토큰이 있으면 Authorization 헤더 추가
  const token = localStorage.getItem("token");
  if (token) {
    config.headers = {
      ...config.headers,
      Authorization: `Bearer ${token}`,
    };
  }

  const response = await fetch(`${API_BASE_URL}${url}`, config);

  // 401 에러시 토큰 삭제 후 로그인 페이지로 리다이렉트
  if (response.status === 401) {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    window.location.href = "/login";
    throw new Error("인증이 만료되었습니다. 다시 로그인해주세요.");
  }

  if (!response.ok) {
    throw new Error(`HTTP error! status: ${response.status}`);
  }

  return response.json();
};

// 인증 API 객체
export const authAPI = {
  // 로그인
  login: async (loginData: LoginRequest): Promise<LoginResponse> => {
    return request("/login", {
      method: "POST",
      body: JSON.stringify(loginData),
    });
  },

  // 로그아웃
  logout: async (): Promise<void> => {
    try {
      await request("/logout", {
        method: "POST",
      });
    } finally {
      // 서버 요청 성공/실패 관계없이 로컬에서 토큰 삭제
      localStorage.removeItem("token");
      localStorage.removeItem("user");
    }
  },

  // 서비스 상태 확인
  health: async (): Promise<{
    status: string;
    redis: string;
    timestamp: string;
  }> => {
    return request("/health");
  },

  // 토큰 유효성 검사
  isAuthenticated: (): boolean => {
    const token = localStorage.getItem("token");
    return !!token;
  },

  // 현재 사용자 정보 가져오기
  getCurrentUser: () => {
    const userString = localStorage.getItem("user");
    return userString ? JSON.parse(userString) : null;
  },
};
