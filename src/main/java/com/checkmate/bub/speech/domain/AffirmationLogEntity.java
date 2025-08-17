package com.checkmate.bub.speech.domain;

import com.checkmate.bub.global.config.audit.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "affirmation_logs")
public class AffirmationLogEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;  // UR-USER-017: 유저 ID

    private String sentence;  // UR-USER-017: 문장 내용 (MainAffirmationResponseDto의 affirmation1/2/3 중 하나 저장, e.g., "오늘은 좋은 날입니다")

    private String problem;  // 문제 카테고리에 해당하는 이름

}

