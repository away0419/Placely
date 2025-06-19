import React from "react";
import TodaySalesCard from "./components/TodaySalesCard";
import TopProductsCard from "./components/TopProductsCard";
import StockStatusCard from "./components/StockStatusCard";
import styles from "./MainPage.module.css"; // 스타일 분리(필요시)

// 메인 대시보드 페이지: 오늘의 정보, 상단바, 카드형 레이아웃
const MainPage: React.FC = () => {
  return (
    <div className={styles.mainPageRoot}>
      {/* 대시보드 카드 영역 */}
      <main className={styles.dashboardMain}>
        <TodaySalesCard />
        <TopProductsCard />
        <StockStatusCard />
      </main>
    </div>
  );
};

export default MainPage;
