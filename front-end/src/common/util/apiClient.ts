import axios from "axios";
import type {
  AxiosInstance,
  InternalAxiosRequestConfig,
  AxiosResponse,
} from "axios";

// API 응답 공통 타입
export interface ApiResponse<T = any> {
  data: T;
  message?: string;
  success: boolean;
}

// API 에러 타입
export interface ApiError {
  message: string;
  status: number;
  code?: string;
}

// 서비스별 API 설정
export const API_CONFIGS = {
  auth: {
    baseURL: import.meta.env.VITE_AUTH_API_URL || "http://localhost:8081/auth",
    timeout: 10000,
  },
  pos: {
    baseURL: import.meta.env.VITE_POS_API_URL || "http://localhost:8082/pos",
    timeout: 15000,
  },
  ai: {
    baseURL: import.meta.env.VITE_AI_API_URL || "http://localhost:8083/ai",
    timeout: 30000,
  },
} as const;

/**
 * API 클라이언트 생성 함수
 */
export const createApiClient = (
  serviceName: keyof typeof API_CONFIGS
): AxiosInstance => {
  const config = API_CONFIGS[serviceName];

  // axios 인스턴스 생성
  const client = axios.create({
    baseURL: config.baseURL,
    timeout: config.timeout,
    headers: {
      "Content-Type": "application/json",
    },
  });

  // 요청 인터셉터: 토큰 자동 추가
  client.interceptors.request.use(
    (config: InternalAxiosRequestConfig) => {
      const token = localStorage.getItem("token");

      if (token && config.headers) {
        config.headers.Authorization = `Bearer ${token}`;
      }

      // 요청 로깅 (개발환경에서만)
      if (import.meta.env.DEV) {
        console.log(
          `🚀 [${serviceName.toUpperCase()}] ${config.method?.toUpperCase()} ${
            config.url
          }`,
          config.data
        );
      }

      return config;
    },
    (error) => {
      console.error("❌ 요청 인터셉터 에러:", error);
      return Promise.reject(error);
    }
  );

  // 응답 인터셉터: 에러 처리 중앙화
  client.interceptors.response.use(
    (response: AxiosResponse) => {
      // 성공 응답 로깅 (개발환경에서만)
      if (import.meta.env.DEV) {
        console.log(
          `✅ [${serviceName.toUpperCase()}] ${response.status}`,
          response.data
        );
      }

      return response;
    },
    (error) => {
      const { response } = error;

      if (response) {
        const { status, data } = response;

        // 에러 로깅
        console.error(`❌ [${serviceName.toUpperCase()}] ${status}:`, data);

        // 상태 코드별 처리
        switch (status) {
          case 401:
            // 인증 만료 - 토큰 삭제 후 로그인 페이지로
            localStorage.removeItem("token");
            localStorage.removeItem("user");

            // 현재 페이지가 로그인 페이지가 아닌 경우에만 리다이렉트
            if (window.location.pathname !== "/login") {
              window.location.href = "/login";
            }

            throw new Error("인증이 만료되었습니다. 다시 로그인해주세요.");

          case 403:
            throw new Error("접근 권한이 없습니다.");

          case 404:
            throw new Error("요청한 리소스를 찾을 수 없습니다.");

          case 500:
            throw new Error(
              "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
            );

          default:
            throw new Error(
              data?.message || `요청 처리 중 오류가 발생했습니다. (${status})`
            );
        }
      } else if (error.code === "ECONNABORTED") {
        // 타임아웃 에러
        throw new Error(
          "요청 시간이 초과되었습니다. 네트워크 연결을 확인해주세요."
        );
      } else {
        // 네트워크 에러
        throw new Error("네트워크 연결을 확인해주세요.");
      }
    }
  );

  return client;
};

// 서비스별 API 클라이언트 인스턴스
export const authApiClient = createApiClient("auth");
export const posApiClient = createApiClient("pos");
export const aiApiClient = createApiClient("ai");

// 공통 유틸리티 함수들
export const apiUtils = {
  /**
   * 토큰 존재 여부 확인
   */
  hasToken: (): boolean => {
    return !!localStorage.getItem("token");
  },

  /**
   * 사용자 정보 가져오기
   */
  getCurrentUser: () => {
    const userString = localStorage.getItem("user");
    return userString ? JSON.parse(userString) : null;
  },

  /**
   * 토큰과 사용자 정보 저장
   */
  saveAuthData: (token: string, user: any) => {
    localStorage.setItem("token", token);
    localStorage.setItem("user", JSON.stringify(user));
  },

  /**
   * 인증 정보 삭제
   */
  clearAuthData: () => {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
  },

  /**
   * API 요청 래퍼 (에러 처리 포함)
   */
  handleApiCall: async <T>(
    apiCall: () => Promise<AxiosResponse<T>>
  ): Promise<T> => {
    try {
      const response = await apiCall();
      return response.data;
    } catch (error) {
      // 이미 인터셉터에서 처리된 에러를 다시 던짐
      throw error;
    }
  },
};
