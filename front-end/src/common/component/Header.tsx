import React, { useState } from "react";
import styles from "./Header.module.css";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

// 서비스 메뉴 목록 정의
const serviceMenus = [
  { key: "dashboard", label: "대시보드", path: "/dashboard" },
  { key: "sales", label: "판매", path: "/sales" },
  { key: "product", label: "상품관리", path: "/product" },
  { key: "stock", label: "재고관리", path: "/stock" },
  { key: "analysis", label: "매출분석", path: "/analysis" },
  { key: "settings", label: "설정", path: "/settings" },
];

// 상단바: 로고, 메뉴 버튼, 내 정보 버튼
const Header: React.FC = () => {
  const [drawerOpen, setDrawerOpen] = useState(false);
  const navigate = useNavigate();
  const { isAuthenticated, user, logout } = useAuth();

  // 메뉴 클릭 시 이동
  const handleMenuClick = (path: string) => {
    navigate(path);
    setDrawerOpen(false);
  };

  // 로그아웃 처리
  const handleLogout = async () => {
    try {
      await logout();
      navigate("/login");
    } catch (error) {
      console.error("로그아웃 중 오류:", error);
    }
  };

  return (
    <header className={styles.mainHeader}>
      {/* 홈 아이콘 클릭 시 메인 페이지로 이동 */}
      <div
        className={styles.mainHeaderLogo}
        onClick={() => handleMenuClick("/")}
      >
        Placely
      </div>

      {/* PC/데스크탑: 서비스 메뉴 */}
      <nav className={styles.serviceMenu}>
        {serviceMenus.map((menu) => (
          <button
            key={menu.key}
            className={styles.menuBtn}
            onClick={() => handleMenuClick(menu.path)}
          >
            {menu.label}
          </button>
        ))}
      </nav>

      {/* 사용자 정보 영역 */}
      <div className={styles.mainHeaderActions}>
        {isAuthenticated ? (
          <>
            {/* 데스크톱: 사용자 정보와 버튼들 */}
            <div className="hidden xl:flex items-center space-x-3">
              <span className="text-sm text-gray-600">
                안녕하세요,{" "}
                <span className="font-semibold text-green-700">
                  {user?.fullName || user?.username}
                </span>
                님
              </span>
              <button
                onClick={() => navigate("/profile")}
                className="px-3 py-1.5 text-sm text-gray-600 hover:text-green-700 hover:bg-green-50 rounded-lg transition-all duration-200 font-medium"
              >
                내 정보
              </button>
              <button
                onClick={handleLogout}
                className="px-3 py-1.5 text-sm text-gray-600 hover:text-red-600 hover:bg-red-50 rounded-lg transition-all duration-200 font-medium"
              >
                로그아웃
              </button>
            </div>

            {/* 태블릿 이하: 햄버거 버튼 */}
            <button
              className={styles.mainHeaderIconBtn}
              aria-label="메뉴 열기"
              onClick={() => setDrawerOpen(true)}
            >
              <span className="material-icons">menu</span>
            </button>
          </>
        ) : (
          <>
            {/* 비로그인 상태: 로그인 버튼 */}
            <div className="hidden xl:flex items-center space-x-3">
              <button
                onClick={() => navigate("/login")}
                className="px-4 py-2 bg-green-700 text-white text-sm font-semibold rounded-lg hover:bg-green-800 transition-colors duration-200 shadow-sm"
                style={{ backgroundColor: "var(--color-primary)" }}
              >
                로그인
              </button>
            </div>

            {/* 태블릿 이하: 햄버거 버튼 */}
            <button
              className={styles.mainHeaderIconBtn}
              aria-label="메뉴 열기"
              onClick={() => setDrawerOpen(true)}
            >
              <span className="material-icons">menu</span>
            </button>
          </>
        )}
      </div>

      {/* 오른쪽 드로어 메뉴 */}
      {drawerOpen && (
        <div
          className={styles.drawerOverlay}
          onClick={() => setDrawerOpen(false)}
        >
          <nav className={styles.drawer} onClick={(e) => e.stopPropagation()}>
            <button
              className={styles.closeBtn}
              onClick={() => setDrawerOpen(false)}
              aria-label="메뉴 닫기"
            >
              <span className="material-icons">close</span>
            </button>

            {/* 사용자 정보 (모바일) */}
            {isAuthenticated && user && (
              <div className={styles.drawerUserInfo}>
                <p className="text-sm text-gray-600 mb-1">안녕하세요</p>
                <p className={`${styles.userName} text-lg`}>
                  {user.fullName || user.username}님
                </p>
              </div>
            )}

            {/* 서비스 메뉴 */}
            <div className="flex-1">
              {serviceMenus.map((menu) => (
                <button
                  key={menu.key}
                  className={styles.drawerMenuBtn}
                  onClick={() => handleMenuClick(menu.path)}
                >
                  {menu.label}
                </button>
              ))}
            </div>

            {/* 인증 관련 메뉴 */}
            <div className="border-t border-gray-200 mt-auto">
              {isAuthenticated ? (
                <>
                  <button
                    className={styles.drawerMenuBtn}
                    onClick={() => handleMenuClick("/profile")}
                  >
                    내 정보 관리
                  </button>
                  <button
                    className={`${styles.drawerMenuBtn} text-red-600 hover:text-red-700`}
                    onClick={handleLogout}
                  >
                    로그아웃
                  </button>
                </>
              ) : (
                <button
                  className={`${styles.drawerMenuBtn} text-green-700 font-semibold`}
                  onClick={() => handleMenuClick("/login")}
                >
                  로그인
                </button>
              )}
            </div>
          </nav>
        </div>
      )}
    </header>
  );
};

export default Header;
