package com.checkmate.bub.speech.domain;

import com.checkmate.bub.category.constant.CategoryType;
import com.checkmate.bub.global.config.audit.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * UR-USER-017: 음성 인식 로그 저장 엔티티
 * 사용자가 문장을 정확히 읽었을 때 확언 로그를 저장
 */
@Entity
@Table(name = "affirmation_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AffirmationLogEntity extends BaseTimeEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // 현재 카카오 로그인된 사용자의 닉네임 (Authentication에서 가져옴)
    @Column(nullable = false)
    private String userNickname;
    
    // MainAffirmationResponseDto.affirmation1/2/3에서 가져온 문장 내용
    @Column(nullable = false, length = 1000)
    private String sentence;
    
    // CategoryType.PROBLEM 사용
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoryType problem;
    
    // 톤 설정
    @Column(nullable = false)
    private String tone;
    
    // BaseTimeEntity의 createdAt이 읽은 시간으로 자동 설정됨
    
    // 톤 변경을 위한 메서드 (UR-USER-020)
    public void updateTone(String newTone) {
        this.tone = newTone;
    }
}