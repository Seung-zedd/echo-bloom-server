package com.checkmate.bub.domain.bridge.domain;

import com.checkmate.bub.domain.affirmation.domain.Affirmation;
import com.checkmate.bub.domain.category.domain.Category;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
// 중복 매핑 방지 및 무결성 강화: 유니크 제약·NOT NULL 적용
@Table(
        name = "affirmation_category_bridge",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_affirmation_category",
                        columnNames = {"affirmation_id", "category_id"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AffirmationCategoryBridge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "affirmation_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Affirmation affirmation;

    @JoinColumn(name = "category_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Category category;
}
