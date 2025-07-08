## 개요

<details><summary>프로젝트 DB 아키텍처</summary>

Placely는 AI 기반 매장 운영 인사이트 플랫폼으로, 4개의 마이크로서비스로 구성됩니다:

- **Auth 서비스**: 사용자 인증 및 권한 관리
- **POS 서비스**: 매장 운영 핵심 비즈니스 (상품, 판매, 재고)
- **AI 서비스**: AI 기반 추천 및 분석 시스템
- **Gateway 서비스**: API 라우팅 (DB 불필요)

</details>

<br/>

## 🚀 실행 방법

<details><summary>순차적 실행</summary>

```sql
-- Oracle SQL*Plus나 SQL Developer에서 순차적으로 실행
@run_all_tables_with_comments.sql        -- Part 1: Auth 서비스 테이블
@run_all_tables_with_comments_part2.sql  -- Part 2: POS 서비스 테이블
@run_all_tables_with_comments_part3.sql  -- Part 3: AI 서비스 테이블 + 인덱스 + 기본 데이터
```

</details>

<br/>

## 📊 테이블 구조 개요

#### Auth 서비스 (인증/권한)

<details><summary>Auth 서비스 테이블 (6개)</summary>

| 테이블명                | 설명             | 주요 특징              |
| ----------------------- | ---------------- | ---------------------- |
| `AUTH_USERS`            | 사용자 정보      | 완전한 감사 컬럼 적용  |
| `AUTH_ROLES`            | 역할 정보        | 완전한 감사 컬럼 적용  |
| `AUTH_USER_ROLES`       | 사용자-역할 매핑 | 단순 매핑 테이블       |
| `AUTH_PERMISSIONS`      | 권한 정보        | 완전한 감사 컬럼 적용  |
| `AUTH_ROLE_PERMISSIONS` | 역할-권한 매핑   | 단순 매핑 테이블       |
| `AUTH_TOKENS`           | JWT 토큰 관리    | 임시 데이터, 성능 우선 |

</details>

<br/>

#### POS 서비스 (매장 운영)

<details><summary>POS 서비스 테이블 (13개)</summary>

| 테이블명                  | 설명               | 주요 특징             |
| ------------------------- | ------------------ | --------------------- |
| `POS_OWNERS`              | 점주 정보          | 완전한 감사 컬럼 적용 |
| `POS_STORES`              | 매장 정보          | 완전한 감사 컬럼 적용 |
| `POS_STORE_USER_ROLES`    | 매장별 사용자 역할 | 복잡한 권한 분기 지원 |
| `POS_CATEGORIES`          | 상품 카테고리      | 계층 구조 지원        |
| `POS_OPTION_GROUPS`       | 옵션 그룹          | 옵션 관리 시스템      |
| `POS_OPTION_ITEMS`        | 옵션 항목          | 옵션 관리 시스템      |
| `POS_PRODUCTS`            | 상품 정보          | 완전한 감사 컬럼 적용 |
| `POS_PRODUCT_OPTIONS`     | 상품-옵션 연결     | 단순 연결 테이블      |
| `POS_INVENTORY`           | 재고 관리          | 완전한 감사 컬럼 적용 |
| `POS_SALES`               | 판매 헤더          | 완전한 감사 컬럼 적용 |
| `POS_SALE_ITEMS`          | 판매 상세          | 완전한 감사 컬럼 적용 |
| `POS_SALE_ITEM_OPTIONS`   | 판매 옵션 상세     | 이력 보존             |
| `POS_PAYMENTS`            | 결제 정보          | 이력 테이블           |
| `POS_INVENTORY_MOVEMENTS` | 재고 이동 이력     | 이력 테이블           |

</details>

<br/>

#### AI 서비스 (추천/분석)

<details><summary>AI 서비스 테이블 (8개)</summary>

| 테이블명                      | 설명               | 주요 특징                |
| ----------------------------- | ------------------ | ------------------------ |
| `AI_CUSTOMER_BEHAVIOR`        | 고객 행동 분석     | 대용량 데이터, 성능 우선 |
| `AI_RECOMMENDATIONS`          | 상품 추천 이력     | 대용량 데이터, 성능 우선 |
| `AI_SALES_PREDICTIONS`        | 판매 예측          | 기본 시간 정보만         |
| `AI_CUSTOMER_SEGMENTS`        | 고객 세그먼트      | 완전한 감사 컬럼 적용    |
| `AI_CUSTOMER_SEGMENT_MAPPING` | 고객-세그먼트 매핑 | 단순 매핑 테이블         |
| `AI_PRODUCT_ASSOCIATIONS`     | 상품 연관 분석     | 주기적 재생성            |
| `AI_MODEL_PERFORMANCE`        | AI 모델 성능 추적  | 로그 성격                |
| `AI_RECOMMENDATION_CACHE`     | 실시간 추천 캐시   | 캐시 테이블, 성능 우선   |

</details>

<br/>

## 🎯 주요 개선사항

#### 1. 코멘트 시스템

<details><summary>코멘트 예시</summary>

```sql
-- 기본키 컬럼 예시
COMMENT ON COLUMN AUTH_USERS.USER_ID IS '사용자 ID (Primary Key)';

-- 외래키 컬럼 예시
COMMENT ON COLUMN POS_STORES.USER_ID IS '점주 ID (Foreign Key: AUTH_USERS.USER_ID)';

-- 일반 컬럼 예시
COMMENT ON COLUMN POS_PRODUCTS.BASE_PRICE IS '기본 가격';

-- 제약조건 설명 예시
COMMENT ON COLUMN AUTH_USERS.STATUS IS '계정 상태 (ACTIVE:활성, INACTIVE:비활성, SUSPENDED:정지)';
```

</details>

<br/>

#### 2. 복잡한 권한 분기 시스템

<details><summary>매장별 권한 관리</summary>

- **POS_STORE_USER_ROLES** 테이블로 매장별 사용자 역할 매핑
- 한 사용자가 매장A에서는 점주, 매장B에서는 직원 역할 가능
- 권한 만료일, 부여자 추적, 상태 관리 지원
- 동적 권한 체크 시스템

```sql
-- 예시: 김사장이 본점에서는 점주, 지점에서는 매니저 역할
INSERT INTO POS_STORE_USER_ROLES (STORE_ID, USER_ID, ROLE_ID, NOTES)
VALUES (1, 100, 2, '본점 점주 권한');

INSERT INTO POS_STORE_USER_ROLES (STORE_ID, USER_ID, ROLE_ID, NOTES)
VALUES (2, 100, 3, '지점 매니저 권한');
```

</details>

<br/>

#### 3. 최적화된 감사 컬럼 전략

<details><summary>테이블 유형별 감사 컬럼 적용</summary>

- **✅ 완전한 감사 컬럼 (7개)**: 핵심 비즈니스 데이터

  - CREATED_AT, CREATED_BY, UPDATED_AT, UPDATED_BY
  - IS_DELETED, DELETED_AT, DELETED_BY

- **🔄 기본 시간 정보 (1-2개)**: 이력/로그 테이블

  - CREATED_AT, UPDATED_AT (선택적)

- **🔗 최소 정보**: 단순 매핑 테이블

  - GRANTED_AT, ASSIGNED_AT 등

- **⚡ 감사 컬럼 없음**: AI 분석/캐시 테이블
  - 성능 우선, 대용량 데이터 처리

</details>

<br/>

## 🔍 실행 후 확인

<details><summary>생성된 객체 확인</summary>

```sql
-- 테이블 생성 확인
SELECT 'Auth 서비스 테이블' AS SERVICE, COUNT(*) AS TABLE_COUNT
FROM USER_TABLES WHERE TABLE_NAME LIKE 'AUTH_%'
UNION ALL
SELECT 'POS 서비스 테이블' AS SERVICE, COUNT(*) AS TABLE_COUNT
FROM USER_TABLES WHERE TABLE_NAME LIKE 'POS_%'
UNION ALL
SELECT 'AI 서비스 테이블' AS SERVICE, COUNT(*) AS TABLE_COUNT
FROM USER_TABLES WHERE TABLE_NAME LIKE 'AI_%';

-- 시퀀스 확인
SELECT COUNT(*) AS SEQUENCE_COUNT
FROM USER_SEQUENCES
WHERE SEQUENCE_NAME LIKE 'SEQ_%';

-- 인덱스 확인
SELECT COUNT(*) AS INDEX_COUNT
FROM USER_INDEXES
WHERE INDEX_NAME LIKE 'IDX_%';

-- 코멘트 확인
SELECT COUNT(*) AS COMMENT_COUNT
FROM USER_COL_COMMENTS
WHERE COMMENTS IS NOT NULL;
```

</details>

<br/>

## 주요 설계 특징

<details><summary>설계 고려사항</summary>

1. **마이크로서비스 아키텍처 고려**

   - 서비스별 테이블 네이밍 (AUTH*, POS*, AI\_ 접두사)
   - 서비스 간 직접적인 FK 참조 최소화

2. **확장성 고려**

   - Oracle SEQUENCE를 통한 ID 자동 생성
   - JSON 컬럼(CLOB) 활용으로 유연한 데이터 구조

3. **성능 최적화**

   - 주요 조회 패턴에 맞는 인덱스 생성
   - AI 서비스용 캐시 테이블 별도 구성

4. **데이터 무결성**
   - CHECK 제약조건으로 유효값 검증
   - 적절한 FK 관계 설정

</details>

<br/>

## 향후 확장 계획

<details><summary>확장 가능한 영역</summary>

1. **쿠폰/할인 시스템**

   - 프로모션 관리 테이블
   - 쿠폰 발급/사용 이력

2. **배송/주문 시스템**

   - 온라인 주문 관리
   - 배송 상태 추적

3. **포인트/멤버십 시스템**

   - 포인트 적립/사용 이력
   - 멤버십 등급별 혜택

4. **리포팅/대시보드**
   - 집계 테이블 (OLAP)
   - 실시간 매출 모니터링

</details>

<br/>

## 📝 사용 시 주의사항

<details><summary>개발 시 참고사항</summary>

1. **Oracle 환경 요구사항**

   - Oracle 11g 이상 권장
   - 충분한 테이블스페이스 용량 확보

2. **JPA Entity 매핑 시**

   - Oracle은 컬럼명을 대문자로 생성
   - `@Column(name = "USER_ID")` 형태로 매핑 필요

3. **시퀀스 사용**

   - 모든 기본키는 시퀀스로 자동 생성
   - `SEQ_테이블명.NEXTVAL` 형태로 사용

4. **외래키 제약조건**
   - 데이터 삽입 시 참조 무결성 확인
   - 테스트 데이터 삽입 순서 고려

</details>

<br/>
