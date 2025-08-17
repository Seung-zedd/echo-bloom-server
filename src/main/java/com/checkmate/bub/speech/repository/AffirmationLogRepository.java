package com.checkmate.bub.speech.repository;

import com.checkmate.bub.speech.domain.AffirmationLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * UR-USER-017: 음성 인식 로그 저장 Repository
 * 사용자의 음성 인식 기록을 관리
 * SecurityUtils.getCurrentNickname()을 통해 현재 로그인된 사용자 식별
 */
@Repository
public interface AffirmationLogRepository extends JpaRepository<AffirmationLogEntity, Long> {
    
    // 특정 사용자의 로그 조회 (최신순) - SpeechService.getUserSpeechLogs()에서 사용
    List<AffirmationLogEntity> findByUserNicknameOrderByCreatedAtDesc(String userNickname);
}