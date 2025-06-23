## 개요

<details><summary>프로젝트 DB 아키텍처</summary>

Placely는 AI 기반 매장 운영 인사이트 플랫폼으로, 4개의 마이크로서비스로 구성됩니다:

- **Auth 서비스**: 사용자 인증 및 권한 관리
- **POS 서비스**: 매장 운영 핵심 비즈니스 (상품, 판매, 재고)
- **AI 서비스**: AI 기반 추천 및 분석 시스템
- **Gateway 서비스**: API 라우팅 (DB 불필요)

</details>

<br/>

## 서비스별 테이블 구조

### Auth 서비스 (인증/인가)

<details><summary>Auth 서비스 테이블 목록</summary>

| 테이블명                | 설명             | 주요 컬럼                                          |
| ----------------------- | ---------------- | -------------------------------------------------- |
| `AUTH_USERS`            | 사용자 정보      | USER_ID, USERNAME, EMAIL, PASSWORD_HASH            |
| `AUTH_ROLES`            | 역할 정보        | ROLE_ID, ROLE_NAME (ADMIN, MANAGER, CASHIER, USER) |
| `AUTH_USER_ROLES`       | 사용자-역할 매핑 | USER_ID, ROLE_ID                                   |
| `AUTH_PERMISSIONS`      | 권한 정보        | PERMISSION_ID, RESOURCE_TYPE, ACTION_TYPE          |
| `AUTH_ROLE_PERMISSIONS` | 역할-권한 매핑   | ROLE_ID, PERMISSION_ID                             |
| `AUTH_TOKENS`           | JWT 토큰 관리    | TOKEN_ID, USER_ID, TOKEN_TYPE, EXPIRES_AT          |

**기본 역할**: ADMIN(관리자), MANAGER(매장관리자), CASHIER(계산원), USER(일반사용자)

</details>

<br/>

### POS 서비스 (매장 운영)

<details><summary>POS 서비스 테이블 목록</summary>

| 테이블명                  | 설명           | 주요 컬럼                                                    |
| ------------------------- | -------------- | ------------------------------------------------------------ |
| `POS_OWNERS`              | 점주 정보      | OWNER_ID, EMAIL, OWNER_NAME, STATUS_CODE                     |
| `POS_STORES`              | 매장 정보      | STORE_ID, OWNER_ID, STORE_NAME, STORE_CODE, STATUS_CODE      |
| `POS_CATEGORIES`          | 상품 카테고리  | CATEGORY_ID, CATEGORY_NAME, PARENT_ID, STATUS_CODE           |
| `POS_OPTION_GROUPS`       | 옵션 그룹      | OPTION_GROUP_ID, GROUP_NAME (사이즈/온도/시럽)               |
| `POS_OPTION_ITEMS`        | 옵션 항목      | OPTION_ITEM_ID, ITEM_NAME (S/M/L, 아이스/핫)                 |
| `POS_PRODUCTS`            | 상품 정보      | PRODUCT_ID, STORE_ID, PRODUCT_CODE, BASE_PRICE               |
| `POS_PRODUCT_OPTIONS`     | 상품-옵션 연결 | PRODUCT_ID, OPTION_ITEM_ID, EXTRA_PRICE                      |
| `POS_INVENTORY`           | 재고 관리      | STORE_ID, PRODUCT_ID, CURRENT_STOCK                          |
| `POS_SALES`               | 판매 헤더      | SALE_ID, STORE_ID, RECEIPT_NUMBER, TOTAL_AMOUNT, STATUS_CODE |
| `POS_SALE_ITEMS`          | 판매 상세      | SALE_ID, PRODUCT_ID, QUANTITY, BASE_PRICE                    |
| `POS_SALE_ITEM_OPTIONS`   | 판매 옵션 상세 | SALE_ITEM_ID, OPTION_NAME, OPTION_PRICE                      |
| `POS_PAYMENTS`            | 결제 정보      | PAYMENT_ID, SALE_ID, PAYMENT_METHOD, STATUS_CODE             |
| `POS_INVENTORY_MOVEMENTS` | 재고 이동 이력 | MOVEMENT_TYPE (IN/OUT/ADJUST)                                |

**결제 방법**: CASH(현금), CARD(카드), QR(QR결제), MIXED(복합결제)

</details>

<br/>

### AI 서비스 (추천/분석)

<details><summary>AI 서비스 테이블 목록</summary>

| 테이블명                      | 설명               | 주요 컬럼                                 |
| ----------------------------- | ------------------ | ----------------------------------------- |
| `AI_CUSTOMER_BEHAVIOR`        | 고객 행동 분석     | SESSION_ID, AGE_GROUP, VISIT_DURATION     |
| `AI_RECOMMENDATIONS`          | 상품 추천 이력     | RECOMMENDATION_TYPE, SCORE_VALUE, CLICKED |
| `AI_SALES_PREDICTIONS`        | 판매 예측          | PREDICTED_QUANTITY, CONFIDENCE_LEVEL      |
| `AI_CUSTOMER_SEGMENTS`        | 고객 세그먼트      | SEGMENT_NAME, CRITERIA (JSON)             |
| `AI_CUSTOMER_SEGMENT_MAPPING` | 고객-세그먼트 매핑 | CUSTOMER_ID, SEGMENT_ID, CONFIDENCE       |
| `AI_PRODUCT_ASSOCIATIONS`     | 상품 연관분석      | SUPPORT, CONFIDENCE, LIFT                 |
| `AI_MODEL_PERFORMANCE`        | AI 모델 성능 추적  | MODEL_NAME, METRIC_VALUE                  |
| `AI_RECOMMENDATION_CACHE`     | 실시간 추천 캐시   | CACHE_KEY, RECOMMENDATIONS (JSON)         |

**추천 타입**: POPULAR(인기상품), SIMILAR(유사상품), CROSS_SELL(교차판매), SEASONAL(계절상품)
**고객 세그먼트**: VIP고객, 단골고객, 신규고객, 이탈위험고객

</details>

<br/>

## 실행 방법

### 전체 테이블 생성

<details><summary>Oracle SQL*Plus에서 실행</summary>

```sql
-- Oracle DB 접속 후
@00_create_all_tables.sql
```

**또는 개별 실행:**

```sql
@01_auth_tables.sql    -- Auth 서비스 테이블
@02_pos_tables.sql     -- POS 서비스 테이블
@03_ai_tables.sql      -- AI 서비스 테이블
```

</details>

<br/>

## 주요 설계 특징

### 데이터베이스 설계 원칙

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

### 추가 예정 기능

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

## 참고사항

### 개발 환경 설정

<details><summary>로컬 개발시 주의사항</summary>

- Oracle 11g 이상 권장
- 테이블스페이스 충분한 용량 확보
- 개발용 데이터 별도 스크립트 준비 필요
- JPA Entity 매핑시 컬럼명 대소문자 주의

</details>
