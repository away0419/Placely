/**
 * 새로운 API 구조 사용 예시
 *
 * 이 파일은 개선된 API 구조를 어떻게 사용하는지 보여주는 예시입니다.
 * 실제 컴포넌트에서 이런 방식으로 사용하시면 됩니다.
 */

import { authAPI } from "./authAPI";
import { userAPI } from "./userAPI";
import LoadingSpinner from "../component/LoadingSpinner";

// ========================================
// 1. 로그인 예시
// ========================================
export const handleLogin = async (username: string, password: string) => {
  try {
    // 🚀 로그인 (토큰 자동 저장됨)
    const response = await authAPI.login({ username, password });
    console.log("로그인 성공:", response.user);

    // 리다이렉트 또는 상태 업데이트
    window.location.href = "/dashboard";
  } catch (error) {
    console.error("로그인 실패:", error);
    // 에러 메시지 표시
    alert(error instanceof Error ? error.message : "로그인에 실패했습니다.");
  }
};

// ========================================
// 2. 로그아웃 예시
// ========================================
export const handleLogout = async () => {
  try {
    // 🚀 로그아웃 (토큰 자동 삭제됨)
    await authAPI.logout();
    console.log("로그아웃 완료");

    // 로그인 페이지로 리다이렉트
    window.location.href = "/login";
  } catch (error) {
    console.error("로그아웃 실패:", error);
  }
};

// ========================================
// 3. 프로필 정보 조회 예시
// ========================================
export const loadUserProfile = async () => {
  try {
    // 🚀 내 프로필 정보 가져오기 (토큰 자동 포함됨)
    const profile = await userAPI.getMyProfile();
    console.log("프로필 정보:", profile);
    return profile;
  } catch (error) {
    console.error("프로필 로드 실패:", error);
    throw error;
  }
};

// ========================================
// 4. 사용자 정보 수정 예시
// ========================================
export const updateProfile = async (userInfo: {
  email: string;
  phone: string;
  fullName: string;
  birthDate: string;
  gender: string;
}) => {
  try {
    // 🚀 사용자 정보 업데이트
    const updatedProfile = await userAPI.updateUserInfo(userInfo);
    console.log("업데이트 완료:", updatedProfile);
    return updatedProfile;
  } catch (error) {
    console.error("프로필 업데이트 실패:", error);
    throw error;
  }
};

// ========================================
// 5. 비밀번호 변경 예시
// ========================================
export const changePassword = async (
  currentPassword: string,
  newPassword: string
) => {
  try {
    // 🚀 비밀번호 변경
    await userAPI.updatePassword({ currentPassword, newPassword });
    console.log("비밀번호 변경 완료");
    alert("비밀번호가 성공적으로 변경되었습니다.");
  } catch (error) {
    console.error("비밀번호 변경 실패:", error);
    alert(
      error instanceof Error ? error.message : "비밀번호 변경에 실패했습니다."
    );
  }
};

// ========================================
// 6. 토큰 유효성 검사 예시
// ========================================
export const checkAuthStatus = async () => {
  try {
    // 🚀 토큰 존재 여부 먼저 확인
    if (!authAPI.isAuthenticated()) {
      console.log("토큰이 없습니다.");
      return false;
    }

    // 🚀 서버와 토큰 유효성 검증
    const isValid = await authAPI.validateToken();
    console.log("토큰 유효성:", isValid);
    return isValid;
  } catch (error) {
    console.error("토큰 검증 실패:", error);
    return false;
  }
};

// ========================================
// 7. React 컴포넌트에서 사용 예시
// ========================================
/*
const ProfilePage: React.FC = () => {
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const loadData = async () => {
      try {
        setLoading(true);
        const profileData = await loadUserProfile();
        setProfile(profileData);
      } catch (err) {
        setError('프로필을 불러올 수 없습니다.');
      } finally {
        setLoading(false);
      }
    };

    loadData();
  }, []);

  if (loading) {
    return <LoadingSpinner fullScreen message="프로필을 불러오는 중..." />;
  }

  if (error) {
    return <div className="text-red-600">{error}</div>;
  }

  return (
    <div>
      <h1>내 프로필</h1>
      {profile && (
        <div>
          <p>이름: {profile.fullName}</p>
          <p>이메일: {profile.email}</p>
          // ... 기타 프로필 정보
        </div>
      )}
    </div>
  );
};
*/

// ========================================
// 8. 에러 처리 패턴 예시
// ========================================
export const apiCallWithErrorHandling = async () => {
  try {
    const result = await userAPI.getMyProfile();
    return { success: true, data: result };
  } catch (error) {
    // 에러 타입별 처리
    if (error instanceof Error) {
      if (error.message.includes("인증")) {
        // 인증 에러 - 자동으로 로그인 페이지로 리다이렉트됨
        return { success: false, error: "AUTH_ERROR", message: error.message };
      } else if (error.message.includes("네트워크")) {
        // 네트워크 에러
        return {
          success: false,
          error: "NETWORK_ERROR",
          message: error.message,
        };
      } else {
        // 기타 에러
        return {
          success: false,
          error: "UNKNOWN_ERROR",
          message: error.message,
        };
      }
    }

    return {
      success: false,
      error: "UNKNOWN_ERROR",
      message: "알 수 없는 오류가 발생했습니다.",
    };
  }
};
