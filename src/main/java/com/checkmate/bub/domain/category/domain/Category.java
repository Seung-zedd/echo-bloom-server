package com.checkmate.bub.domain.category.domain;

import com.checkmate.bub.domain.category.constant.CategoryType;
import com.checkmate.bub.global.config.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
//* 1. JPA를 위해 기본 생성자는 필요하지만, 외부에서 함부로 쓰지 못하게 protected로 제한합니다.
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "category",  // 테이블 이름 (필요 시 지정)
        indexes = {@Index(name = "idx_category_type_name",  // 인덱스 이름: 컬럼 조합 반영
                columnList = "category_type, name",  // DB 컬럼 이름으로 변경
                unique = true)})  // 중복 방지 필요 시 true
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

}
