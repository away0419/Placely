import React from "react";
import "./StockStatusCard.css";

// 현재 재고 카드 컴포넌트
const StockStatusCard: React.FC = () => {
  // 더미 데이터 (실제 데이터 연동 시 API로 대체)
  const stockList = [
    { name: "아메리카노", stock: 32 },
    { name: "카페라떼", stock: 18 },
    { name: "카푸치노", stock: 7 },
    { name: "녹차라떼", stock: 12 },
    { name: "에스프레소", stock: 25 },
  ];

  return (
    <section className="dashboard-card stock-status-card">
      <h2>현재 재고</h2>
      <ul className="stock-status-list">
        {stockList.map((item) => (
          <li key={item.name}>
            <span className="stock-status-name">{item.name}</span>
            <span className="stock-status-qty">{item.stock}개</span>
          </li>
        ))}
      </ul>
    </section>
  );
};

export default StockStatusCard;
