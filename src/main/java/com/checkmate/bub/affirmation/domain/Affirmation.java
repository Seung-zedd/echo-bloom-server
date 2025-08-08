package com.checkmate.bub.affirmation.domain;

import com.checkmate.bub.global.config.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
//* 1. JPA를 위해 기본 생성자는 필요하지만, 외부에서 함부로 쓰지 못하게 protected로 제한합니다.
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Affirmation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "affirmation_id")
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String content; // 클로바 모델로 만든 확언 문구

    //? createdAt 필드는 이미 Audit 기능인 BaseEntity를 상속했으므로 제외했습니다.
}
