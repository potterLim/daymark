# Daymark

Daymark는 하루의 계획과 회고를 한 곳에 정리하는 Spring Boot 기반 웹 애플리케이션입니다.

아침에는 오늘의 목표와 집중 기준을 세우고, 저녁에는 실행 결과와 배운 점을 남깁니다. 쌓인 기록은 주간 흐름, 기록 라이브러리, Markdown/PDF 내보내기로 다시 확인할 수 있습니다.

Daymark는 개인 루틴 기록 도구의 사용자 경험과 작은 SaaS 서비스의 운영 경계를 함께 다룹니다. 인증, 요청 제한, 운영 지표, 운영 준비 검증, 공개 저장소 위생 검사까지 포함합니다.

## 화면

![Daymark 홈 화면](docs/assets/daymark-home.png)

| 작성 | 개인 주간 리뷰 |
| --- | --- |
| ![Daymark 아침 계획 작성 화면](docs/assets/daymark-morning-plan.png) | ![Daymark 개인 주간 리뷰 화면](docs/assets/daymark-weekly-review.png) |

## 주요 기능

- Morning Plan: 날짜별 목표, 집중 영역, 예상 변수를 기록합니다.
- Evening Review: 아침 목표를 체크리스트로 확인하고 성과와 개선점을 남깁니다.
- Weekly Review: 월요일부터 일요일까지의 루틴 흐름과 목표 완료율을 확인합니다.
- Records: 날짜 범위와 키워드로 장기 기록을 탐색합니다.
- Export: 선택한 기록을 Markdown으로 다운로드하거나 PDF 저장용 보고서로 확인합니다.
- Account: Google 계정 확인 후 Workspace ID와 비밀번호를 관리합니다.
- Operations: 관리자 전용 화면에서 Workspace 성장, 루틴 수행, 목표 완료, 내보내기, 로그인 흐름을 확인합니다.

## 기술 스택

| 영역 | 기술 |
| --- | --- |
| Backend | Java 17, Spring Boot 3.5.9 |
| Web | Spring MVC, Thymeleaf |
| Security | Spring Security, OAuth2 Client |
| Data | Spring Data JPA, Flyway, MySQL |
| Local/Test | H2, Testcontainers |
| Build | Gradle, Checkstyle, JaCoCo |

## 로컬 실행

로컬 프로필은 H2 메모리 데이터베이스를 사용합니다.

```bash
./gradlew bootRun --args="--spring.profiles.active=local"
```

접속 주소:

```text
http://127.0.0.1:8080
```

Google 로그인까지 확인하려면 Google OAuth 클라이언트를 만들고 환경 변수를 설정합니다.

```bash
export GOOGLE_CLIENT_ID="your-google-client-id"
export GOOGLE_CLIENT_SECRET="your-google-client-secret"
```

로컬 redirect URI:

```text
http://127.0.0.1:8080/login/oauth2/code/google
http://localhost:8080/login/oauth2/code/google
```

## 테스트와 빌드

기본 테스트:

```bash
./gradlew test
```

Docker가 준비되어 있다면 MySQL 통합 테스트도 실행할 수 있습니다.

```bash
./gradlew mysqlIntegrationTest
```

테스트, Checkstyle, 공개 저장소 위생 검사, 커버리지 리포트까지 함께 확인합니다.

```bash
./gradlew check
```

배포용 JAR:

```bash
./gradlew bootJar
```

생성 파일:

```text
build/libs/daymark.jar
```

커버리지 리포트:

```text
build/reports/jacoco/test/html/index.html
```

## 환경 변수

`local` 프로필은 H2 메모리 데이터베이스와 로컬 확인용 Google OAuth 기본값을 사용합니다.
기본 프로필과 `production` 프로필은 MySQL과 보안 값을 요구합니다.

| 환경 변수 | 설명 |
| --- | --- |
| `DAYMARK_PUBLIC_BASE_URL` | 공개 HTTPS 주소 |
| `DATABASE_URL` | MySQL JDBC URL |
| `DATABASE_USERNAME` | MySQL 사용자 |
| `DATABASE_PASSWORD` | MySQL 비밀번호 |
| `DAYMARK_REMEMBER_ME_KEY` | remember-me 서명 키 |
| `GOOGLE_CLIENT_ID` | Google OAuth 클라이언트 ID |
| `GOOGLE_CLIENT_SECRET` | Google OAuth 클라이언트 secret |

운영에서 함께 확인하는 값:

| 환경 변수 | 설명 |
| --- | --- |
| `SERVER_SERVLET_SESSION_COOKIE_SECURE` | 운영 세션 쿠키 Secure 설정 |
| `DAYMARK_REMEMBER_ME_COOKIE_SECURE` | remember-me 쿠키 Secure 설정 |
| `DAYMARK_ADMINISTRATOR_WORKSPACE_IDS` | 관리자 권한을 받을 Workspace ID 목록 |
| `DAYMARK_REQUIRE_ALERT_WEBHOOK` | 운영 준비 검증에서 alert webhook을 필수로 볼지 여부 |
| `DAYMARK_ALERT_WEBHOOK_URL` | 운영 알림 webhook URL |
| `DAYMARK_WEEKLY_SUMMARY_ENABLED` | 주간 운영 요약 스케줄 활성화 여부 |
| `DAYMARK_WEEKLY_SUMMARY_CRON` | 주간 운영 요약 실행 cron |
| `DAYMARK_WEEKLY_SUMMARY_ZONE` | 주간 운영 요약 기준 시간대 |

Docker Compose 실행은 `.env.example`의 `MYSQL_*`, `APP_PORT` 값을 함께 사용합니다. Compose의 `app` 서비스는 `db` 서비스 이름을 기준으로 `DATABASE_URL`을 구성합니다.

실제 secret 값은 저장소에 커밋하지 않습니다. `.env.example`과 `ops/aws/ecs-express-env.example`에는 예시 값만 둡니다. `production` 프로필은 `example.com`, `change-this` 같은 placeholder 값을 운영 준비 검증에서 거부합니다.

## 운영 기준

- 기본 프로필은 필수 데이터베이스와 보안 값이 없으면 실행되지 않도록 구성합니다.
- `production` 프로필은 HTTPS 공개 주소, secure cookie, remember-me key, Google OAuth 설정을 검증합니다.
- 로그인, 가입, 기록 저장, 내보내기 요청에는 rate limit을 적용합니다.
- CI는 `./gradlew check bootJar`와 MySQL Testcontainers 통합 테스트를 실행합니다.
- Dockerfile, compose 설정, AWS 배포 보조 스크립트를 포함하되 실제 secret과 운영 전용 문서는 공개 Git에 올리지 않습니다.

## 프로젝트 구조

```text
src/main/java/com/potterlim/daymark
├─ config
├─ controller
├─ dto
├─ entity
├─ identity
├─ repository
├─ security
├─ service
└─ support
```

주요 데이터는 `user_account`, `daymark_entry`, `operation_usage_event`, `weekly_operation_metric_snapshot` 테이블에 저장됩니다. 날짜별 기록은 하나의 Markdown 문자열이 아니라 섹션별 텍스트로 저장하고 미리보기와 내보내기 화면에서 다시 조합합니다.

## 문서

- [문서 안내](docs/README.md)
- [프로젝트 구조](docs/project-architecture.md)

## 저장소 관리

- 로그, 캡처, 내보내기 결과물, DB dump, 실제 운영 secret은 Git에 포함하지 않습니다.
- 배포, 운영 인수인계, 출시 점검 문서는 로컬 전용 문서로 관리합니다.
- 공개 문서는 현재 코드와 제품 흐름을 기준으로 유지합니다.
