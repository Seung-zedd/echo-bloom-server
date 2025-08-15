package com.checkmate.bub.user.domain;

import com.checkmate.bub.global.config.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
//* 1. JPA를 위해 기본 생성자는 필요하지만, 외부에서 함부로 쓰지 못하게 protected로 제한합니다.
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "users")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private Long kakaoId;

    @Column(nullable = false)
    private String nickname; // 카카오 실명

    @Column(length = 500)
    private String profileImageUrl;
    private String email;
    private String displayName; // 앱에서 사용할 별칭
    private LocalDate dateOfBirth; // 생년월일
    private String gender; // 성
    private String interest; // 관심사
    private String jobGroup; // 직군

}
