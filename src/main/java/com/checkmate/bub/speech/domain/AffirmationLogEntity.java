package com.checkmate.bub.speech.domain;

import com.checkmate.bub.global.config.audit.BaseTimeEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AffirmationLogEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;  // UR-USER-017: 유저 ID

    private String sentence;  // UR-USER-017: 문장 내용 (MainAffirmationResponseDto의 affirmation1/2/3 중 하나 저장, e.g., "오늘은 좋은 날입니다")

    private String category;  // UR-USER-017: 문장 카테고리 (문제) - e.g., "PROBLEM" 또는 Enum CategoryType.PROBLEM

    private String tone;  // UR-USER-017: 톤 (선택적, e.g., "positive", "motivational" - 요구사항 확장)
}

