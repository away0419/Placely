import React from "react";

interface LoadingSpinnerProps {
  size?: "small" | "medium" | "large";
  color?: string;
}

// Toss 스타일의 로딩 스피너 컴포넌트
const LoadingSpinner: React.FC<LoadingSpinnerProps> = ({
  size = "medium",
  color = "var(--color-primary)",
}) => {
  const sizeClasses = {
    small: "h-4 w-4",
    medium: "h-8 w-8",
    large: "h-12 w-12",
  };

  return (
    <div className="flex justify-center items-center">
      <div
        className={`animate-spin rounded-full border-2 border-gray-200 border-t-current ${sizeClasses[size]}`}
        style={{ borderTopColor: color }}
      />
    </div>
  );
};

export default LoadingSpinner;
