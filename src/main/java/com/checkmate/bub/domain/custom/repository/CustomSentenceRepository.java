package com.checkmate.bub.domain.custom.repository;

import com.checkmate.bub.domain.custom.domain.CustomSentence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * UR-USER-028, UR-USER-036, UR-USER-037, UR-USER-038: 커스텀 문장 레포지토리
 */
@Repository
public interface CustomSentenceRepository extends JpaRepository<CustomSentence, Long> {

    /**
     * 사용자의 모든 커스텀 문장 조회
     */
    List<CustomSentence> findByUserNicknameOrderByCreatedAtDesc(String userNickname);

    /**
     * 특정 사용자의 특정 ID 커스텀 문장 조회
     */
    Optional<CustomSentence> findByIdAndUserNickname(Long id, String userNickname);

    /**
     * 사용자 커스텀 문장 개수
     */
    long countByUserNickname(String userNickname);
}
