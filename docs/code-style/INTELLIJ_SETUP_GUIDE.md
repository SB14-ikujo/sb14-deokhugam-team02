# IntelliJ 실시간 코드 스타일 적용 가이드

메뉴 클릭 경로 등 상세 절차는 공식 가이드 참고: https://naver.github.io/hackday-conventions-java/#_intellij

아래는 이 프로젝트에서 어떤것을 설정해야하는지에 대해 간단 설명

## 필수

- **Formatter import**: `rule-config/intellij-formatter.xml`을 IntelliJ Code Style Scheme으로 가져오기. `Reformat Code`를 눌렀을 때
  이 프로젝트 컨벤션대로 정렬되게 해준다.
- **Actions on Save** : Tools > Actions on Save > Reformat code, Optimize Imports 적용

## 선택

- **CheckStyle-IDEA 플러그인**: `rule-config/checkstyle-rules.xml`(+ `rule-config/checkstyle-suppressions.xml`)을 등록해서, 빌드
  전에도 에디터에서 바로 규칙 위반에 밑줄이 뜨게 해준다. Checkstyle 버전은 `build.gradle`의 `checkstyle.toolVersion`과 동일하게 맞출 것 ([버전 확인](https://central.sonatype.com/artifact/com.puppycrawl.tools/checkstyle)).
    - 설정 파일 등록 시 **Store relative to project location**을 체크한다 (절대경로로 저장되면 팀원 PC에서 경로가 깨짐).
    - NEXT 화면에서 `suppressionFile` 값을 물어보면 `rule-config/checkstyle-suppressions.xml`을 입력한다.
    - 등록 후 목록에서 추가한 설정의 체크박스를 켜야 적용된다.

## 공식 가이드와 다른 부분 (주의)

- 공식 가이드는 **탭** 들여쓰기 기준이지만, 이 프로젝트는 `.editorconfig`(`indent_style = space`)에 맞춰 **스페이스**로 바꿨다.
  `rule-config/intellij-formatter.xml`에 이미 반영돼 있으니 import만 하면 되고, 가이드에 나오는 탭/스페이스 수동 설정은 따로 할 필요 없다.
- 가이드의 "줄바꿈 후 추가 들여쓰기 단계 조정"(Continuation indent를 8로 바꾸는 팁)은 **적용하지 말 것**. `rule-config/checkstyle-rules.xml`의
  `Indentation` 모듈이 `lineWrappingIndentation=4`로 고정돼 있어서, 8로 바꾸면 IntelliJ 자동 포맷과 Checkstyle 검사 결과가 서로 어긋난다.
- 가이드의 "파일 끝에 개행 없으면 추가" 설정도 필요 없다. `.editorconfig`의 `insert_final_newline = true`를 IntelliJ가 이미 자동으로 지켜준다.
