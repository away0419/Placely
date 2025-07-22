import { authApiClient } from "./apiClient";

// 사용자 관련 타입 정의
export interface UserInfo {
  email: string;
  phone: string;
  fullName: string;
  birthDate: string;
  gender: string;
}

export interface PasswordUpdateRequest {
  currentPassword: string;
  newPassword: string;
}

export interface UserProfileResponse {
  id: number;
  username: string;
  email: string;
  fullName: string;
  phone?: string;
  birthDate?: string;
  gender?: string;
  createdAt: string;
  updatedAt: string;
}

/**
 * 사용자 관련 API 서비스
 */
export const userAPI = {
  /**
   * 내 프로필 정보 조회
   */
  getMyProfile: async (): Promise<UserProfileResponse> => {
    const response = await authApiClient.get<UserProfileResponse>("/user/me");
    return response.data;
  },

  /**
   * 사용자 정보 업데이트
   */
  updateUserInfo: async (userInfo: UserInfo): Promise<UserProfileResponse> => {
    const response = await authApiClient.put<UserProfileResponse>(
      "/user",
      userInfo
    );
    return response.data;
  },

  /**
   * 비밀번호 변경
   */
  updatePassword: async (
    passwordData: PasswordUpdateRequest
  ): Promise<void> => {
    await authApiClient.put("/user/password", passwordData);
  },

  /**
   * 계정 삭제 (탈퇴)
   */
  deleteAccount: async (password: string): Promise<void> => {
    await authApiClient.delete("/user", {
      data: { password },
    });
  },

  /**
   * 이메일 중복 검사
   */
  checkEmailDuplicate: async (
    email: string
  ): Promise<{ available: boolean }> => {
    const response = await authApiClient.post<{ available: boolean }>(
      "/user/check-email",
      { email }
    );
    return response.data;
  },

  /**
   * 사용자명 중복 검사
   */
  checkUsernameDuplicate: async (
    username: string
  ): Promise<{ available: boolean }> => {
    const response = await authApiClient.post<{ available: boolean }>(
      "/user/check-username",
      { username }
    );
    return response.data;
  },

  /**
   * 비밀번호 재설정 요청 (이메일 발송)
   */
  requestPasswordReset: async (email: string): Promise<void> => {
    await authApiClient.post("/user/password-reset-request", { email });
  },

  /**
   * 비밀번호 재설정 실행
   */
  resetPassword: async (token: string, newPassword: string): Promise<void> => {
    await authApiClient.post("/user/password-reset", {
      token,
      newPassword,
    });
  },

  /**
   * 이메일 인증 요청
   */
  requestEmailVerification: async (): Promise<void> => {
    await authApiClient.post("/user/email-verification-request");
  },

  /**
   * 이메일 인증 확인
   */
  verifyEmail: async (token: string): Promise<void> => {
    await authApiClient.post("/user/email-verification", { token });
  },
};
