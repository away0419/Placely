import React from "react";

interface LoadingSpinnerProps {
  size?: "small" | "medium" | "large";
  color?: "primary" | "secondary" | "white" | "gray";
  fullScreen?: boolean;
  message?: string;
  className?: string;
}

const LoadingSpinner: React.FC<LoadingSpinnerProps> = ({
  size = "medium",
  color = "primary",
  fullScreen = false,
  message,
  className = "",
}) => {
  // 크기 설정
  const sizeClasses = {
    small: "h-4 w-4",
    medium: "h-8 w-8",
    large: "h-12 w-12",
  };

  // 색상 설정
  const colorClasses = {
    primary: "border-green-600",
    secondary: "border-green-400",
    white: "border-white",
    gray: "border-gray-400",
  };

  // 스피너 컴포넌트
  const spinner = (
    <div className="flex flex-col items-center justify-center">
      <div
        className={`animate-spin rounded-full border-2 border-transparent border-t-2 ${sizeClasses[size]} ${colorClasses[color]} ${className}`}
        style={{ borderTopColor: "currentColor" }}
      />
      {message && (
        <p className="mt-3 text-sm text-gray-600 text-center max-w-xs">
          {message}
        </p>
      )}
    </div>
  );

  // 전체 화면 스피너
  if (fullScreen) {
    return (
      <div className="fixed inset-0 z-50 flex items-center justify-center bg-white bg-opacity-90 backdrop-blur-sm">
        {spinner}
      </div>
    );
  }

  // 일반 스피너
  return spinner;
};

export default LoadingSpinner;
