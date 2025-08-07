# 🗣️🌸 에코블룸 (EchoBloom) - AI 긍정 확언 스피킹 서비스 (Backend)
하루 한 문장, 당신의 목소리로 긍정적인 변화를 만드는 AI 기반 스피킹 서비스 VocaGlow (보카글로우)의 백엔드 레포지토리입니다.

## 🌟 주요 기능 (MVP)
- **AI 맞춤 확언 생성**: 사용자가 선택한 카테고리(성격, 목표, 톤)에 맞춰 HyperCLOVA X가 실시간으로 긍정 확언 문구를 생성합니다.

- **음성 인식 발화**: 사용자가 생성된 문장을 따라 말하면, CLOVA Speech가 발음의 정확도를 체크합니다.

- **성장 기록**: 성공적으로 말한 확언은 DB에 기록되어 사용자의 성장 과정을 추적할 수 있습니다.

- **소셜 로그인**: 카카오 소셜 로그인을 통해 간편하게 서비스를 시작할 수 있습니다.

## 🛠️ 기술 스택
**Language**: Java 21

**Framework**: Spring Boot 3.x

**Database**: PostgreSQL

**Data Access**: Spring Data JPA, QueryDSL

**AI**: Naver CLOVA Studio (HyperCLOVA X), CLOVA Speech

**Authentication**: JWT, OAuth 2.0

## 💾 데이터베이스 스키마 (ERD)
서비스의 전체 데이터베이스 구조는 다음과 같습니다.
<img width="804" height="1109" alt="BUB_ERD 설계" src="https://github.com/user-attachments/assets/7e2960d7-54c6-4c1f-b208-8aae272d9c2f" />

## 🚀 시작하기
프로젝트를 로컬 환경에서 실행하는 방법입니다.



```txt
# 레포지토리 클론
$ git clone https://github.com/checkmate-BUB/bub-server.git

# 프로젝트 디렉토리로 이동
$ cd bub-server

# 빌드
$ ./gradlew build

# 실행
$ java -jar build/libs/bub-server-0.0.1-SNAPSHOT.jar
```
