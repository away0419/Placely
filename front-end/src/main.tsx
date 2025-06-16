import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import MainPage from "./pages/MainPage";
import Dashboard from "./pages/Dashboard";
import StoreManagement from "./pages/StoreManagement";
import ProductManagement from "./pages/ProductManagement";
import OptionManagement from "./pages/OptionManagement";
import OrderHistory from "./pages/OrderHistory";
import StaffKiosk from "./pages/StaffKiosk";
import "./index.css";

// 라우팅 구조 정의 (한글 주석)
createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<MainPage />} />
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/stores" element={<StoreManagement />} />
        <Route path="/products" element={<ProductManagement />} />
        <Route path="/options" element={<OptionManagement />} />
        <Route path="/orders" element={<OrderHistory />} />
        <Route path="/kiosk" element={<StaffKiosk />} />
      </Routes>
    </BrowserRouter>
  </StrictMode>
);
