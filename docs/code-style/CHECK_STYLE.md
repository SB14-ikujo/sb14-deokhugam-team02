# Checkstyle 규칙 설명

`rule-config/checkstyle-rules.xml`은 [naver/hackday-conventions-java](https://github.com/naver/hackday-conventions-java)의 `naver-checkstyle-rules.xml`을 기반으로 한다. 아래는 파일 안에서 하나로 묶여있던 각 구간이 무엇을 검사하는지 정리한 것 (원본에 있던 `[tag]` 형태 주석은 지금 설명을 여기로 옮기면서 XML에서는 지웠고, 각 규칙의 위반 메시지 앞에 여전히 `[tag]`가 붙어서 나오므로 빌드 로그에서 이 표를 검색해서 찾아볼 수 있다).

## 파일 공통 (Checker 레벨, `.java` 전체에 적용)

| 태그 | 하는 일 |
|---|---|
| `[encoding-utf8]` | 소스 파일 인코딩을 UTF-8로 강제 |
| `[newline-lf]` | CRLF 금지, LF만 허용 |
| `[newline-eof]` | 파일 끝에 개행 1개 필요 |
| `[no-trailing-spaces]` | 줄 끝 공백 금지 |
| `[indentation-space]` | 파일 어디에도 탭 문자 금지 (들여쓰기뿐 아니라 문자열·주석 안의 탭도 포함, `FileTabCharacter` 모듈) — **원본은 `[indentation-tab]`(탭 강제)였는데, 이 프로젝트 `.editorconfig`(`indent_style = space`)에 맞춰 반대로 바꿈** |
| `[line-length-120]` | 한 줄 최대 120자 (`package`/`import`/URL 포함 줄은 예외) |

## Naming (이름 규칙)

| 태그 | 하는 일 |
|---|---|
| `[list-uppercase-abbr]` | 이름 속 대문자는 최대 2개까지만 연속 허용 (`getIO` OK, `getHTTP` 위반 → `getHttp`). `DAO`, `BO`는 예외 허용 |
| `[package-lowercase]` | 패키지명은 소문자만 |
| `[class-interface-lower-camelcase]` | 클래스/인터페이스명 네이밍 패턴 검사 (`TypeName` 체크, 별도 포맷 지정이 없어 Checkstyle 기본값인 UpperCamelCase 적용 — 태그 이름과 달리 실제로는 대문자로 시작해야 함) |
| `[method-lower-camelcase]` | 메서드명은 lowerCamelCase |
| `[var-lower-camelcase]` | 멤버/파라미터/지역변수명은 lowerCamelCase |
| `[avoid-1-char-var]` | 지역변수 한 글자 이름 금지 (for문 카운터는 예외) |

## Declarations (선언부)

| 태그 | 하는 일 |
|---|---|
| `[1-top-level-class]` | 파일당 top-level 클래스 1개만 (클래스 안의 중첩 클래스는 OK) — 원본은 message가 모듈 밖에 있고 key가 틀려 태그가 안 나왔는데, 모듈 안으로 옮기고 key를 `one.top.level.class`로 수정 |
| `[avoid-star-import]` | `import a.b.*` 금지 (static import는 허용) |
| `[modifier-order]` | 제어자(`public static final` 등) 순서는 JLS 권장 순서 |
| `[newline-after-annotation]` | 클래스/메서드/생성자의 어노테이션은 한 줄에 단독으로 (단, 파라미터 없는 어노테이션 1개는 같은 줄 허용) |
| `[1-state-per-line]` | 한 줄에 statement 하나만 (`int a = 1; int b = 2;` 금지) — 원본 message key(`needBraces`)가 틀려 태그가 안 나왔는데, `multiple.statements.line`으로 수정 |
| `[1-var-per-declaration]` | 변수 선언은 한 줄에 하나만 |
| `[array-square-after-type]` | 배열 대괄호는 타입 뒤에 (`String[] arr`, `String arr[]` 금지) |
| `[long-value-suffix]` | long 리터럴 접미사는 대문자 `L` |
| `[special-escape]` | 특수문자는 8진수/유니코드(`\011`, `\u000a` 등) 대신 이스케이프 시퀀스(`\n`, `\t`, `\"`, `\\` 등)로 작성 — 원본 메시지 태그가 `[array-square-after-type]`로 잘못 적혀 있던 것을 `[special-escape]`로 수정 |

## Indentation (들여쓰기)

| 태그 | 하는 일 |
|---|---|
| `[indentation-space]` | 위와 동일 (탭 문자 금지) — Checker 레벨 모듈(`FileTabCharacter`)이라 XML에는 이 구간이 아니라 파일 공통 쪽에 있다. 탭 폭 계산용 `tabWidth = 4`도 Checker 레벨에 있다 |

## Braces (중괄호)

| 태그 | 하는 일 |
|---|---|
| `[braces-knr-style]` | K&R 스타일 (여는 중괄호는 줄 끝, 닫는 중괄호는 단독 줄) |
| `[sub-flow-after-brace]` | `else`/`catch`/`finally`는 앞 블록의 닫는 중괄호와 같은 줄에 |
| `[need-braces]` | `if`/`for`/`while` 등에서 중괄호 생략 금지 |

## Line-wrapping (줄바꿈)

| 태그 | 하는 일 |
|---|---|
| `[1-line-package-import]` | `package`/`import` 문은 줄바꿈 금지 |
| `[block-indentation]` / `[indentation-after-line-wrapping]` | 블록·줄바꿈 들여쓰기 폭 4칸 — `.editorconfig`의 `indent_size = 4`와 일치 |
| `[line-wrapping-position]` | 쉼표는 줄 끝에, `.`과 연산자는 다음 줄 시작에 오도록 |

## Blank lines (빈 줄)

| 태그 | 하는 일 |
|---|---|
| `[import-grouping]` | import를 `java` → `javax` → `org` → `net` → 그 외 → `com.sprint.deokhugam` 순으로 그룹핑하고 그룹 사이 빈 줄 필요. static import는 맨 위, 그룹 안은 알파벳순 (원본은 `com.naver` 계열 패키지였던 걸 이 프로젝트 패키지로 교체) |
| `[blankline-between-methods]` | 메서드 사이에는 빈 줄 필요 |

## Whitespace (공백)

| 태그 | 하는 일 |
|---|---|
| `[space-around-brace]` | 중괄호 앞뒤 공백 (단, 빈 생성자/메서드/반복문 블록은 예외) |
| `[space-between-keyword-parentheses]` | `if`/`for`/`while` 등 키워드와 괄호 사이 공백 |
| `[no-space-between-identifier-parentheses]` | 메서드 호출 시 이름과 괄호 사이 공백 금지 |
| `[no-space-typecasting]` | 타입 캐스팅 괄호 안팎 공백 금지 |
| `[generic-whitespace]` | 제네릭 `<>` 안팎 공백 규칙 |
| `[space-after-comma-semicolon]` | 쉼표/세미콜론 뒤엔 공백, 앞엔 공백 금지 |
| `[space-around-colon]` | 콜론(삼항 연산자, enhanced-for) 앞뒤 공백 |
| `[no-space-unary-operator]` | 단항 연산자와 피연산자 사이 공백 금지 |
| `[space-around-binary-ternary-operator]` | 이항/삼항 연산자 앞뒤 공백 |

## 예외 처리

- 소스 코드에서 `// @checkstyle:off` ~ `// @checkstyle:on` 사이, 또는 `// @checkstyle:ignore` 주석이 붙은 줄은 검사 대상에서 빠진다.
- `rule-config/checkstyle-suppressions.xml`에 파일/규칙 단위로 예외를 등록할 수 있다 (현재는 빈 템플릿).
- `module-info.java`는 애초에 검사 대상에서 제외된다.

## 심각도(severity)와 빌드 실패

`Checker`의 `severity`는 `warning`으로 선언되어 있지만, `build.gradle`의 `checkstyle { maxWarnings = 0 }` 설정 때문에 경고가 하나라도 있으면 `checkstyleMain`/`checkstyleTest`가 실패한다. 즉 실질적으로는 전부 강제 규칙이다.

빌드 없이 규칙 하나만 빠르게 확인하려면 `./gradlew checkstyleMain`(운영 코드) / `./gradlew checkstyleTest`(테스트 코드)를 단독으로 돌리면 된다.
