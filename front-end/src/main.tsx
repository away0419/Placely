import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { BrowserRouter } from "react-router-dom";
import Router from "./router";
import "./index.css";
import Header from "./common/component/Header";
import { AuthProvider } from "./common/context/AuthContext";

// 앱 진입점 및 라우터 적용 (한글 주석)
createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <BrowserRouter>
      <AuthProvider>
        {/* 상단바 */}
        <Header />
        <Router />
      </AuthProvider>
    </BrowserRouter>
  </StrictMode>
);
