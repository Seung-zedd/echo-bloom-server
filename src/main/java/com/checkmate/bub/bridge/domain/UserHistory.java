package com.checkmate.bub.bridge.domain;

import com.checkmate.bub.affirmation.domain.Affirmation;
import com.checkmate.bub.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
//* 1. JPA를 위해 기본 생성자는 필요하지만, 외부에서 함부로 쓰지 못하게 protected로 제한합니다.
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
//* "사용자의 확언 기록"이라는 서비스의 핵심적인 도메인 의미를 가지기 때문에 suffix에 bridge를 붙이지 않았음.
public class UserHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_history_id")
    private Long id;

    // UserHistory → User로 향하는 단방향 관계(∵ 사용자 확언 기록 테이블에 사용자를 외래키로 접근만 하면 됨)
    @JoinColumn(name = "user_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    // 위와 동문
    @JoinColumn(name = "affirmation_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Affirmation affirmation;
    private LocalDateTime viewedAt;
}
