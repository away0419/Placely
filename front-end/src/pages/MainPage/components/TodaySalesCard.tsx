import React from "react";
import "./TodaySalesCard.css";

// 오늘의 매출 카드 컴포넌트
const TodaySalesCard: React.FC = () => {
  // 더미 데이터 (실제 데이터 연동 시 API로 대체)
  const totalSales = 1250000; // 총합 매출액 (원)
  const avgSalesPerHour = 156250; // 시간당 평균 매출액 (원)

  return (
    <section className="dashboard-card today-sales-card">
      <h2>오늘의 매출</h2>
      <div className="today-sales-info">
        <div>
          <span className="today-sales-label">총합 매출액</span>
          <span className="today-sales-value">
            {totalSales.toLocaleString()}원
          </span>
        </div>
        <div>
          <span className="today-sales-label">시간당 평균 매출액</span>
          <span className="today-sales-value">
            {avgSalesPerHour.toLocaleString()}원
          </span>
        </div>
      </div>
    </section>
  );
};

export default TodaySalesCard;
