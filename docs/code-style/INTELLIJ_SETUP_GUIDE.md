# IntelliJ 코드 스타일 설정 가이드

이 프로젝트의 코드 컨벤션을 IntelliJ에 적용하는 방법을 정리한 문서.
기본 규칙은 [네이버 캠퍼스 핵데이 Java 코딩 컨벤션](https://naver.github.io/hackday-conventions-java/#_intellij)을 따르며, 일부 설정은 프로젝트에 맞게
변경했다 ([공식 가이드와 다른 부분](#3-공식-가이드와-다른-부분-주의) 참고).

## 관련 파일

| 파일                                        | 역할                                 |
|-------------------------------------------|------------------------------------|
| `rule-config/intellij-formatter.xml`      | IntelliJ 포매터 설정 (Reformat Code 기준) |
| `rule-config/checkstyle-rules.xml`        | Checkstyle 검사 규칙                   |
| `rule-config/checkstyle-suppressions.xml` | Checkstyle 검사 예외 대상 (특정 파일·규칙 제외)  |
| `.editorconfig`                           | 들여쓰기 방식, 파일 끝 개행 등 에디터 공통 설정       |

---

## 1. 필수 설정

### 1-1. Code Style Scheme 가져오기

`Reformat Code` 실행 시 프로젝트 컨벤션대로 코드가 정렬되도록 설정한다.

1. `Settings` > `Editor` > `Code Style` > `Java`로 이동
2. `Scheme` 옆 `⋮` > `Import Scheme` > `IntelliJ IDEA code style XML` 선택
3. `rule-config/intellij-formatter.xml` 선택 후 가져오기
4. `Scheme`에 가져온 스킴이 선택되어 있는지 확인하고 `OK`

> `Reformat Code`는 설정 화면이 아니라 에디터에서 실행한다.
> - 현재 파일: `Code` > `Reformat Code` (macOS `⌥⌘L` / Windows `Ctrl+Alt+L`)
> - 폴더 전체: 프로젝트 트리에서 폴더 우클릭 > `Reformat Code`

### 1-2. Actions on Save

파일 저장 시 정렬과 import 정리가 자동으로 실행되도록 설정한다.

1. `Settings` > `Tools` > `Actions on Save`로 이동
2. `Reformat code`, `Optimize imports` 체크
3. `OK`

---

## 2. 선택 설정: CheckStyle-IDEA 플러그인

빌드 전에도 에디터에서 규칙 위반 위치에 바로 밑줄이 표시되도록 설정한다.

### 2-1. 플러그인 설치

`Settings` > `Plugins` > `Marketplace`에서 **CheckStyle-IDEA** 설치

### 2-2. 플러그인 설정

1. `Settings` > `Tools` > `Checkstyle`로 이동
2. `Configuration File` 목록에서 `+` 클릭 후 아래와 같이 입력
    - Description: 자유롭게 입력 (예: `deokhugam`)
    - `Use a local Checkstyle file` 선택
    - File: `rule-config/checkstyle-rules.xml`
    - `Store relative to project location` 체크 권장
   > `.idea`는 git에 올리지 않으므로 이 설정은 각자 PC에만 저장된다. 절대경로로 저장해도 팀원에게 영향은 없지만,
   > 체크해두면 프로젝트 폴더를 옮기거나 다시 clone해도 경로가 유지된다.
3. `NEXT` 클릭 후 속성 입력 화면에서 `suppressionFile`의 Value에 `rule-config/checkstyle-suppressions.xml`을 입력하고 `NEXT` > `FINISH`
4. 목록에 추가된 설정의 **Active 체크박스를 켜고** `Apply` > `OK`

---

## 3. 공식 가이드와 다른 부분 (주의)

- 공식 가이드는 **탭** 들여쓰기 기준이지만, 이 프로젝트는 `.editorconfig`(`indent_style = space`)에 맞춰 **스페이스**로 바꿨다.
  `rule-config/intellij-formatter.xml`에 이미 반영돼 있으니 import만 하면 되고, 가이드에 나오는 탭/스페이스 수동 설정은 따로 할 필요 없다.
- 가이드의 "줄바꿈 후 추가 들여쓰기 단계 조정"(Continuation indent를 8로 바꾸는 팁)은 **적용하지 말 것**. `rule-config/checkstyle-rules.xml`의
  `Indentation` 모듈이 `lineWrappingIndentation=4`로 고정돼 있어서, 8로 바꾸면 IntelliJ 자동 포맷과 Checkstyle 검사 결과가 서로 어긋난다.
- 가이드의 "파일 끝에 개행 없으면 추가" 설정도 필요 없다. `.editorconfig`의 `insert_final_newline = true`를 IntelliJ가 이미 자동으로 지켜준다.

---

## 4. 최종 검사

PR을 올리면 GitHub Actions(`.github/workflows/<파일명>.yml`)에서 Checkstyle 검사가 자동으로 실행되며, 규칙 위반이 있으면 검사가 실패한다.

PR 올리기 전에 로컬에서 미리 확인하려면 아래 명령어를 실행한다.

```bash
./gradlew checkstyleMain checkstyleTest
```

- 위반이 있으면 빌드가 실패한다 (`maxWarnings = 0`).
- 상세 결과: 검사 실패 시 터미널에 출력되는 `See the report at: ...` 링크를 클릭해서 확인
