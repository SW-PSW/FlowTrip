# FlowTrip

강원 지역 관광 데이터를 기반으로 사용자 맞춤형 여행지를 추천하고, 사용자가 직접 DAY별 여행 코스를 구성해 저장 및 공유할 수 있는 AI 여행 추천 웹 플랫폼입니다.

FlowTrip은 React 기반 초기 입력 화면과 Spring Boot + Thymeleaf 기반 결과/마이페이지/공유 게시판 화면을 함께 사용하는 혼합 구조로 구현되었습니다. 사용자는 여행 일정, 동행 유형, 이동수단, 여행 무드, 특이사항 등을 입력하고, 시스템은 자체 구축한 강원 관광 데이터와 생성형 AI, 네이버 API, 기상청 날씨 데이터를 활용해 추천 결과를 제공합니다.

## 주요 기능

- React 기반 초기 화면, 로그인, 회원가입, 여행 조건 입력
- Spring Security 기반 세션 로그인
- Excel 기반 강원 관광 데이터 import
- 사용자 조건 기반 여행 테마 및 장소 후보 필터링
- OpenAI API를 활용한 AI 여행지 추천
- 추천 사유, 추천 동선, 대체 장소, 주의사항 제공
- 네이버 지역 검색 API 기반 추천 장소 및 주변 업체 검색
- 주변 식당, 카페, 숙소, 관광지 조회
- 네이버 지도 JavaScript API 기반 지도 표시
- 장소 리뷰 및 이미지 조회
- 기상청 예보 기반 여행일 날씨 표시
- 네이버 검색 결과, 리뷰 수, 저장 수, 날씨를 반영한 혼잡도 분석
- DAY별 여행 코스 구성
- 여행 코스 DB 저장
- 마이페이지에서 회원별 저장 코스 조회
- 저장 코스 상세에서 DAY별 코스 확인 및 장소 삭제
- 여행 코스 공유 게시판
- 공유글 작성, 목록, 상세 조회
- 사용자별 공유 프로필 및 강원 지역 뱃지 표시

## 기술 스택

### Backend

- Java 17
- Spring Boot
- Spring Web MVC
- Spring Security
- Spring Data JPA
- Bean Validation
- Thymeleaf
- MySQL
- Apache POI
- Jackson

### Frontend

- React
- Vite
- HTML / CSS / JavaScript
- Thymeleaf Template

### External API

- OpenAI API
- Naver Search API
- Naver Map JavaScript API
- 기상청 날씨 데이터

## 프로젝트 구조

```text
src
└── main
    ├── java/capstone/hallym/xx/flowtrip
    │   ├── config          # Spring Security 설정
    │   ├── controller      # MVC/API Controller
    │   ├── dto             # 요청/응답 DTO
    │   ├── entity          # JPA Entity
    │   ├── init            # Excel 데이터 초기 적재
    │   ├── repository      # Spring Data JPA Repository
    │   └── service         # 추천, 검색, 날씨, 혼잡도, 인증 서비스
    │
    └── resources
        ├── data            # FlowTrip Excel 관광 데이터
        ├── static
        │   ├── images      # 배경 이미지, 로고, 뱃지 이미지
        │   └── react       # React 빌드 결과물
        └── templates       # Thymeleaf 화면
```

## 화면 역할 분리

이 프로젝트는 전체 화면을 React SPA로 구성하지 않습니다.

- React 담당
  - 초기 화면
  - 로그인
  - 회원가입
  - 여행 조건 입력

- Thymeleaf 담당
  - AI 추천 결과 화면
  - 주변 업체/지도/날씨/혼잡도 표시
  - 나의 여행 코스 목록 및 상세
  - 공유 게시판 목록/작성/상세
  - 사용자 프로필 및 뱃지 화면

React 빌드 결과물은 다음 경로에 위치합니다.

```text
src/main/resources/static/react
```

## 데이터 구조

FlowTrip은 자체 조사한 강원 지역 관광 데이터 Excel 파일을 사용합니다.

```text
src/main/resources/data/flowtrip_db.xlsx
```

애플리케이션 시작 시 `ExcelImportService`가 해당 Excel 파일을 읽어 지역, 테마, 장소, 태그 데이터를 MySQL에 적재합니다.

주요 데이터:

- 강원 지역 정보
- 지역별 여행 테마
- 여행지 및 장소 후보
- 장소 카테고리
- 무드, 동행자, 이동수단, 날씨 적합도
- 장소 태그 및 테마 태그

## 추천 방식

1. 사용자가 여행 조건을 입력합니다.
2. 서버가 Excel 기반 DB에서 조건에 맞는 테마와 장소 후보를 필터링합니다.
3. 후보 장소는 점수화되며, 지역/테마/카테고리 다양성 보정을 적용합니다.
4. 저장 수가 낮은 장소와 덜 노출된 후보도 일부 가산하여 반복 추천을 줄입니다.
5. OpenAI API가 후보 데이터 안에서 최종 추천 여행지를 선택합니다.
6. 네이버 API로 실제 장소 정보, 좌표, 주변 업체를 보정합니다.
7. 기상청 예보와 네이버 검색/리뷰/저장 수를 기반으로 혼잡도를 계산합니다.
8. 사용자는 추천 결과를 바탕으로 DAY별 코스를 구성합니다.

## 혼잡도 분석 지표

혼잡도는 다음 요소를 종합하여 0~100점으로 산출합니다.

- 추천 장소의 기본 특성
- 네이버 지역 검색 결과 수
- 네이버 블로그/리뷰 검색량
- FlowTrip 사용자 저장 수
- 여행일 날짜 및 주말 여부
- 여행 스타일 및 혼잡 회피 선호
- 기상청 날씨 예보

혼잡도 결과는 `낮음`, `보통`, `높음` 등급으로 제공됩니다.

## 실행 환경

- Java 17
- MySQL 8.x 권장
- Maven Wrapper 포함

## 환경 설정

보안상 API Key와 DB 접속 정보가 포함된 `application.properties` 파일은 Git에 포함하지 않습니다.

`.gitignore`에 다음 파일이 등록되어 있습니다.

```text
src/main/resources/application.properties
```

따라서 프로젝트를 실행하려면 로컬에서 직접 아래 파일을 생성해야 합니다.

```text
src/main/resources/application.properties
```

예시:

```properties
# Server
server.port=8080

# MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/flowtrip?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
spring.datasource.username=YOUR_DB_USERNAME
spring.datasource.password=YOUR_DB_PASSWORD
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# OpenAI
openai.api.key=YOUR_OPENAI_API_KEY

# Naver Search API
naver.search.client-id=YOUR_NAVER_SEARCH_CLIENT_ID
naver.search.client-secret=YOUR_NAVER_SEARCH_CLIENT_SECRET

# Naver Map JavaScript API
naver.map.client-id=YOUR_NAVER_MAP_CLIENT_ID
```

> 실제 API Key, DB 비밀번호, Client Secret은 절대 GitHub에 커밋하지 마세요.

## MySQL 데이터베이스 생성

로컬 MySQL에서 데이터베이스를 생성합니다.

```sql
CREATE DATABASE flowtrip
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

## 실행 방법

### 1. 저장소 클론

```bash
git clone https://github.com/SW-PSW/FlowTrip.git
cd FlowTrip
```

### 2. `application.properties` 생성

```text
src/main/resources/application.properties
```

위 파일에 MySQL 접속 정보와 OpenAI/Naver API Key를 입력합니다.

### 3. 애플리케이션 실행

```bash
./mvnw spring-boot:run
```

Windows 환경:

```bash
mvnw.cmd spring-boot:run
```

### 4. 접속

```text
http://localhost:8080
```

초기 접속 시 React 화면으로 이동합니다.

```text
http://localhost:8080/react/index.html
```

## 빌드 확인

```bash
./mvnw -DskipTests compile
```

## 주요 URL

| URL | 설명 |
|---|---|
| `/` | React 초기 화면으로 redirect |
| `/react/index.html` | React 초기 화면 |
| `/api/auth/signup` | React 회원가입 API |
| `/login` | Spring Security 로그인 |
| `/submit` | 여행 조건 제출 및 추천 결과 생성 |
| `/travel-result/latest` | 최근 추천 결과 다시 보기 |
| `/api/places/search` | 업체 직접 검색 |
| `/api/places/more` | 주변 업체 더보기 |
| `/api/place/detail` | 장소 리뷰/이미지 조회 |
| `/api/travel-course/save` | 여행 코스 저장 |
| `/my-travel` | 나의 여행 코스 목록 |
| `/my-travel/{id}` | 나의 여행 코스 상세 |
| `/my-travel/{id}/share` | 저장 코스 공유글 작성 |
| `/shared-travel` | 공유 게시판 목록 |
| `/shared-travel/{id}` | 공유 게시글 상세 |
| `/shared-travel/users/{userId}` | 사용자 공유 프로필 |

## 초기 테스트용 DB 리셋

처음부터 다시 테스트해야 할 경우 MySQL에서 아래 쿼리를 사용할 수 있습니다.

```sql
SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE shared_travel_posts;
TRUNCATE TABLE travel_course_items;
TRUNCATE TABLE travel_plans;

TRUNCATE TABLE place_tags;
TRUNCATE TABLE theme_tags;
TRUNCATE TABLE places;
TRUNCATE TABLE themes;
TRUNCATE TABLE tags;
TRUNCATE TABLE regions;

SET FOREIGN_KEY_CHECKS = 1;
```

회원 데이터까지 초기화하려면 `app_users`도 함께 비웁니다.

```sql
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE app_users;
SET FOREIGN_KEY_CHECKS = 1;
```

`regions` 테이블이 비어 있으면 애플리케이션 재시작 시 Excel 데이터가 다시 import됩니다.

## 주의 사항

- `src/main/resources/application.properties`는 Git에 포함하지 않습니다.
- OpenAI API Key, Naver Client ID/Secret, DB 비밀번호는 반드시 로컬에서만 관리해야 합니다.
- React는 초기 화면과 여행 조건 입력까지만 담당합니다.
- 추천 결과, 마이페이지, 공유 게시판은 Thymeleaf 기반 화면입니다.
- 전체 프로젝트를 React SPA로 전환하지 않는 구조입니다.
- `travel-result.html`은 Thymeleaf 변수와 네이버 지도 JavaScript API를 함께 사용합니다.

## 오픈소스 사용 목적

- Spring Boot: 백엔드 애플리케이션 실행 및 서버 구조 구성
- Spring Web MVC: HTTP 요청 처리 및 Controller 라우팅
- Spring Security: 로그인 인증 및 세션 관리
- Spring Data JPA: MySQL 데이터 저장/조회
- Thymeleaf: 서버 사이드 HTML 렌더링
- React: 초기 화면 및 여행 조건 입력 UI
- Vite: React 프론트엔드 빌드
- MySQL Connector/J: MySQL 연결
- Jackson Databind: JSON 파싱 및 객체 변환
- Apache POI: Excel 여행 데이터 읽기
- Bean Validation: 사용자 입력값 검증
- Lombok: Java 코드 작성 보조
- H2 Database: 테스트용 인메모리 DB
- Maven: 의존성 관리 및 빌드

## 프로젝트 특징

- 자체 구축한 강원 관광 데이터 400개 기반 추천
- 생성형 AI와 데이터 기반 필터링을 결합한 추천 구조
- 주변 업체, 날씨, 혼잡도, 지도 정보를 한 화면에서 제공
- 사용자가 직접 DAY별 코스를 구성하는 커스터마이징 방식
- 저장 코스 기반 마이페이지 및 공유 게시판 제공
- 공유 활동과 여행 코스 키워드 기반 지역 뱃지 기능 제공
