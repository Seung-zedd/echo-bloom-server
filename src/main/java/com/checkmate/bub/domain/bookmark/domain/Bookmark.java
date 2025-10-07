package com.checkmate.bub.domain.bookmark.domain;

import com.checkmate.bub.global.config.audit.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * UR-USER-021, UR-USER-022, AR-ADMIN-005: 확언 북마크 엔티티
 * 사용자가 문장을 정확히 읽은 후 북마크에 추가/제거할 수 있는 기능
 */
@Entity
@Table(
        name = "bookmark",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_nickname", "sentence"})
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bookmark extends BaseTimeEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // 현재 카카오 로그인된 사용자의 닉네임
    @Column(nullable = false)
    private String userNickname;
    
    // 북마크된 문장 내용
    @Column(nullable = false, length = 1000)
    private String sentence;
    
    // 문장의 톤
    @Column(nullable = false)
    private String tone;
}
