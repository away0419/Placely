-- ============================================================================
-- Placely 프로젝트 Oracle DB 통합 실행 스크립트 (코멘트 포함)
-- ============================================================================
-- 
-- 이 스크립트는 다음 파일들을 순차적으로 실행합니다:
-- 1. run_all_tables_with_comments.sql (Auth 서비스 테이블)
-- 2. run_all_tables_with_comments_part2.sql (POS 서비스 테이블)
-- 3. run_all_tables_with_comments_part3.sql (AI 서비스 테이블 + 인덱스 + 기본 데이터)
--
-- 실행 방법: @run_all_commented_tables.sql
-- ============================================================================

-- 스풀 로그 시작
SPOOL placely_table_creation.log;

-- 실행 시작 메시지
SELECT 'Placely 데이터베이스 테이블 생성 시작...' AS MESSAGE FROM DUAL;
SELECT TO_CHAR(SYSDATE, 'YYYY-MM-DD HH24:MI:SS') AS START_TIME FROM DUAL;

-- ============================================================================
-- Part 1: Auth 서비스 테이블 실행
-- ============================================================================
SELECT 'Part 1: Auth 서비스 테이블 생성 중...' AS MESSAGE FROM DUAL;
@run_all_tables_with_comments.sql

-- ============================================================================
-- Part 2: POS 서비스 테이블 실행
-- ============================================================================
SELECT 'Part 2: POS 서비스 테이블 생성 중...' AS MESSAGE FROM DUAL;
@run_all_tables_with_comments_part2.sql

-- ============================================================================
-- Part 3: AI 서비스 테이블 + 인덱스 + 기본 데이터 실행
-- ============================================================================
SELECT 'Part 3: AI 서비스 테이블, 인덱스, 기본 데이터 생성 중...' AS MESSAGE FROM DUAL;
@run_all_tables_with_comments_part3.sql

-- ============================================================================
-- 최종 실행 결과 확인
-- ============================================================================
SELECT 'Placely 데이터베이스 테이블 생성 완료!' AS MESSAGE FROM DUAL;
SELECT TO_CHAR(SYSDATE, 'YYYY-MM-DD HH24:MI:SS') AS END_TIME FROM DUAL;

-- 생성된 객체 수 확인
SELECT '=== 생성된 객체 수 확인 ===' AS MESSAGE FROM DUAL;

-- 테이블 수 확인
SELECT 'Auth 서비스 테이블' AS SERVICE, COUNT(*) AS TABLE_COUNT 
FROM USER_TABLES WHERE TABLE_NAME LIKE 'AUTH_%'
UNION ALL
SELECT 'POS 서비스 테이블' AS SERVICE, COUNT(*) AS TABLE_COUNT 
FROM USER_TABLES WHERE TABLE_NAME LIKE 'POS_%'
UNION ALL
SELECT 'AI 서비스 테이블' AS SERVICE, COUNT(*) AS TABLE_COUNT 
FROM USER_TABLES WHERE TABLE_NAME LIKE 'AI_%'
UNION ALL
SELECT '전체 테이블' AS SERVICE, COUNT(*) AS TABLE_COUNT 
FROM USER_TABLES WHERE TABLE_NAME LIKE 'AUTH_%' OR TABLE_NAME LIKE 'POS_%' OR TABLE_NAME LIKE 'AI_%';

-- 시퀀스 수 확인
SELECT 'SEQ' AS OBJECT_TYPE, COUNT(*) AS OBJECT_COUNT 
FROM USER_SEQUENCES 
WHERE SEQUENCE_NAME LIKE 'SEQ_%';

-- 인덱스 수 확인
SELECT 'INDEX' AS OBJECT_TYPE, COUNT(*) AS OBJECT_COUNT 
FROM USER_INDEXES 
WHERE INDEX_NAME LIKE 'IDX_%';

-- 코멘트 수 확인
SELECT 'COMMENT' AS OBJECT_TYPE, COUNT(*) AS OBJECT_COUNT 
FROM USER_COL_COMMENTS 
WHERE COMMENTS IS NOT NULL 
  AND TABLE_NAME IN (
    SELECT TABLE_NAME FROM USER_TABLES 
    WHERE TABLE_NAME LIKE 'AUTH_%' OR TABLE_NAME LIKE 'POS_%' OR TABLE_NAME LIKE 'AI_%'
  );

-- 기본 데이터 확인
SELECT '=== 기본 데이터 확인 ===' AS MESSAGE FROM DUAL;

SELECT 'AUTH_ROLES' AS TABLE_NAME, COUNT(*) AS ROW_COUNT FROM AUTH_ROLES
UNION ALL
SELECT 'AUTH_PERMISSIONS' AS TABLE_NAME, COUNT(*) AS ROW_COUNT FROM AUTH_PERMISSIONS
UNION ALL
SELECT 'POS_CATEGORIES' AS TABLE_NAME, COUNT(*) AS ROW_COUNT FROM POS_CATEGORIES
UNION ALL
SELECT 'POS_OPTION_GROUPS' AS TABLE_NAME, COUNT(*) AS ROW_COUNT FROM POS_OPTION_GROUPS
UNION ALL
SELECT 'POS_OPTION_ITEMS' AS TABLE_NAME, COUNT(*) AS ROW_COUNT FROM POS_OPTION_ITEMS
UNION ALL
SELECT 'AI_CUSTOMER_SEGMENTS' AS TABLE_NAME, COUNT(*) AS ROW_COUNT FROM AI_CUSTOMER_SEGMENTS;

-- 실행 결과 요약
SELECT '=== 실행 결과 요약 ===' AS MESSAGE FROM DUAL;
SELECT 
  '총 ' || (SELECT COUNT(*) FROM USER_TABLES WHERE TABLE_NAME LIKE 'AUTH_%' OR TABLE_NAME LIKE 'POS_%' OR TABLE_NAME LIKE 'AI_%') ||
  '개의 테이블, ' || (SELECT COUNT(*) FROM USER_SEQUENCES WHERE SEQUENCE_NAME LIKE 'SEQ_%') ||
  '개의 시퀀스, ' || (SELECT COUNT(*) FROM USER_INDEXES WHERE INDEX_NAME LIKE 'IDX_%') ||
  '개의 인덱스가 생성되었습니다.' AS SUMMARY
FROM DUAL;

-- 에러 발생 시 확인할 수 있는 쿼리
SELECT '=== 에러 발생 시 확인 ===' AS MESSAGE FROM DUAL;
SELECT 'USER_ERRORS 테이블을 확인하세요: SELECT * FROM USER_ERRORS;' AS ERROR_CHECK FROM DUAL;

-- 스풀 로그 종료
SPOOL OFF;

-- 완료 메시지
SELECT 'Placely 데이터베이스 테이블 생성이 완료되었습니다!' AS MESSAGE FROM DUAL;
SELECT 'placely_table_creation.log 파일에서 상세 로그를 확인하세요.' AS LOG_MESSAGE FROM DUAL; 