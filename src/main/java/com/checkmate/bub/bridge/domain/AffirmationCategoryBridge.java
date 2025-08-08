package com.checkmate.bub.bridge.domain;

import com.checkmate.bub.affirmation.domain.Affirmation;
import com.checkmate.bub.category.domain.Category;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AffirmationCategoryBridge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "affirmation_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Affirmation affirmation;

    @JoinColumn(name = "category_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Category category;
}
