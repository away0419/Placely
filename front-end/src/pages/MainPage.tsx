import React from "react";
import { Link } from "react-router-dom";
import "./MainPage.css"; // 스타일 분리(필요시)

// 메인화면: 서비스 소개 및 주요 메뉴 네비게이션
const MainPage: React.FC = () => {
  return (
    <div className="main-page-container">
      <h1>Placely</h1>
      <p>AI 기반 매장 운영 인사이트 플랫폼</p>
      {/* 주요 메뉴 네비게이션 */}
      <nav className="main-nav">
        <Link to="/dashboard" className="main-nav-btn">
          대시보드
        </Link>
        <Link to="/stores" className="main-nav-btn">
          매장 관리
        </Link>
        <Link to="/products" className="main-nav-btn">
          상품 관리
        </Link>
        <Link to="/options" className="main-nav-btn">
          옵션 관리
        </Link>
        <Link to="/orders" className="main-nav-btn">
          주문/매출 이력
        </Link>
        <Link to="/kiosk" className="main-nav-btn">
          직원/키오스크
        </Link>
      </nav>
      {/* 추후: 로그인/회원가입, 서비스 소개 등 추가 가능 */}
    </div>
  );
};

export default MainPage;
