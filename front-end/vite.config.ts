import { defineConfig, loadEnv } from "vite";
import react from "@vitejs/plugin-react";
import { VitePWA } from "vite-plugin-pwa";
import path from "path";

// 환경별로 .env 파일을 자동으로 로드하도록 설정 (한글 주석)
export default defineConfig(({ mode }) => {
  // .env, .env.local, .env.dev, .env.prod 등 환경별 변수 자동 로드
  const env = loadEnv(mode, process.cwd(), "");

  return {
    plugins: [
      react(),
      VitePWA({
        registerType: "autoUpdate",
        manifest: {
          name: "Placely",
          short_name: "Placely",
          description: "AI 기반 매장 운영 인사이트 플랫폼",
          theme_color: "#ffffff",
          icons: [
            {
              src: "/icon-192x192.png",
              sizes: "192x192",
              type: "image/png",
            },
            {
              src: "/icon-512x512.png",
              sizes: "512x512",
              type: "image/png",
            },
          ],
        },
      }),
    ],
    resolve: {
      alias: {
        "@": path.resolve(__dirname, "src"), // 절대경로 import 지원
      },
    },
    server: {
      port: env.VITE_PORT ? Number(env.VITE_PORT) : 5173, // 환경별 포트 지정
    },
    define: {
      "process.env": env, // 환경변수 전체 주입
    },
  };
});
// 환경별 .env 파일을 통해 local, dev, prod 세팅을 분리할 수 있음
