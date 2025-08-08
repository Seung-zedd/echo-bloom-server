package com.checkmate.bub.category.domain;

import com.checkmate.bub.global.config.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
//* 1. JPA를 위해 기본 생성자는 필요하지만, 외부에서 함부로 쓰지 못하게 protected로 제한합니다.
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Category extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long id;

    // postgreSQL 예약어 충돌을 피하기 위함
    @Column(name = "category_type")
    private String type;
    private String name; // 태그 이름


}
