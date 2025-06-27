-- 테스트용 사용자 데이터 생성 스크립트
-- 비밀번호는 모두 "password123" (BCrypt 인코딩됨)

-- 1. 기본 사용자 (점주)
INSERT INTO AUTH_USERS (
    USER_ID, USERNAME, EMAIL, PASSWORD_HASH, PHONE, FULL_NAME, BIRTH_DATE, 
    GENDER, STATUS, CREATED_AT, UPDATED_AT, IS_DELETED
) VALUES (
    SEQ_AUTH_USERS.NEXTVAL, 
    'testowner', 
    'testowner@placely.com', 
    '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', -- password123
    '010-1111-2222', 
    '테스트 점주', 
    DATE '1980-01-01',
    'M', 
    'ACTIVE', 
    CURRENT_TIMESTAMP, 
    CURRENT_TIMESTAMP, 
    'N'
);

-- 2. 일반 직원
INSERT INTO AUTH_USERS (
    USER_ID, USERNAME, EMAIL, PASSWORD_HASH, PHONE, FULL_NAME, BIRTH_DATE, 
    GENDER, STATUS, CREATED_AT, UPDATED_AT, IS_DELETED
) VALUES (
    SEQ_AUTH_USERS.NEXTVAL, 
    'teststaff', 
    'teststaff@placely.com', 
    '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', -- password123
    '010-3333-4444', 
    '테스트 직원', 
    DATE '1990-01-01',
    'F', 
    'ACTIVE', 
    CURRENT_TIMESTAMP, 
    CURRENT_TIMESTAMP, 
    'N'
);

-- 3. 관리자
INSERT INTO AUTH_USERS (
    USER_ID, USERNAME, EMAIL, PASSWORD_HASH, PHONE, FULL_NAME, BIRTH_DATE, 
    GENDER, STATUS, CREATED_AT, UPDATED_AT, IS_DELETED
) VALUES (
    SEQ_AUTH_USERS.NEXTVAL, 
    'admin', 
    'admin@placely.com', 
    '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', -- password123
    '010-5555-6666', 
    '시스템 관리자', 
    DATE '1975-01-01',
    'M', 
    'ACTIVE', 
    CURRENT_TIMESTAMP, 
    CURRENT_TIMESTAMP, 
    'N'
);

-- 사용자별 역할 부여
-- 점주 역할
INSERT INTO AUTH_USER_ROLES (USER_ID, ROLE_ID) 
SELECT u.USER_ID, r.ROLE_ID 
FROM AUTH_USERS u, AUTH_ROLES r 
WHERE u.USERNAME = 'testowner' AND r.ROLE_NAME = 'OWNER';

-- 직원 역할
INSERT INTO AUTH_USER_ROLES (USER_ID, ROLE_ID) 
SELECT u.USER_ID, r.ROLE_ID 
FROM AUTH_USERS u, AUTH_ROLES r 
WHERE u.USERNAME = 'teststaff' AND r.ROLE_NAME = 'CASHIER';

-- 관리자 역할
INSERT INTO AUTH_USER_ROLES (USER_ID, ROLE_ID) 
SELECT u.USER_ID, r.ROLE_ID 
FROM AUTH_USERS u, AUTH_ROLES r 
WHERE u.USERNAME = 'admin' AND r.ROLE_NAME = 'ADMIN';

COMMIT;

-- 생성된 테스트 사용자 확인
SELECT 
    u.USER_ID,
    u.USERNAME,
    u.EMAIL,
    u.FULL_NAME,
    r.ROLE_NAME
FROM AUTH_USERS u
LEFT JOIN AUTH_USER_ROLES ur ON u.USER_ID = ur.USER_ID
LEFT JOIN AUTH_ROLES r ON ur.ROLE_ID = r.ROLE_ID
WHERE u.USERNAME IN ('testowner', 'teststaff', 'admin')
ORDER BY u.USERNAME; 