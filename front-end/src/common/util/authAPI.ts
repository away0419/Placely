import { authApiClient, apiUtils } from "./apiClient";

// 인증 관련 타입 정의
export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  user: {
    id: number;
    username: string;
    email: string;
    fullName: string;
  };
}

export interface User {
  id: number;
  username: string;
  email: string;
  fullName: string;
}

export interface HealthResponse {
  status: string;
  redis: string;
  timestamp: string;
}

/**
 * 인증 관련 API 서비스
 */
export const authAPI = {
  /**
   * 로그인
   */
  login: async (loginData: LoginRequest): Promise<LoginResponse> => {
    try {
      const response = await authApiClient.post<LoginResponse>(
        "/login",
        loginData
      );

      // 로그인 성공 시 토큰과 사용자 정보 저장
      const { token, user } = response.data;
      apiUtils.saveAuthData(token, user);

      return response.data;
    } catch (error) {
      console.error("로그인 실패:", error);
      throw error;
    }
  },

  /**
   * 로그아웃
   */
  logout: async (): Promise<void> => {
    try {
      // 서버에 로그아웃 요청 (토큰이 필요한 요청)
      await authApiClient.post("/logout");
    } catch (error) {
      console.error("로그아웃 요청 실패:", error);
      // 서버 요청이 실패해도 로컬 데이터는 정리
    } finally {
      // 로컬 인증 정보 삭제
      apiUtils.clearAuthData();
    }
  },

  /**
   * 서비스 상태 확인 (헬스체크)
   */
  health: async (): Promise<HealthResponse> => {
    const response = await authApiClient.get<HealthResponse>("/health");
    return response.data;
  },

  /**
   * 현재 사용자 정보 가져오기 (로컬 스토리지에서)
   */
  getCurrentUser: (): User | null => {
    return apiUtils.getCurrentUser();
  },

  /**
   * 토큰 존재 여부 확인
   */
  isAuthenticated: (): boolean => {
    return apiUtils.hasToken();
  },

  /**
   * 토큰 유효성 검증 (서버와 통신)
   */
  validateToken: async (): Promise<boolean> => {
    try {
      await authApiClient.get("/user/me"); // 인증이 필요한 엔드포인트
      return true;
    } catch (error) {
      console.warn("토큰 검증 실패:", error);
      return false;
    }
  },

  /**
   * 사용자 정보 새로고침 (서버에서 최신 정보 가져오기)
   */
  refreshUserInfo: async (): Promise<User> => {
    const response = await authApiClient.get<User>("/user/me");

    // 새로운 사용자 정보로 로컬 스토리지 업데이트
    const currentToken = localStorage.getItem("token");
    if (currentToken) {
      apiUtils.saveAuthData(currentToken, response.data);
    }

    return response.data;
  },
};
