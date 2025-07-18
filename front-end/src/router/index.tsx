import { Routes, Route } from "react-router-dom";
import MainPage from "../pages/MainPage/MainPage";
import Dashboard from "../pages/Dashboard/Dashboard";
import StoreManagement from "../pages/StoreManagement/StoreManagement";
import ProductManagement from "../pages/ProductManagement/ProductManagement";
import OptionManagement from "../pages/OptionManagement/OptionManagement";
import OrderHistory from "../pages/OrderHistory/OrderHistory";
import StaffKiosk from "../pages/StaffKiosk/StaffKiosk";
import Login from "../pages/Auth/Login";
import UserProfile from "../pages/UserProfile/UserProfile";
import { ProtectedRoute } from "../common/context/AuthContext";

// 라우팅 구조 정의 (한글 주석)
const Router = () => (
  <Routes>
    <Route path="/" element={<MainPage />} />
    <Route path="/login" element={<Login />} />
    <Route
      path="/profile"
      element={
        <ProtectedRoute>
          <UserProfile />
        </ProtectedRoute>
      }
    />
    <Route
      path="/dashboard"
      element={
        <ProtectedRoute>
          <Dashboard />
        </ProtectedRoute>
      }
    />
    <Route
      path="/store"
      element={
        <ProtectedRoute>
          <StoreManagement />
        </ProtectedRoute>
      }
    />
    <Route
      path="/product"
      element={
        <ProtectedRoute>
          <ProductManagement />
        </ProtectedRoute>
      }
    />
    <Route
      path="/option"
      element={
        <ProtectedRoute>
          <OptionManagement />
        </ProtectedRoute>
      }
    />
    <Route
      path="/order"
      element={
        <ProtectedRoute>
          <OrderHistory />
        </ProtectedRoute>
      }
    />
    <Route
      path="/kiosk"
      element={
        <ProtectedRoute>
          <StaffKiosk />
        </ProtectedRoute>
      }
    />
  </Routes>
);

export default Router;
