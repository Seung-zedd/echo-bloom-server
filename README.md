# 🗣️🌸 에코블룸 (EchoBloom) - AI 긍정 확언 스피킹 서비스 (Backend)
하루 한 문장, 당신의 목소리로 긍정적인 변화를 만드는 AI 기반 스피킹 서비스 에코블룸 (EchoBloom)의 백엔드 레포지토리입니다.

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

**API Documentation**: Apidog(Swagger에서 이전)

**AI**: Naver CLOVA Studio (HyperCLOVA X), CLOVA Speech Recognition(+STT)

**Authentication**: JWT, OAuth 2.0

## MVP 동작 과정
1. 저희 앱의 메인 화면 페이지 입니다. 카카오 소셜 로그인을 통해 앱을 사용하실 수 있고, 로그인이 부담스러우시면 비회원 체험 기능을 통해 저희 앱의 간략한 서비스를 사용하실 수 있습니다.
<img width="531" height="912" alt="image" src="https://github.com/user-attachments/assets/735e0276-574d-4f56-97e2-5de344d446d7" />

<br><br><br>

2. 먼저 본인이 겪고 있는 문제를 최대 3개까지 선택합니다.
<img width="527" height="910" alt="image" src="https://github.com/user-attachments/assets/24e540ce-2407-40e4-977e-de8757d4ea67" />

   <br><br><br>

3. 그러면 사용자가 선택한 문제에 맞춰서 HyperClova X 모델이 3가지 다른 톤을 생성해줍니다.
<img width="527" height="912" alt="image" src="https://github.com/user-attachments/assets/4ff71263-39d7-47f8-a626-0c5719952d41" />

<br><br><br>

4. 홈 화면으로 가면 사용자가 선택한 문제, 톤을 조합해서 긍정 확언 문구 3가지를 만들어줍니다.(오른쪽 초록색 버튼을 눌러서 다른 확언 문구도 확인하실 수 있습니다.)
<img width="529" height="908" alt="image" src="https://github.com/user-attachments/assets/d66e7522-c64a-4a30-9a87-0eeee7b0e871" />

<br><br><br>
   
5. 긍정 확언 문구를 보는 것 뿐만 아니라, 아래의 꽃 버튼을 사용자가 눌러 따라 읽게 유도함으로써 뇌의 변화를 불러일으키게 합니다.
<img width="525" height="908" alt="image" src="https://github.com/user-attachments/assets/1f432109-4d05-4edb-b8e7-a1a6c41a77b7" />

<br><br><br>

6. 95% 이상 한국어의 정확성을 가진 Clova Speech Recognition 기술을 활용해 사용자가 성공적으로 읽으면 아래와 같은 화면을 보여줍니다.
<img width="528" height="912" alt="image" src="https://github.com/user-attachments/assets/d6787596-3363-47d4-97ca-ca36978b67db" />


## 💾 데이터베이스 스키마 (ERD)
서비스의 전체 데이터베이스 구조는 다음과 같습니다.
<img width="804" height="1712" alt="ERD 설계(최종)" src="https://github.com/user-attachments/assets/416f548b-ec46-47bb-98c3-f142d08b670a" />


## 🚀 시작하기
프로젝트를 로컬 환경에서 실행하는 방법입니다.

```txt
# 레포지토리 클론
$ git clone https://github.com/checkmate-BUB/echo-bloom-server.git

# 프로젝트 디렉토리로 이동
$ cd echo-bloom-server

# 빌드
$ ./gradlew build

# 실행
$ java -jar build/libs/echo-bloom-server-0.0.1-SNAPSHOT.jar
```
