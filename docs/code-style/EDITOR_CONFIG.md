# .editorconfig 설정

[editorconfig 공식 문서 참고](https://editorconfig.org/)

IDE·에디터가 달라도 들여쓰기, 줄바꿈, 인코딩 같은 기본 포맷을 통일하기 위해 프로젝트 루트에 `.editorconfig`를 둔다. <br/>
IntelliJ는 기본 내장 지원이라 별도 플러그인 없이 자동 적용된다.

## 현재 설정 (`/.editorconfig`)

```ini
root = true

[*]
charset = utf-8
end_of_line = lf
insert_final_newline = true
trim_trailing_whitespace = true

[*.java]
indent_style = space
indent_size = 4

[*.gradle]
indent_style = space
indent_size = 4

[*.{yml,yaml}]
indent_style = space
indent_size = 2

[*.bat]
end_of_line = crlf
```

## 규칙별 설명

- `root = true`: 이 파일이 최상위 설정임을 명시. 상위 디렉터리로 올라가며 다른 `.editorconfig`를 추가로 찾지 않는다.
- `[*]` (모든 파일 공통)
    - `charset = utf-8`: 인코딩 통일.
    - `end_of_line = lf`: 줄바꿈은 LF로 고정 (CLAUDE.md 코드 스타일 규칙과 동일).
    - `insert_final_newline = true`: 파일 끝에 개행 하나 보장.
    - `trim_trailing_whitespace = true`: 줄 끝 공백 자동 제거.
- `[*.java]`: 공백 4칸 들여쓰기.
- `[*.gradle]`: `build.gradle` 등 Gradle 스크립트도 공백 4칸.
- `[*.{yml,yaml}]`: `application.yml` 등 YAML은 공백 2칸.
- `[*.bat]`: Windows 배치 파일만 예외적으로 CRLF 허용.

## 적용 범위

`.editorconfig`는 **IntelliJ에서 편집·포맷팅할 때 파일을 정리하는 용도로만** 사용한다.
빌드 단계의 강제 검사는 하지 않는다.

- 빌드에서 강제하려면 `org.ec4j.editorconfig` Gradle 플러그인을 연동해야 하지만,
  빌드 시 **Gradle 10과 호환되지 않을 수 있다는 경고**가 발생해서 연동제거.
- 대신 `.java` 파일의 기본 포맷 규칙은 **Checkstyle이 빌드 단계에서 검사**한다.
  (`.editorconfig`와 겹치는 규칙: UTF-8 인코딩, LF 줄바꿈, 파일 끝 개행, 줄 끝 공백 금지, 탭 금지)

Checkstyle 설정은 [GRADLE_CODE_STYPE.md](GRADLE_CODE_STYPE.md) 참고.
