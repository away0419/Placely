import { defineConfig, loadEnv } from "vite";
import react from "@vitejs/plugin-react";
import { VitePWA } from "vite-plugin-pwa";
import path from "path";

// 환경별로 .env 파일을 자동으로 로드하도록 설정 (한글 주석)
export default defineConfig(({ mode }) => {
  // .env.common 파일을 먼저 로드하고, 그 다음에 mode별 환경변수를 로드하여 병합
  const commonEnv = loadEnv("common", process.cwd(), "");
  const modeEnv = loadEnv(mode, process.cwd(), "");

  // common과 mode 환경변수를 병합 (mode가 우선순위가 높음)
  const env = { ...commonEnv, ...modeEnv };

  console.log("====== 실행 환경 ======");
  // console.log("Merged Env:", env);
  console.log("Mode:", mode);
  console.log("=======================");

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
      // Vite 표준: import.meta.env를 통해 브라우저에서 접근할 수 있도록 환경변수 주입
      "import.meta.env.VITE_AUTH_API_URL": JSON.stringify(
        env.VITE_AUTH_API_URL
      ),
      "import.meta.env.VITE_PORT": JSON.stringify(env.VITE_PORT),
      "import.meta.env.VITE_TEST_KEY": JSON.stringify(env.VITE_TEST_KEY),
    },
  };
});
// .env.common 파일이 모든 모드에서 기본으로 로드되고,
// mode별 환경변수가 그 위에 덮어씌워짐
