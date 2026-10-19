# 🧪 테스트 커버리지 가이드 (JaCoCo)

## 1. 로컬에서 커버리지 확인

```bash
# 테스트 실행 + 리포트 생성
./gradlew build

# HTML 리포트 열기
open build/jacocoHtml/index.html

# (선택) 프로젝트 전체 커버리지 80% 검증
./gradlew jacocoTestCoverageVerification
```

| 파일          | 위치                                               | 용도                              |
|-------------|--------------------------------------------------|---------------------------------|
| `test.exec` | `build/jacoco/`                                  | 테스트 실행 중 기록된 원본 데이터 (직접 열 일 없음) |
| HTML 리포트    | `build/jacocoHtml/index.html`                    | 브라우저로 확인                        |
| XML 리포트     | `build/reports/jacoco/test/jacocoTestReport.xml` | CI(PR 코멘트)에서 사용                 |

**리포트 보는 법**

- 🟢 초록: 테스트가 실행한 줄
- 🔴 빨강: 테스트가 실행하지 않은 줄
- 🟡 노랑: 분기(if 등) 중 일부만 실행된 줄

---

## 2. 커버리지 측정 제외 대상

테스트할 로직이 없거나 자동 생성되는 코드는 측정에서 제외합니다.

| 패턴                                          | 대상                  |
|---------------------------------------------|---------------------|
| `**/*Application*`                          | Spring Boot 실행 클래스  |
| `**/*Exception*`, `**/*ErrorCode*`          | 예외 / 에러코드           |
| `**/*Request*`, `**/*Response*`, `**/*Dto*` | 요청·응답 DTO           |
| `**/common/**`                              | 공통 설정               |
| `**/domain/Q*.class`                        | QueryDSL 자동 생성 Q클래스 |
| `**/*MapperImpl*`                           | MapStruct 자동 생성 구현체 |

> ⚠️ QueryDSL로 **직접 작성한 쿼리(RepositoryImpl 등)** 와 **엔티티의 도메인 로직**은 테스트 대상입니다.

---

## 3. PR 커버리지 체크 (GitHub Actions)

PR을 올리면 CI가 테스트를 실행하고, **이번 PR에서 변경된 파일의 커버리지**를 PR 코멘트로 남깁니다.

```
./gradlew build          → 테스트 실행 + XML 리포트 생성
madrapps/jacoco-report   → PR 변경 파일 커버리지 계산 + 코멘트 작성 (기준 80%)
80% 검사 step            → 80% 미만이면 CI 실패 (현재 주석 처리)
```

- 현재는 **코멘트로 표시만** 하고, 80% 미만이어도 머지를 막지 않습니다.
- 강제하려면 `ci.yml`의 `변경 파일 커버리지 80% 검사` step 주석을 제거합니다.

---

## 4. 왜 build.gradle(Gradle 태스크)이 아니라 CI로 체크하나요?

|          | Gradle 태스크 (`jacocoTestCoverageVerification`) | CI (`madrapps/jacoco-report`) |
|----------|-----------------------------------------------|-------------------------------|
| 검사 기준    | **프로젝트 전체**만 가능                               | **PR 변경 파일** / 전체 모두 가능       |
| 결과 확인    | CI 로그 (Actions 탭에서 찾아야 함)                     | **PR 코멘트**에 바로 표시             |
| 파일별 커버리지 | ❌                                             | ✅                             |

**이유**

1. **Gradle(JaCoCo)은 Git/PR 정보를 모릅니다.**
   빌드된 클래스 전체를 대상으로 계산하기 때문에, "이번 PR에서 바뀐 파일"만 골라 검사할 수 없습니다.
2. **전체 기준으로 막으면 내 작업과 무관하게 PR이 실패합니다.**
   develop에 테스트가 부족한 코드가 이미 있으면, 내 코드를 100% 테스트해도 전체가 80% 미만이라 실패합니다.
3. **CI는 PR 정보를 알고 있습니다.**
   Action이 GitHub API로 PR 변경 파일 목록을 조회해, 해당 파일의 커버리지만 계산합니다.
4. **리뷰어가 PR 화면에서 바로 확인할 수 있습니다.**

> Gradle 검증 태스크는 `build`에 연결하지 않고, 로컬에서 전체 커버리지를 확인하는 용도로 남겨 둡니다.

### (참고) CI 없이 Gradle 태스크만으로 검증하는 방법

`build.gradle`에 아래 코드를 추가하면 `./gradlew build`(= CI의 빌드 step) 실행 시 **프로젝트 전체 커버리지 80%** 검증이 함께 실행됩니다.

```groovy
// build(check) 실행 시 커버리지 검증도 함께 실행
tasks.named('check') {
    dependsOn jacocoTestCoverageVerification
}
```

- 80% 미만이면 **빌드 실패 → PR CI 실패**
- 단, 기준이 **프로젝트 전체**이므로 기존 코드의 커버리지가 낮으면 내 작업과 무관하게 실패합니다.
  빌드는 실패시키지 않고 **경고만** 출력하려면 `failOnViolation = false`를 추가합니다. (결과는 CI 로그에서만 확인 가능)

```groovy
jacocoTestCoverageVerification {
    violationRules {
        failOnViolation = false   // 미달이어도 빌드 실패 대신 경고만 출력
        rule { /*...*/ }
    }
}
```

---

## 5. 참고

- `GITHUB_TOKEN`은 Actions가 자동 발급하는 토큰이라 별도 등록이 필요 없습니다.
- PR 코멘트 작성을 위해 워크플로우에 `pull-requests: write` 권한이 설정되어 있습니다.
- fork 레포에서 올린 PR은 권한 제한으로 코멘트가 작성되지 않습니다. 같은 레포에 브랜치를 만들어 PR을 올려 주세요.


- [제코코 그레들 플러그인 사용법](https://docs.gradle.org/9.7.1/userguide/jacoco_plugin.html)
- [테스트커버리지 배찌 공홈](https://app.codecov.io/)
