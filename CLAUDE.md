# 덕후감 백엔드

## 개요
- 도서 이미지 OCR 기반 ISBN 매칭 도서 리뷰 서비스 (코드잇 스프린트 중급 프로젝트 2팀)
- 프론트는 코드잇 제공 빌드 산출물: `src/main/resources/static/` (수정·리뷰 대상 아님)

## 스택
- Java 17, Spring Boot 4.1.1, Gradle 9.7.1(wrapper), Spring Data JPA, PostgreSQL 15, Lombok
- 패키지 루트: `com.sprint.deokhugam`

## 로컬 실행
1. `.env.example`을 `.env`로 복사하고 값 채우기 (`.env`는 커밋 금지)
2. Docker Desktop 실행
3. IntelliJ 실행 또는 `./gradlew bootRun` → postgres가 자동 기동됨
- 전체(앱+ELK): `docker compose -f docker-compose-logging-pipeline.yml --profile full up -d --build`

## 빌드 / 테스트
- `./gradlew build` — 테스트에도 Docker가 필요 (테스트 중 compose로 postgres 기동)

## 패키지 / 레이어 규칙
- TODO(팀 합의)

## 코드 스타일
- `.editorconfig` 준수 (UTF-8, LF, Java 4칸, yml 2칸)
- `open-in-view: false` → 지연 로딩은 서비스 계층 트랜잭션 안에서 처리

## 브랜치 / 커밋 / PR
- 작업 브랜치 → `develop` → `main` (main은 develop에서만 PR)
- 커밋: `feat:`, `fix:`, `refactor:`, `docs:`, `chore:` + 설명 (세부 TODO)

## 비밀값
- 비밀번호·키는 `.env`로만. 코드·yml에 하드코딩 금지, 기본값도 두지 않음

## 로깅
- `logback-spring.xml`의 LOG_PATTERN과 `logstash.conf`의 grok 패턴은 짝. 한쪽을 바꾸면 반드시 둘 다 수정
- MDC 키: `requestedIp`, `requestedUserId`

## AI 도구
- PR이 열리면 Claude가 자동 리뷰(참고용, 머지 필수 조건 아님). 재리뷰는 PR 코멘트에 `@claude`
- Claude의 모든 코멘트는 한국어로 작성~~~~~~~~~~~~~~~~~~~~
