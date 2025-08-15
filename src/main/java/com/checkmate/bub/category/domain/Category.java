package com.checkmate.bub.category.domain;

import com.checkmate.bub.category.constant.CategoryType;
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
    @Enumerated(EnumType.STRING)
    @Column(name = "category_type")
    private CategoryType type; // 문제, 톤 -> 오늘의 확언 문구 생성
    private String name; // 태그 이름

    // 더티체킹을 위한 업데이트 메서드
    public void update(CategoryType type, String name) {
        this.type = type;
        this.name = name;
    }
}
