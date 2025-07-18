import React, { useState, useEffect } from "react";
import { userAPI } from "../../common/util/userAPI.js";

interface UserInfo {
  email: string;
  phone: string;
  fullName: string;
  birthDate: string;
  gender: string;
}

interface PasswordData {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}

// 사용자 프로필 페이지 - 정보 수정과 비밀번호 변경
const UserProfile: React.FC = () => {
  const [activeTab, setActiveTab] = useState<"info" | "password">("info");
  const [userInfo, setUserInfo] = useState<UserInfo>({
    email: "",
    phone: "",
    fullName: "",
    birthDate: "",
    gender: "",
  });
  const [passwordData, setPasswordData] = useState<PasswordData>({
    currentPassword: "",
    newPassword: "",
    confirmPassword: "",
  });
  const [isLoading, setIsLoading] = useState(false);
  const [message, setMessage] = useState("");
  const [isError, setIsError] = useState(false);

  // 사용자 정보 입력 처리
  const handleUserInfoChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ) => {
    const { name, value } = e.target;
    setUserInfo((prev) => ({
      ...prev,
      [name]: value,
    }));
    clearMessage();
  };

  // 비밀번호 입력 처리
  const handlePasswordChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setPasswordData((prev) => ({
      ...prev,
      [name]: value,
    }));
    clearMessage();
  };

  // 메시지 초기화
  const clearMessage = () => {
    if (message) {
      setMessage("");
      setIsError(false);
    }
  };

  // 사용자 정보 업데이트
  const handleUpdateUserInfo = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);

    try {
      await userAPI.updateUserInfo(userInfo);
      setMessage("사용자 정보가 성공적으로 업데이트되었습니다.");
      setIsError(false);
    } catch (error) {
      setMessage("사용자 정보 업데이트에 실패했습니다.");
      setIsError(true);
    } finally {
      setIsLoading(false);
    }
  };

  // 비밀번호 변경
  const handleUpdatePassword = async (e: React.FormEvent) => {
    e.preventDefault();

    if (passwordData.newPassword !== passwordData.confirmPassword) {
      setMessage("새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
      setIsError(true);
      return;
    }

    if (passwordData.newPassword.length < 8) {
      setMessage("비밀번호는 8자 이상이어야 합니다.");
      setIsError(true);
      return;
    }

    setIsLoading(true);

    try {
      await userAPI.updatePassword({
        currentPassword: passwordData.currentPassword,
        newPassword: passwordData.newPassword,
      });
      setMessage("비밀번호가 성공적으로 변경되었습니다.");
      setIsError(false);
      setPasswordData({
        currentPassword: "",
        newPassword: "",
        confirmPassword: "",
      });
    } catch (error) {
      setMessage("비밀번호 변경에 실패했습니다. 현재 비밀번호를 확인해주세요.");
      setIsError(true);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* 헤더 */}
        <div className="bg-white rounded-lg shadow-sm p-6 mb-6">
          <h1 className="text-2xl font-bold text-gray-900">내 정보 관리</h1>
          <p className="text-gray-600 mt-1">
            개인정보와 보안 설정을 관리하세요
          </p>
        </div>

        {/* 탭 메뉴 */}
        <div className="bg-white rounded-lg shadow-sm">
          <div className="border-b border-gray-200">
            <nav className="flex space-x-8 px-6">
              <button
                onClick={() => setActiveTab("info")}
                className={`py-4 px-1 border-b-2 font-medium text-sm transition-colors ${
                  activeTab === "info"
                    ? "border-green-500 text-green-600"
                    : "border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300"
                }`}
              >
                개인정보 수정
              </button>
              <button
                onClick={() => setActiveTab("password")}
                className={`py-4 px-1 border-b-2 font-medium text-sm transition-colors ${
                  activeTab === "password"
                    ? "border-green-500 text-green-600"
                    : "border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300"
                }`}
              >
                비밀번호 변경
              </button>
            </nav>
          </div>

          <div className="p-6">
            {/* 메시지 표시 */}
            {message && (
              <div
                className={`mb-6 p-4 rounded-lg ${
                  isError
                    ? "bg-red-50 border border-red-200 text-red-600"
                    : "bg-green-50 border border-green-200 text-green-600"
                }`}
              >
                {message}
              </div>
            )}

            {/* 개인정보 수정 탭 */}
            {activeTab === "info" && (
              <form onSubmit={handleUpdateUserInfo} className="space-y-6">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  {/* 이메일 */}
                  <div>
                    <label
                      htmlFor="email"
                      className="block text-sm font-medium text-gray-700 mb-2"
                    >
                      이메일
                    </label>
                    <input
                      type="email"
                      id="email"
                      name="email"
                      value={userInfo.email}
                      onChange={handleUserInfoChange}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent"
                      placeholder="이메일을 입력하세요"
                    />
                  </div>

                  {/* 전화번호 */}
                  <div>
                    <label
                      htmlFor="phone"
                      className="block text-sm font-medium text-gray-700 mb-2"
                    >
                      전화번호
                    </label>
                    <input
                      type="tel"
                      id="phone"
                      name="phone"
                      value={userInfo.phone}
                      onChange={handleUserInfoChange}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent"
                      placeholder="010-1234-5678"
                    />
                  </div>

                  {/* 이름 */}
                  <div>
                    <label
                      htmlFor="fullName"
                      className="block text-sm font-medium text-gray-700 mb-2"
                    >
                      이름
                    </label>
                    <input
                      type="text"
                      id="fullName"
                      name="fullName"
                      value={userInfo.fullName}
                      onChange={handleUserInfoChange}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent"
                      placeholder="이름을 입력하세요"
                    />
                  </div>

                  {/* 생년월일 */}
                  <div>
                    <label
                      htmlFor="birthDate"
                      className="block text-sm font-medium text-gray-700 mb-2"
                    >
                      생년월일
                    </label>
                    <input
                      type="date"
                      id="birthDate"
                      name="birthDate"
                      value={userInfo.birthDate}
                      onChange={handleUserInfoChange}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent"
                    />
                  </div>

                  {/* 성별 */}
                  <div>
                    <label
                      htmlFor="gender"
                      className="block text-sm font-medium text-gray-700 mb-2"
                    >
                      성별
                    </label>
                    <select
                      id="gender"
                      name="gender"
                      value={userInfo.gender}
                      onChange={handleUserInfoChange}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent"
                    >
                      <option value="">선택해주세요</option>
                      <option value="MALE">남성</option>
                      <option value="FEMALE">여성</option>
                    </select>
                  </div>
                </div>

                <div className="flex justify-end">
                  <button
                    type="submit"
                    disabled={isLoading}
                    className="px-6 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 focus:ring-2 focus:ring-green-500 disabled:opacity-50 transition-colors"
                    style={{ backgroundColor: "var(--color-primary)" }}
                  >
                    {isLoading ? "저장 중..." : "정보 저장"}
                  </button>
                </div>
              </form>
            )}

            {/* 비밀번호 변경 탭 */}
            {activeTab === "password" && (
              <form
                onSubmit={handleUpdatePassword}
                className="space-y-6 max-w-md"
              >
                {/* 현재 비밀번호 */}
                <div>
                  <label
                    htmlFor="currentPassword"
                    className="block text-sm font-medium text-gray-700 mb-2"
                  >
                    현재 비밀번호
                  </label>
                  <input
                    type="password"
                    id="currentPassword"
                    name="currentPassword"
                    value={passwordData.currentPassword}
                    onChange={handlePasswordChange}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent"
                    placeholder="현재 비밀번호를 입력하세요"
                    required
                  />
                </div>

                {/* 새 비밀번호 */}
                <div>
                  <label
                    htmlFor="newPassword"
                    className="block text-sm font-medium text-gray-700 mb-2"
                  >
                    새 비밀번호
                  </label>
                  <input
                    type="password"
                    id="newPassword"
                    name="newPassword"
                    value={passwordData.newPassword}
                    onChange={handlePasswordChange}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent"
                    placeholder="새 비밀번호를 입력하세요 (8자 이상)"
                    required
                  />
                </div>

                {/* 비밀번호 확인 */}
                <div>
                  <label
                    htmlFor="confirmPassword"
                    className="block text-sm font-medium text-gray-700 mb-2"
                  >
                    비밀번호 확인
                  </label>
                  <input
                    type="password"
                    id="confirmPassword"
                    name="confirmPassword"
                    value={passwordData.confirmPassword}
                    onChange={handlePasswordChange}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent"
                    placeholder="새 비밀번호를 다시 입력하세요"
                    required
                  />
                </div>

                <div className="flex justify-end">
                  <button
                    type="submit"
                    disabled={isLoading}
                    className="px-6 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 focus:ring-2 focus:ring-green-500 disabled:opacity-50 transition-colors"
                    style={{ backgroundColor: "var(--color-primary)" }}
                  >
                    {isLoading ? "변경 중..." : "비밀번호 변경"}
                  </button>
                </div>
              </form>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default UserProfile;
