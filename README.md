## Code convention
1. 브랜치 기능 구현이 완료되면 codeRabbit한테 코드 리뷰를 받기 위해 base branch는 dev으로 해서 PR을 생성한다.
2. 피드백을 바탕으로 코드 리팩토링이 완료되면 다시 한번 PR을 생성해서 최종적으로 리뷰를 받는다.
3. 모든 리뷰가 통과가 되면 최종적으로 dev에 merge PR request를 한다.

## 🔧프로젝트 기술 스택
Spring Boot 3.x, Java 21, JPA, PostgreSQL

## 🚀 팀의 Git 워크플로우 (My Team Git Workflow)

이 프로젝트는 main 브랜치와 dev 브랜치를 중심으로, '기능 기반 브랜치 전략(Feature Branch Strategy)'을 사용합니다.

- 🚢main: 배포 가능한 안정적인 버전의 코드만 존재하는 브랜치.

- 🔧dev: 개발의 중심이 되는 브랜치. 모든 기능은 이곳으로 합쳐져 통합 테스트를 거칩니다.

- ✨feature/*: 하나의 기능을 개발하기 위한 브랜치. dev에서 시작하여 dev으로 합쳐집니다.

1. 기능 개발 시작 (Starting a New Feature): 새로운 기능 개발은 항상 최신 상태의 dev 브랜치에서 시작합니다.
```txt
# 1. 메인 개발 브랜치(dev)로 이동합니다.
git checkout dev

# 2. 원격 저장소의 최신 내용을 가져와 로컬 dev 브랜치를 업데이트합니다.
git pull origin dev

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

base branch: dev

compare branch: feature/새로운-기능

PR을 생성하면 CodeRabbit 같은 자동 코드 리뷰 도구가 피드백을 주거나, 동료 개발자가 코드 리뷰를 진행합니다. 피드백을 받으면, 로컬 feature/새로운-기능 브랜치에서 코드를 수정하고 다시 push하여 PR에 반영합니다.

🔃이 과정은 최종 승인(Approve)을 받을 때까지 반복됩니다.

병합 및 뒷정리 (Merge & Clean up): 코드 리뷰가 끝나고 PR이 최종 승인되면, GitHub에서 Merge 버튼을 눌러 feature/새로운-기능 브랜치를 develop 브랜치에 합칩니다.
병합이 완료되면, 제 역할을 다한 기능 브랜치는 깔끔하게 삭제합니다.

```txt
# 1. 다시 메인 개발 브랜치(dev)로 돌아옵니다.
git checkout dev

# 2. 방금 병합된 최신 내용을 원격 저장소에서 다시 가져옵니다.
git pull origin dev

# 3. 이제 필요 없어진 로컬 기능 브랜치를 삭제합니다.
git branch -d feature/새로운-기능

# 4. (선택) GitHub에 있는 원격 기능 브랜치도 삭제합니다.
git push origin --delete feature/새로운-기능
(보통 GitHub에서 Merge 후 바로 브랜치를 삭제하는 버튼이 나타납니다.)
```

5. 릴리즈 및 배포 (Release & Deploy) dev 브랜치에 이번 버전에 포함될 모든 기능들이 통합되고, 충분한 테스트를 통해 안정성이 확보되었다고 판단되면 정식 버전을 배포합니다.

```txt
# 1. 배포의 기준이 될 main 브랜치로 이동합니다.
git checkout main

# 2. main 브랜치를 최신 상태로 업데이트합니다.
git pull origin main

# 3. develop 브랜치의 모든 내용을 main 브랜치로 병합합니다.
git merge dev

# 4. main 브랜치에 병합된 내용을 원격 저장소에 푸시합니다.
git push origin main

# 5. 역사적인 기록을 위해 버전을 명시하는 태그(Tag)를 생성하고, 태그도 푸시합니다.
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0
```

이 과정을 통해 main 브랜치는 항상 안정적이고 배포 가능한 상태를 유지하게 됩니다.
