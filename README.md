[![codecov](https://codecov.io/github/SB14-ikujo/sb14-deokhugam-team02/graph/badge.svg?token=FMH1DWHUZA)](https://codecov.io/github/SB14-ikujo/sb14-deokhugam-team02)

<div align="center">

<a href="http://deokhugam-alb-742426925.ap-northeast-2.elb.amazonaws.com/#/"><img src="src/main/resources/static/images/nav/deokhugam.svg" alt="덕후감 배포 홈페이지" width="300"></a>

# 📚 DEOKHUGAM

### 책 사진 한 장으로 ISBN을 찾아 도서를 등록하고, 리뷰로 소통하는 도서 리뷰 서비스

</div>

<br>

## 📝 프로젝트 소개

**덕후감**은 도서 정보를 간편하게 등록하고, 사용자 간 리뷰와 소통을 지원하는 **도서 리뷰 백엔드 서비스**입니다.
OCR 기반 ISBN 인식과 외부 도서 API 연동으로 도서 등록 과정을 간소화하고, 리뷰·좋아요·댓글·알림 기능을 통해 사용자 간 상호작용을 지원합니다.
또한 **Spring Batch** 기반의 주기적인 데이터 집계를 통해 인기 도서, 인기 리뷰, 파워 유저 순위를 일간·주간·월간·역대 기준으로 제공합니다.

| 구분       | 내용                                                                                 |
|----------|------------------------------------------------------------------------------------|
| 📅 개발 기간 | 2026.09.29 ~ 2026.11.06                                                            |
| 🌐 배포 주소 | [덕후감 서비스 바로가기](http://deokhugam-alb-742426925.ap-northeast-2.elb.amazonaws.com/#/) |
| 📖 스웨거 문서 | [Swagger UI 바로가기](http://deokhugam-alb-742426925.ap-northeast-2.elb.amazonaws.com/swagger-ui/index.html#/) |

## 🛠 기술 스택

| 구분            | 기술                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            |
|:--------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 💻 언어 · 빌드    | ![Java 17](https://img.shields.io/badge/Java_17-ED8B00?style=flat-square&logo=openjdk&logoColor=white) ![Gradle 9.7.1](https://img.shields.io/badge/Gradle_9.7.1-02303A?style=flat-square&logo=gradle&logoColor=white)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| 🌱 프레임워크      | ![Spring Boot 4.1.1](https://img.shields.io/badge/Spring_Boot_4.1.1-6DB33F?style=flat-square&logo=springboot&logoColor=white) ![Spring Web MVC](https://img.shields.io/badge/Spring_Web_MVC-6DB33F?style=flat-square&logo=spring&logoColor=white) ![Validation](https://img.shields.io/badge/Validation-6DB33F?style=flat-square&logo=spring&logoColor=white) ![Spring Batch](https://img.shields.io/badge/Spring_Batch-6DB33F?style=flat-square&logo=spring&logoColor=white) ![Spring Scheduler](https://img.shields.io/badge/Spring_Scheduler-6DB33F?style=flat-square&logo=spring&logoColor=white) ![Actuator](https://img.shields.io/badge/Actuator-6DB33F?style=flat-square&logo=spring&logoColor=white) |
| 🗄 데이터        | ![PostgreSQL 15](https://img.shields.io/badge/PostgreSQL_15-4169E1?style=flat-square&logo=postgresql&logoColor=white) ![Spring Data JPA](https://img.shields.io/badge/Spring_Data_JPA-6DB33F?style=flat-square&logo=spring&logoColor=white) ![QueryDSL 7.0](https://img.shields.io/badge/QueryDSL_7.0-3B6FD9?style=flat-square)                                                                                                                                                                                                                                                                                                                                                                               |
| 🔌 외부 연동      | ![Kakao Book API](https://img.shields.io/badge/Kakao_Book_API-FFCD00?style=flat-square&logo=kakaotalk&logoColor=black) ![OCR.space](https://img.shields.io/badge/OCR.space-0057B8?style=flat-square) ![AWS S3 SDK v2](https://img.shields.io/badge/AWS_S3_SDK_v2-569A31?style=flat-square&logo=amazons3&logoColor=white)                                                                                                                                                                                                                                                                                                                                                                                      |
| 🧰 라이브러리      | ![MapStruct 1.6.3](https://img.shields.io/badge/MapStruct_1.6.3-12A594?style=flat-square) ![Lombok](https://img.shields.io/badge/Lombok-CF3030?style=flat-square) ![spring-security-crypto](https://img.shields.io/badge/spring--security--crypto-6DB33F?style=flat-square&logo=springsecurity&logoColor=white) ![springdoc-openapi 3.1.1](https://img.shields.io/badge/springdoc--openapi_3.1.1-85EA2D?style=flat-square&logo=swagger&logoColor=black)                                                                                                                                                                                                                                                       |
| 🧪 테스트 코드     | ![JUnit 5](https://img.shields.io/badge/JUnit_5-25A162?style=flat-square&logo=junit5&logoColor=white)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| 📈 커버리지 관리    | ![JaCoCo 0.8.13](https://img.shields.io/badge/JaCoCo_0.8.13-4C9A2A?style=flat-square) ![Codecov](https://img.shields.io/badge/Codecov-F01F7A?style=flat-square&logo=codecov&logoColor=white)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| 🧹 코드 품질 · 리뷰 | ![Checkstyle 12.3.0](https://img.shields.io/badge/Checkstyle_12.3.0-2C8EBB?style=flat-square) ![Claude Code Action](https://img.shields.io/badge/Claude_Code_Action-D97757?style=flat-square&logo=anthropic&logoColor=white) ![Git Hooks](https://img.shields.io/badge/Git_Hooks-F05032?style=flat-square&logo=git&logoColor=white) ![EditorConfig](https://img.shields.io/badge/EditorConfig-FEFEFE?style=flat-square&logo=editorconfig&logoColor=black)                                                                                                                                                                                                                                                     |
| 📜 로깅         | ![Logback + MDC](https://img.shields.io/badge/Logback_+_MDC-555555?style=flat-square) ![Elasticsearch](https://img.shields.io/badge/Elasticsearch-005571?style=flat-square&logo=elasticsearch&logoColor=white) ![Logstash](https://img.shields.io/badge/Logstash-005571?style=flat-square&logo=logstash&logoColor=white) ![Kibana](https://img.shields.io/badge/Kibana-005571?style=flat-square&logo=kibana&logoColor=white)                                                                                                                                                                                                                                                                                  |
| ☁️ 인프라 · 배포   | ![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat-square&logo=docker&logoColor=white) ![docker-compose](https://img.shields.io/badge/docker--compose-2496ED?style=flat-square&logo=docker&logoColor=white) ![AWS ECS](https://img.shields.io/badge/AWS_ECS_Fargate-FF9900?style=flat-square&logo=amazonecs&logoColor=white) ![AWS ECR](https://img.shields.io/badge/AWS_ECR-FF9900?style=flat-square&logo=amazonwebservices&logoColor=white) ![AWS RDS](https://img.shields.io/badge/AWS_RDS-527FFF?style=flat-square&logo=amazonrds&logoColor=white)                                                                                                                                           |
| ⚙️ CI/CD      | ![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-2088FF?style=flat-square&logo=githubactions&logoColor=white)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
| 🤝 협업툴        | ![Git](https://img.shields.io/badge/Git-F05032?style=flat-square&logo=git&logoColor=white) ![GitHub](https://img.shields.io/badge/GitHub-181717?style=flat-square&logo=github&logoColor=white) ![Notion](https://img.shields.io/badge/Notion-000000?style=flat-square&logo=notion&logoColor=white) ![Discord](https://img.shields.io/badge/Discord-5865F2?style=flat-square&logo=discord&logoColor=white) ![ZEP 강의실](https://img.shields.io/badge/ZEP_강의실-7C3AED?style=flat-square)                                                                                                                                                                                                                           |

## 👥 팀원별 소개

<table align="center">
<tr>
  <td align="center" width="180">
    <a href="https://github.com/xian980"><img src="https://github.com/xian980.png" width="110" alt="강성준"/></a><br/>
    <a href="https://github.com/xian980"><img src="https://img.shields.io/badge/GitHub-xian980-444C56?style=flat-square&logo=github&logoColor=white&labelColor=2D333B"/></a>
  </td>
  <td align="center" width="180">
    <a href="https://github.com/SB-14-KSJ"><img src="https://github.com/SB-14-KSJ.png" width="110" alt="김세중"/></a><br/>
    <a href="https://github.com/SB-14-KSJ"><img src="https://img.shields.io/badge/GitHub-SB--14--KSJ-444C56?style=flat-square&logo=github&logoColor=white&labelColor=2D333B"/></a>
  </td>
  <td align="center" width="180">
    <a href="https://github.com/yyy2724"><img src="https://github.com/yyy2724.png" width="110" alt="김양현"/></a><br/>
    <a href="https://github.com/yyy2724"><img src="https://img.shields.io/badge/GitHub-yyy2724-444C56?style=flat-square&logo=github&logoColor=white&labelColor=2D333B"/></a>
  </td>
  <td align="center" width="180">
    <a href="https://github.com/pengji-dev"><img src="https://github.com/pengji-dev.png" width="110" alt="김지현"/></a><br/>
    <a href="https://github.com/pengji-dev"><img src="https://img.shields.io/badge/GitHub-pengji--dev-444C56?style=flat-square&logo=github&logoColor=white&labelColor=2D333B"/></a>
  </td>
  <td align="center" width="180">
    <a href="https://github.com/ziezz767"><img src="https://github.com/ziezz767.png" width="110" alt="박성욱"/></a><br/>
    <a href="https://github.com/ziezz767"><img src="https://img.shields.io/badge/GitHub-ziezz767-444C56?style=flat-square&logo=github&logoColor=white&labelColor=2D333B"/></a>
  </td>
  <td align="center" width="180">
    <a href="https://github.com/EUNSOLY"><img src="https://github.com/EUNSOLY.png" width="110" alt="이은솔"/></a><br/>
    <a href="https://github.com/EUNSOLY"><img src="https://img.shields.io/badge/GitHub-EUNSOLY-444C56?style=flat-square&logo=github&logoColor=white&labelColor=2D333B"/></a>
  </td>
</tr>
<tr>
  <td align="center" valign="top"><b>강성준</b><br/><br/><sub><kbd><b>사용자&nbsp;도메인</b></kbd><br/>·&nbsp;회원가입&nbsp;·&nbsp;로그인<br/>·&nbsp;사용자&nbsp;조회&nbsp;·&nbsp;닉네임&nbsp;수정<br/>·&nbsp;논리&nbsp;삭제&nbsp;·&nbsp;물리&nbsp;삭제&nbsp;스케줄러<br/><kbd><b>헬퍼</b></kbd>&nbsp;🤝<br/>·&nbsp;도서&nbsp;상세&nbsp;조회&nbsp;API</sub></td>
  <td align="center" valign="top"><b>김세중</b><br/><br/><sub><kbd><b>댓글&nbsp;도메인</b></kbd><br/>·&nbsp;댓글&nbsp;CRUD&nbsp;·&nbsp;목록&nbsp;조회<br/>·&nbsp;부적절한&nbsp;단어&nbsp;필터링<br/>·&nbsp;댓글&nbsp;알림&nbsp;연결<br/><kbd><b>공통</b></kbd><br/>·&nbsp;README&nbsp;문서&nbsp;정리</sub></td>
  <td align="center" valign="top"><b>김양현</b><br/><br/><sub><kbd><b>대시보드&nbsp;도메인</b></kbd><br/>·&nbsp;인기&nbsp;도서&nbsp;·&nbsp;리뷰&nbsp;·&nbsp;파워&nbsp;유저&nbsp;집계<br/><kbd><b>심화</b></kbd>&nbsp;🚀<br/>·&nbsp;Spring&nbsp;Batch&nbsp;스케줄러<br/>·&nbsp;OCR&nbsp;ISBN&nbsp;인식&nbsp;API<br/><kbd><b>공통</b></kbd><br/>·&nbsp;헥사고날&nbsp;패키지&nbsp;설계<br/>·&nbsp;중앙&nbsp;예외&nbsp;처리<br/>·&nbsp;개발&nbsp;인프라&nbsp;구성</sub></td>
  <td align="center" valign="top"><b>김지현</b><br/><br/><sub><kbd><b>도서&nbsp;도메인</b></kbd><br/>·&nbsp;ISBN&nbsp;검증<br/>·&nbsp;도서&nbsp;등록&nbsp;·&nbsp;수정&nbsp;·&nbsp;삭제<br/>·&nbsp;카카오&nbsp;도서&nbsp;검색<br/>·&nbsp;썸네일&nbsp;처리</sub></td>
  <td align="center" valign="top"><b>박성욱</b><br/><br/><sub><kbd><b>리뷰&nbsp;·&nbsp;좋아요&nbsp;도메인</b></kbd><br/>·&nbsp;리뷰&nbsp;CRUD&nbsp;·&nbsp;목록&nbsp;조회<br/>·&nbsp;리뷰&nbsp;좋아요&nbsp;토글<br/>·&nbsp;좋아요&nbsp;알림&nbsp;연결<br/>·&nbsp;동시성&nbsp;제어<br/>·&nbsp;N+1&nbsp;개선&nbsp;·&nbsp;연쇄&nbsp;삭제<br/>·&nbsp;리뷰&nbsp;도메인&nbsp;테스트</sub></td>
  <td align="center" valign="top"><b>이은솔</b><br/><br/><sub><kbd><b>알림&nbsp;도메인</b></kbd><br/>·&nbsp;알림&nbsp;자동&nbsp;생성<br/>·&nbsp;알림&nbsp;조회&nbsp;·&nbsp;읽음&nbsp;처리<br/>·&nbsp;오래된&nbsp;알림&nbsp;삭제&nbsp;배치<br/><kbd><b>공통</b></kbd><br/>·&nbsp;AWS&nbsp;인프라&nbsp;구성<br/>·&nbsp;CD&nbsp;구축&nbsp;(무중단&nbsp;자동&nbsp;배포)<br/>·&nbsp;MDC&nbsp;로깅<br/>·&nbsp;이미지&nbsp;스토리지<br/>·&nbsp;환경별&nbsp;프로필&nbsp;관리<br/>·&nbsp;로그&nbsp;정기&nbsp;적재&nbsp;🚀<br/>·&nbsp;코드&nbsp;품질<br/><kbd><b>헬퍼</b></kbd>&nbsp;🤝<br/>·&nbsp;도서&nbsp;검색&nbsp;·&nbsp;정렬<br/>·&nbsp;리뷰&nbsp;집계&nbsp;동시성&nbsp;제어</sub></td>
</tr>
</table>

<p align="center"><sub>🤝 헬퍼 (본인 담당 도메인 외에 함께 구현한 부분) &nbsp; 🚀 심화 과제</sub></p>

## 🧩 구현 기능 상세

### 💬 댓글

**담당자** · [김세중](https://github.com/SB-14-KSJ)

- 댓글 CRUD — 본인만 수정 · 삭제
- 목록 — 시간순 정렬, 커서 페이지네이션
- 삭제 — 논리 삭제 기본, 물리 삭제 API 제공

<details>
<summary>📎 문서 · 서비스 상세 보기</summary>
<br>

| 📄 API 문서 (Swagger)                                                               | 🎞 서비스 상세 (GIF)                                                              |
|:----------------------------------------------------------------------------------|:-----------------------------------------------------------------------------|
| <img src="docs/readme/images/features/comments-api.png" width="400" alt="댓글 api"> | <img src="docs/readme/images/features/comments.gif" width="400" alt="댓글 시연"> |

</details>

### 📚 도서 관리

**담당자** · [김지현](https://github.com/pengji-dev)

- 등록 — **ISBN 검증** 후 카카오 책 API로 조회해 등록, 같은 ISBN 중복 등록 불가
- CRUD — 도서 조회 · 수정 · 삭제 (ISBN은 수정 불가)
- 이미지 — 표지 이미지를 **S3**에 저장
- 검색 · 정렬 — 키워드(제목 · 저자 · ISBN) 검색, 정렬 4종(제목 · 출판일 · 평점 · 리뷰 수), 커서 페이지네이션
- 삭제 — 논리 삭제 기본, 물리 삭제 시 리뷰 · 댓글 등 연관 데이터 모두 삭제

<details>
<summary>📎 문서 · 서비스 상세 보기</summary>
<br>

| 📄 API 문서 (Swagger)                                                            | 🎞 서비스 상세 (GIF)                                                           |
|:-------------------------------------------------------------------------------|:--------------------------------------------------------------------------|
| <img src="docs/readme/images/features/books-api.png" width="400" alt="도서 api"> | <img src="docs/readme/images/features/books.gif" width="400" alt="도서 시연"> |

</details>

### ✍️ 리뷰 · 좋아요

**담당자** · [박성욱](https://github.com/ziezz767)

- 리뷰 CRUD — 도서당 1개 작성, 본인만 수정 · 삭제
- 삭제 — 논리 삭제 기본, 물리 삭제 시 좋아요 · 댓글도 함께 삭제
- 검색 · 정렬 — 키워드(닉네임 · 내용 · 도서 제목), 작성자 · 도서 필터, 정렬(작성일 · 평점), 커서 페이지네이션
- 좋아요 — 좋아요 / 취소, 내가 좋아요 했는지 여부(`likedByMe`)

<details>
<summary>📎 문서 · 서비스 상세 보기</summary>
<br>

| 📄 API 문서 (Swagger)                                                              | 🎞 서비스 상세 (GIF)                                                             |
|:---------------------------------------------------------------------------------|:----------------------------------------------------------------------------|
| <img src="docs/readme/images/features/reviews-api.png" width="400" alt="리뷰 api"> | <img src="docs/readme/images/features/reviews.gif" width="400" alt="리뷰 시연"> |

</details>

### 👤 사용자

**담당자** · [강성준](https://github.com/xian980)

- 가입 · 로그인 — 회원가입(이메일 중복 불가), 로그인(**BCrypt** 비밀번호 암호화)
- 동시 가입 처리 — 이메일 · 닉네임 유니크 제약 위반을 **409**로 변환
- 조회 · 수정 — 사용자 조회, 닉네임만 수정
- 삭제 — 논리 삭제 후 **1일 뒤 물리 삭제** (자동 삭제가 있는 유일한 도메인), 연관 데이터(리뷰 · 댓글 · 좋아요 · 알림) 모두 삭제

<details>
<summary>📎 문서 · 서비스 상세 보기</summary>
<br>

| 📄 API 문서 (Swagger)                                                             | 🎞 서비스 상세 (GIF)                                                            |
|:--------------------------------------------------------------------------------|:---------------------------------------------------------------------------|
| <img src="docs/readme/images/features/users-api.png" width="400" alt="사용자 api"> | <img src="docs/readme/images/features/users.gif" width="400" alt="사용자 시연"> |

</details>

### 🔔 알림

**담당자** · [이은솔](https://github.com/EUNSOLY)

- 자동 생성 — 내 리뷰에 **좋아요 · 댓글**이 달렸을 때, 내 리뷰가 기간별 **인기 순위 10위** 안에 들었을 때
- 조회 · 읽음 — 목록 조회(최신순), 알림 하나 읽음, 전체 읽음
- 자동 정리 — 확인한 지 1주가 지난 알림을 매일 배치로 자동 삭제

<details>
<summary>📎 문서 · 서비스 상세 보기</summary>
<br>

| 📄 API 문서 (Swagger)                                                                    | 🎞 서비스 상세 (GIF)                                                                   |
|:---------------------------------------------------------------------------------------|:----------------------------------------------------------------------------------|
| <img src="docs/readme/images/features/notifications-api.png" width="400" alt="알림 api"> | <img src="docs/readme/images/features/notifications.gif" width="400" alt="알림 시연"> |

</details>

### 📊 대시보드 · OCR

**담당자** · [김양현](https://github.com/yyy2724)

- 집계 — 인기 도서 · 인기 리뷰 · 파워 유저를 **일간 · 주간 · 월간 · 역대** 기간별로 집계하고, 매일 배치로 스냅샷을 저장
- 순위 API — 인기 순위 조회 API 3개
- OCR — 책 표지 · 바코드 이미지에서 **ISBN 인식** (심화)

<details>
<summary>📎 문서 · 서비스 상세 보기</summary>
<br>

| 📄 API 문서 (Swagger)                                                                  | 🎞 서비스 상세 (GIF)                                                                 |
|:-------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------|
| <img src="docs/readme/images/features/dashboard-api.png" width="400" alt="대시보드 api"> | <img src="docs/readme/images/features/dashboard.gif" width="400" alt="대시보드 시연"> |

</details>

### 🏗 공통 · 인프라

**설계 · 개발 환경** — 담당자 [김양현](https://github.com/yyy2724)

- 헥사고날 패키지 설계, 중앙 예외 처리
- 프로젝트 초기 세팅 (로그 파이프라인 · Docker · 코드 스타일)
- 개발 인프라 구성: CI 빌드 · 테스트, `main` 브랜치 가드, 커밋 · PR 규칙, Claude 리뷰 워크플로

**AWS 인프라 · 운영** — 담당자 [이은솔](https://github.com/EUNSOLY)

- AWS 인프라 구성, CD 구축 (**무중단 자동 배포**)
- 로그 S3 정기 적재 🚀, MDC 로깅 · 요청자 식별
- 이미지 스토리지 (로컬 / S3), 환경별 프로필 · `.env` 분리
- 코드 품질: Checkstyle · Git Hook · JaCoCo · Codecov

## ⚙️ 핵심 기술

### 📅 기간별 랭킹 스냅샷 · Spring Batch

- 인기 도서 · 인기 리뷰 · 파워 유저를 **일간 · 주간 · 월간 · 역대**(`PeriodType`)로 집계해 **스냅샷 테이블**에 저장하고, 조회 API 는 집계 없이 최신 스냅샷만 읽습니다.
- `dailyRankingJob` 이 인기 도서 → 인기 리뷰 → 인기 리뷰 알림 → 파워 유저 Step 을 순서대로 실행하고, `BatchScheduler` 가 매일 KST 00:00 에 시작합니다.
- 집계 하나가 실패해도 나머지는 계속 수행하고(`runSafely`), 전용 스레드 풀로 여러 스케줄을 동시에 돌립니다.
- `(period, snapshot_date, rank)` 등 유니크 제약으로 같은 날짜 · 기간의 순위 중복을 막습니다.

| 랭킹    | 점수 산정                                    |
|-------|------------------------------------------|
| 인기 도서 | 리뷰 수 × 0.4 + 평점 × 0.6                    |
| 인기 리뷰 | 댓글 수 × 0.7 + 좋아요 수 × 0.3                 |
| 파워 유저 | 리뷰 점수 합 × 0.5 + 좋아요 수 × 0.2 + 댓글 수 × 0.3 |

### 🔍 OCR 기반 ISBN 인식

```
책 표지 · 바코드 이미지 ─▶ OCR.space ─▶ ISBN 추출 ─▶ 카카오 책 API ─▶ 도서 등록
```

- 업로드한 이미지에서 OCR 로 ISBN 을 인식하고, `IsbnValidator` 로 형식 · 체크섬을 검증한 뒤 도서를 등록합니다.
- 인식에 실패하면 전용 에러 코드(`BOOK_OCR_FAILED`)로 응답합니다.

### 🌐 외부 API 연동

- **OCR.space** 로 이미지에서 ISBN 을 인식하고, 인식한 ISBN 으로 **카카오 책 검색 API** 를 호출해 도서 정보를 조회합니다. (`RestClient`)
- 호출 코드는 `port.out` 인터페이스(`BookSearchPort`, `OcrClient`) 뒤에 두어, 서비스 로직은 외부 제공자를 모르고 교체해도 그대로 유지됩니다.
- 표지 썸네일은 내려받은 뒤 스토리지에 저장하고, 다운로드 시 리다이렉트는 따라가지 않도록 제한합니다.

### 🔐 비밀번호 암호화

- `spring-security-crypto` 의 **BCrypt** 로 비밀번호를 단방향 해시해 저장하고, 로그인 때는 해시를 비교합니다.
- `PasswordHasher` 포트와 `BCryptPasswordHasher` 어댑터로 분리해, 해시 방식을 바꿔도 사용자 로직은 영향을 받지 않습니다.

### 🧭 커서 기반 페이지네이션 (QueryDSL)

- 도서 · 댓글 · 알림 · 랭킹 목록을 **커서 방식**으로 제공해, 데이터가 쌓여도 OFFSET 비용이 늘지 않습니다.
- 키워드 검색 · 필터 · 정렬 같은 동적 조건은 `*RepositoryCustom` + QueryDSL 로 구현했습니다.
- 목록 조회마다 반복되는 "다음 페이지 판단 · 응답 변환" 코드를 공통 메서드 두 개로 모았습니다.

```
QueryDSL (limit + 1 조회) ─▶ SliceUtils.toSlice() ─▶ CursorPageResponse.of() ─▶ 응답
```

| 단계                        | 역할                                                                                                      |
|---------------------------|---------------------------------------------------------------------------------------------------------|
| QueryDSL 조회               | 한 페이지 크기보다 **1개 더**(`limit + 1`) 가져옵니다.                                                                 |
| `SliceUtils.toSlice()`    | 결과가 `limit` 보다 많으면 다음 페이지가 있다고 보고(`hasNext`), 확인용으로 더 가져온 1개를 잘라낸 뒤 `Slice` 로 변환합니다.                    |
| `CursorPageResponse.of()` | `Slice` 를 응답 형식(`content` · `nextCursor` · `nextAfter` · `size` · `totalElements` · `hasNext`)으로 변환합니다. |

- `CursorPageResponse.of()` 는 다음 페이지가 있을 때만 **마지막 항목**에서 `nextCursor` · `nextAfter` 를 꺼내고, 없으면 둘 다 `null` 입니다.
- 제네릭 `T` 는 어떤 필드가 있는지 알 수 없으므로, 커서 값을 꺼내는 방법을 람다(`cursorExtractor`, `afterExtractor`)로 넘겨받습니다. 덕분에 `NotificationDto`,
  `ReviewDto` 등 어떤 DTO 에도 같은 변환 메서드를 씁니다.

### ⚡ 동시성 제어

- 리뷰 좋아요 수와 도서의 리뷰 수 · 평점 집계를 **DB 에서 원자적으로 갱신**(`@Modifying` UPDATE 쿼리)해, 동시 요청에서도 값이 틀어지지 않도록 합니다.
- 사용자는 이메일 · 닉네임 유니크 제약을 두고, 동시 가입으로 제약이 깨지면 **409** 로 변환합니다.

### 🗑 논리 삭제 · 물리 삭제 · 연쇄 삭제

- 사용자 · 도서 · 리뷰 · 댓글은 `is_deleted` 로 **논리 삭제**를 기본으로 하고, 물리 삭제 API 를 별도로 제공합니다.
- 물리 삭제 시 `@OnDelete` 로 연관 데이터(리뷰 · 좋아요 · 댓글 · 알림 등)를 **DB 에서 연쇄 삭제**해 고아 데이터를 남기지 않습니다. (도서 등록자는 `SET NULL`)
- 사용자는 논리 삭제 후 1일이 지나면 스케줄러가 물리 삭제합니다.

### 🖼 이미지 스토리지 전환

- `BookImageStorage` 포트 하나에 `AwsS3Storage`(S3 Presigned URL) 와 `LocalStorage` 구현체를 두고, `STORAGE_TYPE=local | s3` 설정만으로
  전환합니다.
- 로컬 개발에서는 AWS 계정 없이도 이미지 업로드를 테스트할 수 있습니다.

### 🧯 통일된 예외 응답

- `ErrorCode` · `DeokhugamException` 을 기반으로 도메인마다 `*ErrorCode` · `*Exception` 을 두고, `GlobalExceptionController` 가 한 형식(
  `ErrorResponse`)으로 응답합니다.

### 📜 요청자 식별 · MDC 로깅 · ELK

- `Deokhugam-Request-User-ID` 헤더를 `MdcLoggingInterceptor` 가 읽어 `UserContextHolder` 에 담고, 컨트롤러에서는 `@RequestUserId` 로
  주입받습니다. (요청 종료 시 정리해 스레드 오염 방지)
- `requestedIp` · `requestedUserId` · `requestMethod` · `requestUrl` 을 MDC 에 담아 Logback → Logstash → Elasticsearch →
  Kibana 로 수집 · 조회합니다.
- `logback-spring.xml` 의 `LOG_PATTERN` 과 `logstash.conf` 의 grok 패턴은 **짝**입니다. 한쪽을 바꾸면 반드시 둘 다 수정하세요.

## 🗃 ERD

모든 테이블은 `BaseEntity` 의 `id(UUID)`, `created_at`, `updated_at` 을 공통으로 가집니다.

<p align="center"><img src="docs/readme/images/ERD1.png" alt="ERD" width="100%"/></p>

<details>
<summary>[실제 ERD 보기]</summary>
<br>

<p align="center"><img src="docs/readme/images/erd.png" alt="실제 ERD" width="90%"/></p>

</details>

- 사용자 · 도서 · 리뷰 · 댓글은 `is_deleted` 로 논리 삭제

## 📁 패키지 구조

백엔드는 **헥사고날 아키텍처(포트 & 어댑터)** 로 구성합니다. 도메인마다 `adapter` / `application.port` / `domain` 으로 나누고, 의존성은 항상 바깥(adapter)에서 안쪽(
domain)으로만 향합니다.
아래는 가장 구성이 큰 `book` 도메인을 펼친 예시이고, 나머지 도메인도 같은 구조입니다.

```
src
├── main
│   ├── java/com/sprint/deokhugam
│   │   ├── book                        # 📚 도서 (펼친 예시)
│   │   │   ├── adapter
│   │   │   │   ├── in
│   │   │   │   │   └── web             # BookController, BookApi(Swagger), dto, mapper
│   │   │   │   └── out
│   │   │   │       ├── kakao           # 카카오 책 검색 Adapter, config, dto
│   │   │   │       ├── external        # OcrSpaceClient (OCR.space)
│   │   │   │       └── storage         # AwsS3Storage, LocalStorage
│   │   │   ├── application
│   │   │   │   ├── dto                 # BookSearchResult, StoredThumbnail 등
│   │   │   │   └── port
│   │   │   │       ├── in              # BookUseCase · BookService, OcrUseCase · OcrService
│   │   │   │       └── out             # BookRepositoryPort, RepositoryCustom(QueryDSL), BookSearchPort, BookImageStorage, OcrClient
│   │   │   └── domain                  # Book
│   │   │       ├── exception           # BookException, BookErrorCode
│   │   │       └── validator           # IsbnValidator
│   │   ├── review                      # ✍️ 리뷰 · 좋아요
│   │   ├── comment                     # 💬 댓글
│   │   ├── user                        # 👤 사용자 (adapter.in.scheduler, adapter.out.security: BCrypt)
│   │   ├── notification                # 🔔 알림 (adapter.in.batch · scheduler)
│   │   ├── dashboard                   # 📊 인기 도서 · 인기 리뷰 · 파워 유저 (adapter.in.batch · scheduler)
│   │   └── common                      # 공통 (config, context, domain, exception, logging, pagination, util)
│   └── resources
│       ├── application.yml             # 공통 설정
│       ├── application-local.yml       # 로컬 환경 (Docker PostgreSQL, 로컬 스토리지)
│       ├── application-prod.yml        # 운영 환경 (AWS RDS, S3)
│       ├── logback-spring.xml          # 로그 패턴 (logstash.conf 와 짝)
│       └── static                      # 웹 화면 빌드 결과물
└── test/java/com/sprint/deokhugam      # 도메인별 단위 · 슬라이스 테스트 (main 과 같은 패키지 구조)
```

<p align="center"><img src="docs/readme/images/package-structure2.png" alt="패키지 구조" width="100%"/></p>

## ☁️ 인프라 구조

#### 🚀 배포 흐름 (CI/CD)

```mermaid
%%{init: {"theme":"base","flowchart":{"curve":"basis","nodeSpacing":28,"rankSpacing":48,"padding":14,"wrappingWidth":400},"themeVariables":{"fontFamily":"-apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif","fontSize":"15px","lineColor":"#94A3B8","textColor":"#334155","edgeLabelBackground":"#FFFFFF"}}}%%
flowchart TB
    gh["🐙 GitHub<br/>main 머지"]
    gha["⚙️ GitHub Actions<br/>빌드 · 배포"]
    ecr["📦 Amazon ECR<br/>Docker 이미지"]
    app["🌱 Amazon ECS Fargate<br/>덕후감 앱"]
    rds[("🐘 RDS<br/>PostgreSQL")]
    s3[("🪣 Amazon S3<br/>도서 이미지 · 로그")]
    elk["📊 ELK<br/>Logstash · Elasticsearch · Kibana"]
    gh --> gha -- " OIDC로 AWS 인증 " --> ecr -- " 배포 " --> app
    app --> rds
    app --> s3
    app -.-> elk
    classDef app fill: #ECFDF5, stroke: #10B981, stroke-width: 2.5px, color: #064E3B
    classDef db fill: #EFF6FF, stroke: #3B82F6, stroke-width: 2px, color: #1E3A8A
    classDef ops fill: #F8FAFC, stroke: #64748B, stroke-width: 1.5px, color: #1E293B
    classDef mon fill: #FFF7ED, stroke: #F97316, stroke-width: 2px, color: #7C2D12
    classDef store fill: #F0FDFA, stroke: #14B8A6, stroke-width: 2px, color: #134E4A
    class gh ops
    class gha ops
    class ecr ops
    class app app
    class rds db
    class s3 store
    class elk mon
```

#### 🌐 서비스 흐름 (사용자 요청)

```mermaid
%%{init: {"theme":"base","flowchart":{"curve":"basis","nodeSpacing":28,"rankSpacing":48,"padding":14,"wrappingWidth":400},"themeVariables":{"fontFamily":"-apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif","fontSize":"15px","lineColor":"#94A3B8","textColor":"#334155","edgeLabelBackground":"#FFFFFF"}}}%%
flowchart TB
    user(["👥 사용자"])
    alb["⚖️ ALB<br/>로드 밸런서"]
    app["🌱 Amazon ECS Fargate<br/>덕후감 앱"]
    rds[("🐘 RDS<br/>PostgreSQL")]
    s3[("🪣 Amazon S3<br/>도서 이미지")]
    ext["🔌 외부 API<br/>카카오 책 검색 · OCR.space"]
    user -- " HTTP 80 " --> alb --> app
    app --> rds
    app --> s3
    app -. " 도서 · OCR 요청 " .-> ext
    classDef client fill: #F8FAFC, stroke: #94A3B8, stroke-width: 1.5px, color: #0F172A
    classDef net fill: #F5F3FF, stroke: #8B5CF6, stroke-width: 2px, color: #4C1D95
    classDef app fill: #ECFDF5, stroke: #10B981, stroke-width: 2.5px, color: #064E3B
    classDef db fill: #EFF6FF, stroke: #3B82F6, stroke-width: 2px, color: #1E3A8A
    classDef store fill: #F0FDFA, stroke: #14B8A6, stroke-width: 2px, color: #134E4A
    classDef ops fill: #F8FAFC, stroke: #64748B, stroke-width: 1.5px, color: #1E293B
    class user client
    class alb net
    class app app
    class rds db
    class s3 store
    class ext ops
```

<details>
<summary>[자세히 보기]</summary>
<br>

<p align="center"><img src="docs/readme/images/deploy-architecture.png" alt="배포 구조 상세" width="90%"/></p>

</details>

## 🚀 시작하기

### 1. 준비 사항

- JDK 17
- Docker Desktop (PostgreSQL 이 compose 로 자동 기동되며, 테스트에도 필요)

### 2. 저장소 받기

```bash
git clone <저장소 URL>
cd sb14-deokhugam-team02
```

### 3. 환경변수 설정

[.env.example](.env.example) 을 `.env.local` 로 복사한 뒤 값을 채웁니다. 카카오·OCR.space 키가 없으면 도서 등록과 OCR 기능을 사용할 수 없습니다.

```bash
cp .env.example .env.local
```

| 환경 | 파일           | 비고                                    |
|----|--------------|---------------------------------------|
| 로컬 | `.env.local` | 위 예시대로 직접 작성 (기본 프로필)                 |
| 운영 | `.env.prod`  | 🔒 **비공개** (저장소에 포함하지 않으며 팀 내부에서만 관리) |

`.env.local` · `.env.prod` 는 커밋하지 않습니다.

```properties
POSTGRES_DB=deokhugam
POSTGRES_USER=deokhugam
POSTGRES_PASSWORD=change-me
# 도서 정보 조회 (카카오 책 검색 API)
KAKAO_REST_API_KEY=
# 이미지 저장소 (local | s3)
STORAGE_TYPE=local
STORAGE_PATH=./uploads
# AWS S3 (STORAGE_TYPE=s3 일 때만 필요)
AWS_S3_ACCESS_KEY=
AWS_S3_SECRET_KEY=
AWS_S3_REGION=
AWS_S3_BUCKET=
AWS_S3_PRESIGNED_URL_EXPIRATION=600
# OCR.space (ISBN 이미지 인식)
OCR_SPACE_API_KEY=
```

| 변수                                                                      | 설명                                    |
|-------------------------------------------------------------------------|---------------------------------------|
| `POSTGRES_DB` `POSTGRES_USER` `POSTGRES_PASSWORD`                       | DB 접속 정보                              |
| `KAKAO_REST_API_KEY`                                                    | 카카오 책 검색 API 키 (도서 등록에 필요)            |
| `STORAGE_TYPE`                                                          | 이미지 저장소 `local` \| `s3` (기본값 `local`) |
| `STORAGE_PATH`                                                          | 로컬 이미지 저장 경로 (기본값 `./uploads`)        |
| `AWS_S3_ACCESS_KEY` `AWS_S3_SECRET_KEY` `AWS_S3_REGION` `AWS_S3_BUCKET` | S3 설정 (`STORAGE_TYPE=s3` 일 때 필요)      |
| `AWS_S3_PRESIGNED_URL_EXPIRATION`                                       | Presigned URL 만료(초)                   |
| `OCR_SPACE_API_KEY`                                                     | OCR.space API 키 (ISBN 이미지 인식에 필요)     |

### 4. 실행

```bash
./gradlew bootRun
```

활성 프로필을 따로 지정하지 않으면 `local` 이 기본값이라 위 명령만으로 로컬 환경(`.env.local`)으로 실행되고, PostgreSQL 은 Docker compose 로 자동 기동됩니다.

| 접속 대상      | 주소                                                                                         |
|------------|--------------------------------------------------------------------------------------------|
| 웹 화면       | [http://localhost:8080/](http://localhost:8080/)                                           |
| Swagger UI | [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html) |

앱과 ELK 를 함께 띄우려면 다음 명령을 사용합니다. compose 는 기본으로 `.env` 만 읽으므로 `--env-file` 을 직접 지정해야 합니다.

```bash
docker compose -f docker-compose-logging-pipeline.yml --env-file .env.local --profile full up -d --build
```

### 5. 빌드와 검사

```bash
# 테스트 · Checkstyle 을 포함한 빌드 (Docker 필요)
./gradlew build

# 테스트
./gradlew test

# 코드 스타일 검사
./gradlew checkstyleMain checkstyleTest
```

## 📏 팀 개발 규칙

### 🎨 코드 스타일

에디터 → 커밋 → PR 세 단계에서 자동으로 맞추고 검사합니다.

| 단계  | 도구                     | 자동화 내용                                                                                            |
|-----|------------------------|---------------------------------------------------------------------------------------------------|
| 에디터 | `.editorconfig`        | UTF-8, LF, 파일 끝 개행, Java·Gradle 4칸 / yml 2칸 들여쓰기를 에디터가 자동 적용                                      |
| 에디터 | IntelliJ 포매터           | `rule-config/intellij-formatter.xml` 을 가져와 통일 ([가이드](docs/code-style/INTELLIJ_SETUP_GUIDE.md))    |
| 빌드  | Checkstyle 12.3.0      | [네이버 핵데이 Java 컨벤션](https://naver.github.io/hackday-conventions-java) 기반, 경고 0개(`maxWarnings = 0`) |
| 커밋  | `.githooks/pre-commit` | 커밋 직전에 Checkstyle 을 실행하고, 위반이 있으면 **커밋을 차단**                                                      |

- Gradle 동기화 시 `build.gradle` 이 git 훅 경로를 `.githooks` 로 자동 설정(`core.hooksPath`)하므로 팀원이 별도로 설치할 명령은 없습니다.
- 위반 리포트는 `build/reports/checkstyle/` 에 생성됩니다.

> 실시간 코드 스타일 체크를 위해 가이드에 맞춰 에디터 설정 진행 (필수는 아님)

### 💬 커밋 메시지

```
<타입>: <설명>
```

| 타입         | 용도      | 예시                       |
|------------|---------|--------------------------|
| `feat`     | 신규 기능   | `feat: 도서 목록 조회 기능 추가`   |
| `fix`      | 버그 수정   | `fix: 리뷰 중복 작성 검증 오류 수정` |
| `refactor` | 리팩토링    | `refactor: 랭킹 집계 로직 분리`  |
| `test`     | 테스트     | `test: 댓글 목록 조회 테스트 추가`  |
| `docs`     | 문서      | `docs: README 작성`        |
| `chore`    | 환경설정·빌드 | `chore: 의존성 정리`          |

| 단계 | 도구                     | 자동화 내용                                                        |
|----|------------------------|---------------------------------------------------------------|
| 커밋 | `.githooks/commit-msg` | 커밋 메시지 첫 줄이 `타입: 설명` 형식이 아니면 **커밋을 차단** (Merge · Revert 는 통과) |
| PR | `pr-convention.yml`    | `commit-lint` 가 PR 에 포함된 **모든 커밋** 메시지를 다시 검사                 |

### 🌿 브랜치 전략

```
작업 브랜치 ──PR──▶ develop ──PR──▶ main
```

| 브랜치       | 역할                                |
|-----------|-----------------------------------|
| `main`    | 운영 배포 브랜치. `develop` 에서 온 PR 만 허용 |
| `develop` | 개발 통합 브랜치. 작업 브랜치를 PR 로 합침        |
| 작업 브랜치    | 기능 · 수정 단위로 생성하고 `develop` 으로 PR  |

- 작업 브랜치 이름은 `타입/이슈번호-설명` 으로 짓습니다. (예: `feat/12-review-api`)
- 이슈 템플릿 6종(feat/fix/refactor/test/docs/chore)과 PR 템플릿을 사용합니다.
- 팀원 리뷰 후 2명 이상 승인 완료 시 머지가 가능합니다. Claude 자동 리뷰는 참고용이며 머지 필수 조건은 아닙니다.

| 단계 | 자동화 내용                                                                   |
|----|--------------------------------------------------------------------------|
| PR | 브랜치 이름에서 이슈 번호를 찾아 PR 본문에 `Closes #번호` 를 **자동 연결** (`pr-convention.yml`) |
| PR | `main` 대상 PR 의 출발 브랜치가 `develop` 이 아니면 **머지 차단** (`pr-target-guard.yml`) |

### 🔄 CI/CD

PR 이 열리면 자동으로 검사하고, `main` 에 머지되면 AWS 로 자동 배포합니다.

```mermaid
flowchart LR
    A[작업 브랜치 push] --> B[PR → develop]
B --> C["자동 검사<br/>ci · commit-lint · issue-link"]
B --> R["Claude 자동 리뷰<br/>(참고용)"]
C --> D[팀원 리뷰 · 머지]
R --> D
D --> E["develop<br/>머지 후 ci 재검증"]
E --> F[PR → main]
F --> G["자동 검사<br/>ci · main-only-from-develop"]
G --> H[main 머지]
H --> I["CD (deploy.yml)<br/>ECR push → ECS 배포"]
```

| 구분 | 워크플로우                 | 트리거                                    | 자동화 내용                                                        |
|----|-----------------------|----------------------------------------|---------------------------------------------------------------|
| CI | `ci.yml`              | PR(`develop`, `main`), push(`develop`) | Gradle 빌드 · 테스트 · Checkstyle, JaCoCo 커버리지 PR 코멘트, Codecov 업로드 |
| CI | `pr-convention.yml`   | PR 생성 · 수정                             | 커밋 메시지 검사(`commit-lint`), 이슈 연결(`issue-link`)                 |
| CI | `pr-target-guard.yml` | PR(`main`)                             | 출발 브랜치가 `develop` 인지 검사                                       |
| CI | `claude-review.yml`   | PR 오픈(`develop`)                       | Claude 자동 코드 리뷰 (한국어, 참고용)                                    |
| CI | `claude.yml`          | PR 코멘트 `@claude`                       | 재리뷰 · 요청 처리                                                   |
| CD | `deploy.yml`          | push(`main`)                           | OIDC 인증 → 이미지 빌드 · ECR push → ECS 서비스 배포                      |

## 🔗 관련 문서

- [환경변수 예시](.env.example)
- [애플리케이션 설정](src/main/resources/application.yml)
- [코드 스타일 가이드](docs/code-style)
- [로깅 인터셉터 가이드](docs/logging/LOGGING_INTERCEPTOR_GUIDE.md)
- [JaCoCo 가이드](docs/test/JECOCO_GUIDE.md)
- [이슈 템플릿](.github/ISSUE_TEMPLATE)
- [PR 템플릿](.github/PULL_REQUEST_TEMPLATE.md)
