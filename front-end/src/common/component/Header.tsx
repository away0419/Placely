import React, { useState } from "react";
import styles from "./Header.module.css";
import { useNavigate } from "react-router-dom";

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

  // 메뉴 클릭 시 이동
  const handleMenuClick = (path: string) => {
    navigate(path);
    setDrawerOpen(false);
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
      {/* 태블릿 이하: 햄버거 버튼 */}
      <button
        className={styles.mainHeaderIconBtn}
        aria-label="메뉴 열기"
        onClick={() => setDrawerOpen(true)}
      >
        <span className="material-icons">menu</span>
      </button>
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
            >
              <span className="material-icons">close</span>
            </button>
            {serviceMenus.map((menu) => (
              <button
                key={menu.key}
                className={styles.drawerMenuBtn}
                onClick={() => handleMenuClick(menu.path)}
              >
                {menu.label}
              </button>
            ))}
          </nav>
        </div>
      )}
    </header>
  );
};

export default Header;
