// 사용자 관련 API 호출 함수들
interface UserInfo {
  email: string;
  phone: string;
  fullName: string;
  birthDate: string;
  gender: string;
}

interface PasswordUpdateRequest {
  currentPassword: string;
  newPassword: string;
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

  // 응답이 비어있지 않은 경우에만 JSON 파싱
  const text = await response.text();
  return text ? JSON.parse(text) : {};
};

// 사용자 API 객체
export const userAPI = {
  // 사용자 정보 업데이트
  updateUserInfo: async (userInfo: UserInfo): Promise<number> => {
    return request("/user", {
      method: "PUT",
      body: JSON.stringify(userInfo),
    });
  },

  // 비밀번호 변경
  updatePassword: async (
    passwordData: PasswordUpdateRequest
  ): Promise<void> => {
    return request("/user/password", {
      method: "PUT",
      body: JSON.stringify(passwordData),
    });
  },
};
