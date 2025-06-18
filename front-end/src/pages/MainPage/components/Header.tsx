import React from "react";
import "./Header.css";

// 상단바: 로고, 메뉴 버튼, 내 정보 버튼
const Header: React.FC = () => {
  return (
    <header className="main-header">
      <div className="main-header__logo">Placely</div>
      <div className="main-header__actions">
        {/* 메뉴(햄버거) 버튼 */}
        <button className="main-header__icon-btn" aria-label="메뉴 열기">
          <span className="material-icons">menu</span>
        </button>
        {/* 내 정보(프로필) 버튼 */}
        <button className="main-header__icon-btn" aria-label="내 정보">
          <span className="material-icons">account_circle</span>
        </button>
      </div>
    </header>
  );
};

export default Header;
