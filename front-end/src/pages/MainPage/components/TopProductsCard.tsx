import React from "react";
import "./TopProductsCard.css";

// 팔린 상품 순위 카드 컴포넌트
const TopProductsCard: React.FC = () => {
  // 더미 데이터 (실제 데이터 연동 시 API로 대체)
  const topProducts = [
    { name: "아메리카노", sold: 120 },
    { name: "카페라떼", sold: 95 },
    { name: "카푸치노", sold: 80 },
    { name: "녹차라떼", sold: 60 },
    { name: "에스프레소", sold: 45 },
  ];

  return (
    <section className="dashboard-card top-products-card">
      <h2>팔린 상품 순위</h2>
      <ol className="top-products-list">
        {topProducts.map((item, idx) => (
          <li key={item.name}>
            <span className="top-products-rank">{idx + 1}위</span>
            <span className="top-products-name">{item.name}</span>
            <span className="top-products-sold">{item.sold}개</span>
          </li>
        ))}
      </ol>
    </section>
  );
};

export default TopProductsCard;
