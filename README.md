
## Code convention
1. 브랜치 기능 구현이 완료되면 codeRabbit한테 코드 리뷰를 받기 위해 base branch는 develop으로 해서 PR을 생성한다.
2. 피드백을 바탕으로 코드 리팩토링이 완료되면 다시 한번 PR을 생성해서 최종적으로 리뷰를 받는다.
3. 모든 리뷰가 통과가 되면 최종적으로 develop에 merge PR request를 한다.

## 🔧프로젝트 기술 스택
Spring Boot 3.x, Java 21, JPA, MySQL

## 🚀 팀의 Git 워크플로우 (My Team Git Workflow)

이 프로젝트는 main 브랜치와 develop 브랜치를 중심으로, '기능 기반 브랜치 전략(Feature Branch Strategy)'을 사용합니다.

- 🚢main: 배포 가능한 안정적인 버전의 코드만 존재하는 브랜치.

- 🔧develop: 개발의 중심이 되는 브랜치. 모든 기능은 이곳으로 합쳐져 통합 테스트를 거칩니다.

- ✨feature/*: 하나의 기능을 개발하기 위한 브랜치. develop에서 시작하여 develop으로 합쳐집니다.

1. 기능 개발 시작 (Starting a New Feature): 새로운 기능 개발은 항상 최신 상태의 develop 브랜치에서 시작합니다.
```txt
# 1. 메인 개발 브랜치(develop)로 이동합니다.
git checkout develop

# 2. 원격 저장소의 최신 내용을 가져와 로컬 develop 브랜치를 업데이트합니다.
git pull origin develop

# 3. 새로운 기능 브랜치를 생성하고 그 브랜치로 이동합니다.
# 브랜치 이름 규칙: feature/기능-이름 (예: feature/login)
git checkout -b feature/새로운-기능
```

2. 기능 개발 및 원격에 올리기 (Develop & Push): 현재 브랜치가 feature/새로운-기능 인 것을 항상 확인하며 코딩을 진행합니다. 작업은 작은 단위로 나누어 의미 있는 메시지와 함께 자주 커밋하는 것이 좋습니다.

```txt
# 1. 수정한 파일들을 스테이징합니다.
git add .

# 2. 커밋 메시지를 작성합니다.
git commit -m "feat: 로그인 기능 기본 골격 추가"

# 3. 처음 원격에 올릴 때, -u 옵션으로 로컬 브랜치와 원격 브랜치를 연결합니다.
git push -u origin feature/새로운-기능
```

3. Pull Request (PR) 생성 및 코드 리뷰: 기능 개발이 완료되면, GitHub에서 **Pull Request(PR)**를 생성합니다.

base branch: develop

compare branch: feature/새로운-기능

PR을 생성하면 CodeRabbit 같은 자동 코드 리뷰 도구가 피드백을 주거나, 동료 개발자가 코드 리뷰를 진행합니다. 피드백을 받으면, 로컬 feature/새로운-기능 브랜치에서 코드를 수정하고 다시 push하여 PR에 반영합니다.

🔃이 과정은 최종 승인(Approve)을 받을 때까지 반복됩니다.

병합 및 뒷정리 (Merge & Clean up): 코드 리뷰가 끝나고 PR이 최종 승인되면, GitHub에서 Merge 버튼을 눌러 feature/새로운-기능 브랜치를 develop 브랜치에 합칩니다.
병합이 완료되면, 제 역할을 다한 기능 브랜치는 깔끔하게 삭제합니다.

```txt
# 1. 다시 메인 개발 브랜치(develop)로 돌아옵니다.
git checkout develop

# 2. 방금 병합된 최신 내용을 원격 저장소에서 다시 가져옵니다.
git pull origin develop

# 3. 이제 필요 없어진 로컬 기능 브랜치를 삭제합니다.
git branch -d feature/새로운-기능

# 4. (선택) GitHub에 있는 원격 기능 브랜치도 삭제합니다.
git push origin --delete feature/새로운-기능
(보통 GitHub에서 Merge 후 바로 브랜치를 삭제하는 버튼이 나타납니다.)
```

5. 릴리즈 및 배포 (Release & Deploy) develop 브랜치에 이번 버전에 포함될 모든 기능들이 통합되고, 충분한 테스트를 통해 안정성이 확보되었다고 판단되면 정식 버전을 배포합니다.

```txt
# 1. 배포의 기준이 될 main 브랜치로 이동합니다.
git checkout main

# 2. main 브랜치를 최신 상태로 업데이트합니다.
git pull origin main

# 3. develop 브랜치의 모든 내용을 main 브랜치로 병합합니다.
git merge develop

# 4. main 브랜치에 병합된 내용을 원격 저장소에 푸시합니다.
git push origin main

# 5. 역사적인 기록을 위해 버전을 명시하는 태그(Tag)를 생성하고, 태그도 푸시합니다.
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0
```

이 과정을 통해 main 브랜치는 항상 안정적이고 배포 가능한 상태를 유지하게 됩니다.

# 🗣️ BUB - AI 긍정 확언 스피킹 서비스 (Backend)
하루 한 문장, 당신의 목소리로 긍정적인 변화를 만드는 AI 기반 스피킹 서비스 BUB의 백엔드 레포지토리입니다.

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

