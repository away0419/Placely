import { Routes, Route } from "react-router-dom";
import MainPage from "../pages/MainPage/MainPage";
import Dashboard from "../pages/Dashboard/Dashboard";
import StoreManagement from "../pages/StoreManagement/StoreManagement";
import ProductManagement from "../pages/ProductManagement/ProductManagement";
import OptionManagement from "../pages/OptionManagement/OptionManagement";
import OrderHistory from "../pages/OrderHistory/OrderHistory";
import StaffKiosk from "../pages/StaffKiosk/StaffKiosk";

// 라우팅 구조 정의 (한글 주석)
const Router = () => (
  <Routes>
    <Route path="/" element={<MainPage />} />
    <Route path="/dashboard" element={<Dashboard />} />
    <Route path="/stores" element={<StoreManagement />} />
    <Route path="/products" element={<ProductManagement />} />
    <Route path="/options" element={<OptionManagement />} />
    <Route path="/orders" element={<OrderHistory />} />
    <Route path="/kiosk" element={<StaffKiosk />} />
  </Routes>
);

export default Router;
