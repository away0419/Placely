import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { BrowserRouter } from "react-router-dom";
import Router from "./router";
import "./index.css";
import Header from "./common/component/Header";

// 앱 진입점 및 라우터 적용 (한글 주석)
createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <BrowserRouter>
      {/* 상단바 */}
      <Header />
      <Router />
    </BrowserRouter>
  </StrictMode>
);
