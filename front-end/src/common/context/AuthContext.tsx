import React, {
  createContext,
  useContext,
  useState,
  useEffect,
  useCallback,
} from "react";
import type { ReactNode } from "react";
import { authAPI, type User } from "../util/authAPI";

// AuthContext 타입 정의
interface AuthContextType {
  isAuthenticated: boolean;
  user: User | null;
  login: (username: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  loading: boolean;
  refreshUser: () => Promise<void>;
}

// AuthContext 생성
const AuthContext = createContext<AuthContextType | undefined>(undefined);

// AuthProvider 컴포넌트
interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(false);
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState<boolean>(true);

  // 초기 인증 상태 확인
  useEffect(() => {
    const checkAuthStatus = async () => {
      try {
        // 토큰 존재 여부 확인
        if (!authAPI.isAuthenticated()) {
          setLoading(false);
          return;
        }

        // 로컬 스토리지에서 사용자 정보 가져오기
        const savedUser = authAPI.getCurrentUser();
        if (savedUser) {
          setUser(savedUser);
          setIsAuthenticated(true);
        }

        // 서버와 토큰 유효성 검증 (선택적)
        try {
          await authAPI.validateToken();
          // 토큰이 유효하면 최신 사용자 정보 가져오기
          const latestUser = await authAPI.refreshUserInfo();
          setUser(latestUser);
          setIsAuthenticated(true);
        } catch (error) {
          // 토큰이 유효하지 않으면 로그아웃 처리
          console.warn("토큰 검증 실패, 로그아웃 처리:", error);
          setUser(null);
          setIsAuthenticated(false);
          // 로컬 데이터 정리는 validateToken에서 자동 처리됨
        }
      } catch (error) {
        console.error("인증 상태 확인 중 오류:", error);
        setUser(null);
        setIsAuthenticated(false);
      } finally {
        setLoading(false);
      }
    };

    checkAuthStatus();
  }, []);

  // 로그인 함수
  const login = useCallback(
    async (username: string, password: string): Promise<void> => {
      try {
        const response = await authAPI.login({ username, password });

        setUser(response.user);
        setIsAuthenticated(true);
      } catch (error) {
        console.error("로그인 실패:", error);
        throw error;
      }
    },
    []
  );

  // 로그아웃 함수
  const logout = useCallback(async (): Promise<void> => {
    try {
      await authAPI.logout();
    } catch (error) {
      console.error("로그아웃 중 오류:", error);
    } finally {
      // 로컬 상태 초기화
      setUser(null);
      setIsAuthenticated(false);
    }
  }, []);

  // 사용자 정보 새로고침
  const refreshUser = useCallback(async (): Promise<void> => {
    try {
      if (!isAuthenticated) return;

      const latestUser = await authAPI.refreshUserInfo();
      setUser(latestUser);
    } catch (error) {
      console.error("사용자 정보 새로고침 실패:", error);
      // 인증 오류인 경우 로그아웃 처리
      if (error instanceof Error && error.message.includes("인증")) {
        await logout();
      }
    }
  }, [isAuthenticated, logout]);

  const value: AuthContextType = {
    isAuthenticated,
    user,
    login,
    logout,
    loading,
    refreshUser,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

// AuthContext 사용을 위한 커스텀 훅
export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error("useAuth는 AuthProvider 내부에서만 사용할 수 있습니다.");
  }
  return context;
};

// 인증이 필요한 컴포넌트를 감싸는 HOC
interface ProtectedRouteProps {
  children: ReactNode;
}

export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children }) => {
  const { isAuthenticated, loading } = useAuth();

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-green-600"></div>
      </div>
    );
  }

  if (!isAuthenticated) {
    // 인증되지 않은 경우 로그인 페이지로 리다이렉트
    window.location.href = "/login";
    return null;
  }

  return <>{children}</>;
};
