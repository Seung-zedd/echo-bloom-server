package com.checkmate.bub.domain.custom.domain;

import com.checkmate.bub.global.config.audit.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * UR-USER-028, UR-USER-036, UR-USER-037, UR-USER-038: 사용자 커스텀 문장 엔티티
 * 사용자가 직접 입력한 문장을 저장/수정/삭제할 수 있는 기능
 */
@Entity
@Table(name = "custom_sentence")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomSentence extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 현재 카카오 로그인된 사용자의 닉네임
    @Column(nullable = false)
    private String userNickname;

    // 커스텀 문장 내용
    @Column(nullable = false, length = 1000)
    private String sentence;

    /**
     * UR-USER-037: 커스텀 문장 수정
     */
    public void updateSentence(String sentence) {
        this.sentence = sentence;
    }
}
