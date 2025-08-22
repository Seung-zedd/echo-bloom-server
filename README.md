# 🗣️🌸 에코블룸 (EchoBloom) - AI 긍정 확언 스피킹 서비스 (Backend)
하루 한 문장, 당신의 목소리로 긍정 문구를 스피킹함으로써 정서 회복, 자기 긍정, 행동 변화를 돕는 AI 기반 긍정 확언 스피킹 서비스

> 생각만 하던 나를 움직이게 하는 한 마디 혹시 매일 이런 생각에 갇혀 있진 않으신가요? '할 수 있을까..?' 망설이다 하루를 다 보내버린 적이 있나요? '괜찮아, 괜찮아' 되뇌어도 불안이 가시지 않는 날들. '난 늘 이렇지' 부정적인 생각의 굴레에서 벗어나고 싶나요? 생각만 하고 행동하지 않는 나 자신에게 지친 당신.

<br>

🌻에코블룸(EchoBloom)이 당신의 첫걸음을 도와줘요!

<br><br>

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

---

## 🖼️ 앱 스크린샷
| 홈 화면 페이지 | 온보딩 1 | 온보딩 2 |
| :---: | :---: | :---: |
| <img src="https://github.com/user-attachments/assets/735e0276-574d-4f56-97e2-5de344d446d7" width="250"> | <img src="https://github.com/user-attachments/assets/24e540ce-2407-40e4-977e-de8757d4ea67" width="250"> | <img src="https://github.com/user-attachments/assets/4ff71263-39d7-47f8-a626-0c5719952d41" width="250">

| 메인 화면 | 기능 화면 1 | 기능 화면 2 |
| :---: | :---: | :---: |
| <img src="https://github.com/user-attachments/assets/d66e7522-c64a-4a30-9a87-0eeee7b0e871" width="250"> | <img src="https://github.com/user-attachments/assets/1f432109-4d05-4edb-b8e7-a1a6c41a77b7" width="250"> | <img src="https://github.com/user-attachments/assets/d6787596-3363-47d4-97ca-ca36978b67db" width="250"> |


---

### 상세 설명

- **홈 화면 페이지**
  
  사용자가 소셜 로그인을 통해 직접 앱 서비스를 사용하거나, 비회원 기능을 통해 간단한 앱 서비스 체험이 가능합니다.
  
  <br>

- **온보딩**
  
  사용자가 현재 겪고 있는 문제를 최대 3가지까지 선택하실 수 있으며, HyperCLOVA X 모델이 그에 걸맞는 3가지 색다른 톤이 담긴 예시 문구들을 생성해줍니다.

  <br>

- **메인 화면**
  
  사용자가 온보딩 단계에서 선택한 문제, 톤을 조합해서 HyperCLOVA X 모델이 긍정 확언 문구 3가지를 만들어줍니다.
  - 오른쪽 초록색 버튼을 눌러서 다른 확언 문구도 확인하실 수 있습니다.
 
    <br>

- **기능 화면**
  
  긍정 확언 문구를 보는 것 뿐만 아니라, 아래의 꽃 버튼을 사용자가 눌러 따라 읽게 유도함으로써 뇌의 변화를 불러일으키게 합니다.
  
  95% 이상 한국어의 정확성을 가진 Clova Speech Recognition 기술을 활용해 사용자가 성공적으로 읽으면 아래와 같은 화면을 보여줍니다.

  <br><br>

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

## 👩‍💻 팀원 소개
| 이름 | 역할 | GitHub |
| --- | --- | --- |
| 조승제 | Backend | [Seung-zedd](https://github.com/Seung-zedd) |
| 노범석 | Backend | [@github-id](https://github.com/아이디) |
| 김도현 | Frontend | ... |
| 남도경 | PM | ... |
| 송수연 | PM | ... |
| 정승진 | UI/UX Designer | ... |

## 📜 라이선스
이 프로젝트는 MIT 라이선스를 따릅니다.
