# 🗣️🌸 EchoBloom - AI Positive Affirmation Speaking Service
An AI-based positive affirmation speaking service that helps with emotional recovery, self-affirmation, and behavioral change by speaking one positive phrase a day in your own voice.
 
> A single phrase to move you from just thinking to acting. Are you trapped in these thoughts every day? Have you ever spent a whole day hesitating, wondering, 'Can I do it...?' Days when anxiety doesn't fade, no matter how many times you tell yourself, 'It's okay, it's okay.' Do you want to break free from the cycle of negative thoughts like, 'I'm always like this'? For you, who are tired of just thinking and not acting.

<br>

🌻 EchoBloom is here to help you take the first step!

<br><br>

## 🌟 Key Features (MVP)
- **AI-Powered Custom Affirmations**: HyperCLOVA X generates positive affirmation phrases in real-time based on the categories (personality, goals, tone) selected by the user.

- **Voice Recognition Speaking Practice**: When the user repeats the generated sentence, CLOVA Speech checks the accuracy of the pronunciation.

- **Growth Tracking**: Successfully spoken affirmations are recorded in the DB, allowing users to track their growth process.

- **Social Login**: Users can easily start the service through Kakao social login.

<br>

### Detailed Description
- **Home Screen Page**: Users can use the app service directly through social login or experience a simple version of the app's features as a non-member.

- **Onboarding**: Users can select up to three problems they are currently facing, and the HyperCLOVA X model will generate example phrases in three different tones that are tailored to those problems.

- **Main Screen**: The HyperCLOVA X model creates three positive affirmation phrases by combining the problems and tones selected by the user during the onboarding stage.
  - You can press the green button on the right to see other affirmation phrases.

- **Feature Screen**: Not only can users view the positive affirmation phrases, but they can also press the flower button below to be guided to read them aloud, inducing a change in the brain.
  - Utilizing Clova Speech Recognition technology with over 95% accuracy for Korean, if the user reads it successfully, the second feature screen above is shown.

<br><br>

## 🛠️ Tech Stack
**Language**: Java 21

**Framework**: Spring Boot 3.x

**Database**: PostgreSQL

**Data Access**: Spring Data JPA

**API Documentation**: Apidog (migrated from Swagger)
- v1/endpoint (custom, before implementing bookmark feature): <a href="https://07olkvu7eg.apidog.io">EndPoint</a>

**AI**: Naver CLOVA Studio (HyperCLOVA X), CLOVA Speech Recognition (+STT)

**Authentication**: JWT, OAuth 2.0

<br><br>

## 📂 Project Package Structure
```bash
echo-bloom-server/
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── checkmate/
        │           └── bub/
        │               ├── domain/
        │               │   ├── affirmation/
        │               │   │   ├── controller/
        │               │   │   ├── domain/
        │               │   │   ├── dto/
        │               │   │   └── service/
        │               │   ├── ai/
        │               │   │   └── clova/
        │               │   ├── bookmark/
        │               │   │   ├── controller/
        │               │   │   ├── domain/
        │               │   │   └── service/
        │               │   ├── bridge/
        │               │   │   ├── domain/
        │               │   │   └── repository/
        │               │   ├── category/
        │               │   │   ├── constant/
        │               │   │   ├── domain/
        │               │   │   ├── init/
        │               │   │   └── repository/
        │               │   ├── profile/
        │               │   │   └── member/
        │               │   │       ├── constant/
        │               │   │       ├── controller/
        │               │   │       ├── domain/
        │               │   │       ├── dto/
        │               │   │       ├── repository/
        │               │   │       └── service/
        │               │   │           └── helper/
        │               │   └── speech/
        │               │       ├── constant/
        │               │       ├── controller/
        │               │       ├── domain/
        │               │       ├── dto/
        │               │       ├── repository/
        │               │       └── service/
        │               └── global/
        │                   ├── auth/
        │                   │   ├── controller/
        │                   │   ├── dto/
        │                   │   └── service/
        │                   ├── config/
        │                   │   ├── audit/
        │                   │   ├── feign/
        │                   │   ├── security/
        │                   │   └── web/
        │                   ├── exception/
        │                   ├── jwt/
        │                   └── util/
        └── resources/
            ├── static/
            │   ├── .well-known/
            │   │   └── appspecific/
            │   ├── img/
            │   ├── music/
            │   └── views/
            └── templates/

```

<br><br>

## 💾 Database Schema (ERD)
The overall database structure of the service is as follows.
<br>

<img width="804" height="1712" alt="ERD Design (Final)" src="https://github.com/user-attachments/assets/416f548b-ec46-47bb-98c3-f142d08b670a" />

<br><br>

## 🏭 System Architecture
The overall system architecture of our app service is as follows.
<br>

<img width="3513" height="3063" alt="System Architecture" src="https://github.com/user-attachments/assets/c432f974-b0d1-4f8e-8b37-f51c3eea69ec" />

<br><br>

## 🚀 Getting Started
How to run the project in your local environment.

```txt
# Clone the repository
$ git clone git clone https://github.com/Seung-zedd/echo-bloom-server.git

# Navigate to the project directory
$ cd echo-bloom-server

# Build
$ ./gradlew build

# Run
$ java -jar build/libs/echo-bloom-server-0.0.1-SNAPSHOT.jar
```
<br><br>

## 👩‍💻 Team Members
| Name | Role | GitHub |
| --- | --- | --- |
| Cho Seung-je | Backend | [Seung-zedd](https://github.com/Seung-zedd) |
| Noh Beom-seok | Backend | [prodigy0831](https://github.com/prodigy0831) |
| Kim Do-hyeon | Frontend | [rlaehgus97](https://github.com/rlaehgus97) |
| Jeong Seung-jin | UI/UX Designer | [SengJinn](https://github.com/SengJinn) |
| Nam Do-kyung | PM |  |
| Song Su-yeon | PM |  |

<br><br>

## 📜 License
This project is licensed under the MIT License.
