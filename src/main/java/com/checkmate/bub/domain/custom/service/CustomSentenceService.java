package com.checkmate.bub.domain.custom.service;

import com.checkmate.bub.domain.custom.domain.CustomSentence;
import com.checkmate.bub.domain.custom.repository.CustomSentenceRepository;
import com.checkmate.bub.global.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * UR-USER-028, UR-USER-036, UR-USER-037, UR-USER-038: 커스텀 문장 관리 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CustomSentenceService {

    private final CustomSentenceRepository customSentenceRepository;

    /**
     * UR-USER-036: 커스텀 문장 추가
     */
    @Transactional
    public CustomSentence addCustomSentence(String sentence) {
        String userNickname = SecurityUtils.getCurrentNickname();

        CustomSentence customSentence = CustomSentence.builder()
                .userNickname(userNickname)
                .sentence(sentence)
                .build();

        return customSentenceRepository.save(customSentence);
    }

    /**
     * UR-USER-037: 커스텀 문장 수정
     */
    @Transactional
    public CustomSentence updateCustomSentence(Long id, String sentence) {
        String userNickname = SecurityUtils.getCurrentNickname();

        CustomSentence customSentence = customSentenceRepository.findByIdAndUserNickname(id, userNickname)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 커스텀 문장입니다."));

        customSentence.updateSentence(sentence);
        return customSentence;
    }

    /**
     * UR-USER-038: 커스텀 문장 삭제
     */
    @Transactional
    public void deleteCustomSentence(Long id) {
        String userNickname = SecurityUtils.getCurrentNickname();

        CustomSentence customSentence = customSentenceRepository.findByIdAndUserNickname(id, userNickname)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 커스텀 문장입니다."));

        customSentenceRepository.delete(customSentence);
    }

    /**
     * UR-USER-028: 사용자의 모든 커스텀 문장 조회
     */
    public List<CustomSentence> getUserCustomSentences() {
        String userNickname = SecurityUtils.getCurrentNickname();
        return customSentenceRepository.findByUserNicknameOrderByCreatedAtDesc(userNickname);
    }

    /**
     * 특정 커스텀 문장 조회
     */
    public CustomSentence getCustomSentence(Long id) {
        String userNickname = SecurityUtils.getCurrentNickname();
        return customSentenceRepository.findByIdAndUserNickname(id, userNickname)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 커스텀 문장입니다."));
    }
}
