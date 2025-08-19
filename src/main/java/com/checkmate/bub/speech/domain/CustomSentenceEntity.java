package com.checkmate.bub.speech.domain;

import com.checkmate.bub.global.config.audit.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * AR-ADMIN-005: 개인화된 문장 저장 엔티티
 * 사용자의 커스텀 문장을 저장하고 관리
 */
@Entity
@Table(name = "custom_sentences")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomSentenceEntity extends BaseTimeEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // 현재 카카오 로그인된 사용자의 닉네임
    @Column(nullable = false)
    private String userNickname;
    
    // 커스텀 문장 내용
    @Column(nullable = false, length = 1000)
    private String sentence;
    
    // 문장의 톤
    @Column(nullable = false)
    private String tone;
    
    // 문장 수정을 위한 메서드
    public void updateSentence(String newSentence) {
        this.sentence = newSentence;
    }
    
    // 톤 변경을 위한 메서드
    public void updateTone(String newTone) {
        this.tone = newTone;
    }
}