package com.checkmate.bub.user.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
//* 1. JPA를 위해 기본 생성자는 필요하지만, 외부에서 함부로 쓰지 못하게 protected로 제한합니다.
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long kakaoId;
    private String nickname; // 앱에서 사용할 별칭
    private String profile_image_url;
    private String email;
    private String name; // 사용자 ID
    private LocalDate dateOfBirth; // 생년월일
    private String gender; // 성
    private String interest; // 관심사
    private String job_group; // 직군

}
