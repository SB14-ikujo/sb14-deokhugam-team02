# Gradle 코드 스타일 검사 연동

## Checkstyle 연동

[규칙별 설명 참고](CHECK_STYLE.md) <br/>
[naver-checkstyle-rules](https://github.com/naver/hackday-conventions-java) 기반 규칙으로 커스텀, Gradle 내장 `checkstyle` 플러그인으로
연동 <br/>
[Checkstyle 버전 확인 (Maven Central)](https://central.sonatype.com/artifact/com.puppycrawl.tools/checkstyle)

```groovy
plugins {
    id 'checkstyle'
}

checkstyle {
    maxWarnings = 0 // 규칙이 어긋나는 코드가 하나라도 있을 경우 빌드 fail을 내고 싶다면 이 선언을 추가한다.
    configFile = file("${rootDir}/rule-config/checkstyle-rules.xml")
    configProperties = ["suppressionFile": "${rootDir}/rule-config/checkstyle-suppressions.xml"]
    toolVersion = "12.3.0"  // checkstyle 버전
}
```

- `configFile` / `suppressionFile`: 규칙·예외 파일은 `rule-config/`에 있다 (`rule-config/checkstyle-rules.xml`,
  `rule-config/checkstyle-suppressions.xml`).
- `maxWarnings = 0`: 규칙 파일 내부 `severity`는 `warning`으로 선언돼 있지만, 이 설정 때문에 경고가 하나라도 있으면 빌드가 fail한다 (사실상 전부 강제 규칙).
- `checkstyle` 플러그인은 `org.ec4j.editorconfig`와 달리 `checkstyleMain`/`checkstyleTest`를 **자동으로 `check`에 연결**해준다. 그래서
  `editorconfigCheck`처럼 `check.dependsOn`을 따로 선언하지 않아도 `./gradlew build`/`check` 시 같이 실행된다.
- 단, `editorconfigCheck`와 `checkstyleMain`/`checkstyleTest` 사이에는 실행 순서를 강제하는 의존관계가 없다. `build.gradle`의 "
  editorconfigCheck 후 checkstyle 진행"이라는 주석은 의도일 뿐, 실제로는 Gradle이 둘을 병렬/임의 순서로 돌릴 수 있다. (`check`를 통과하려면 어차피 둘 다 성공해야 하므로
  결과에는 영향 없음)

### IntelliJ 코드 스타일 가져오기

위와 같은 스타일은 build(실행)를 했을 때 확인할 수 있지만, 개발 단계에서 실시간으로 확인하려면 IntelliJ 설정이 필요하다.<br/>
Formatter import부터 CheckStyle-IDEA 플러그인까지 —
**적용 방법: [INTELLIJ_SETUP_GUIDE.md](INTELLIJ_SETUP_GUIDE.md)**

### Checkstyle 태스크 사용법

- `./gradlew checkstyleMain`: 운영 코드(`src/main`)만 검사.
- `./gradlew checkstyleTest`: 테스트 코드(`src/test`)만 검사.
- `./gradlew build`: `editorconfigCheck` + `checkstyleMain`/`checkstyleTest` 포함 전체 빌드.

## 코드 스타일 검사 흐름 정리

코드 스타일은 로컬 두 시점 + 원격 한 시점, 총 세 군데에서 걸러진다.

| 시점 | 트리거 | 실행 내용 | 실패하면 |
|---|---|---|---|
| 1. 개발 중 (로컬) | IntelliJ에서 코드 작성 | Formatter import + (선택) CheckStyle-IDEA 플러그인이 실시간으로 표시 | 에디터에 밑줄/경고만, 강제 아님 |
| 2. `git commit` (로컬) | `.githooks/pre-commit` | `./gradlew checkstyleMain checkstyleTest` | **커밋 자체가 막힘** (`exit 1`) |
| 3. `git push` → PR (원격) | GitHub Actions `ci.yml`의 `build` 잡 | `./gradlew build` (→ `editorconfigCheck` + `checkstyleMain`/`checkstyleTest` 포함) | CI 실패, PR의 `build` 필수 체크 실패로 머지 불가 |

```
commit  ──▶ pre-commit 훅 ──▶ checkstyleMain/Test 통과해야 커밋 생성
   │                                   │
   ▼                                   ▼
push/PR ──▶ GitHub Actions(ci.yml) ──▶ ./gradlew build 통과해야 머지 가능
```

- 1번(IntelliJ)은 참고용이라 어겨도 커밋/푸시가 가능하다. 실제 강제력은 2번(로컬 커밋 차단)과 3번(원격 머지 차단) 두 군데다.
- 2번을 우회하고 싶으면(비상시) `git commit --no-verify`로 훅을 건너뛸 수 있지만, 그래도 3번(CI)에서 다시 걸린다 — 결국 PR을 머지하려면 반드시 통과해야 한다.
- `.githooks/pre-commit`은 `core.hooksPath`가 `.githooks`로 설정돼 있어야 동작한다. `build.gradle`이 Gradle 실행 시 자동으로 이 설정을 해주지만(59~62번째 줄), 저장소를 새로 클론하고 `./gradlew`를 한 번도 안 돌린 상태에서 바로 커밋하면 첫 커밋에는 훅이 안 걸릴 수 있다.
- `.githooks/commit-msg`는 커밋 **메시지 형식**(`feat:`/`fix:`/... 규칙)을 검사하는 별도 훅이라 위 표의 코드 스타일 검사와는 무관하다.
