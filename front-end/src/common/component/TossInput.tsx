import React from "react";

interface TossInputProps {
  label: string;
  type?: string;
  placeholder?: string;
  value: string;
  onChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
  required?: boolean;
  error?: string;
  disabled?: boolean;
}

// Toss 스타일의 입력 필드 컴포넌트
const TossInput: React.FC<TossInputProps> = ({
  label,
  type = "text",
  placeholder,
  value,
  onChange,
  required = false,
  error,
  disabled = false,
}) => {
  return (
    <div className="w-full">
      <label className="block text-sm font-medium text-gray-700 mb-2">
        {label}
        {required && <span className="text-red-500 ml-1">*</span>}
      </label>
      <input
        type={type}
        value={value}
        onChange={onChange}
        placeholder={placeholder}
        required={required}
        disabled={disabled}
        className={`w-full px-3 py-3 border rounded-lg shadow-sm placeholder-gray-400 focus:outline-none focus:ring-2 focus:border-transparent transition-all duration-200 ${
          error
            ? "border-red-300 focus:ring-red-500"
            : "border-gray-300 focus:ring-green-500"
        } ${disabled ? "bg-gray-50 cursor-not-allowed" : ""}`}
      />
      {error && <p className="mt-1 text-sm text-red-600">{error}</p>}
    </div>
  );
};

export default TossInput;
