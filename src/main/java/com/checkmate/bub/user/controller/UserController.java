package com.checkmate.bub.user.controller;

import com.checkmate.bub.affirmation.dto.MainAffirmationResponseDto;
import com.checkmate.bub.affirmation.service.AffirmationService;
import com.checkmate.bub.user.dto.ToneUpdateRequestDto;
import com.checkmate.bub.user.dto.UserCategoryRequestDto;
import com.checkmate.bub.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class UserController {

    //* This Controller is used for my-page view.

    private final UserService userService;
    private final AffirmationService affirmationService;

    // (1) 안녕하세요, {이름} 님 페이지 - 카테고리 조회
    @GetMapping("/categories")
    public ResponseEntity<?> getUserCategories(Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        var userCategories = userService.getUserCategories(userId);
        return ResponseEntity.ok(userCategories);
    }

    // (2) 문제 수정하기 - 문제 선택 및 저장
    @PutMapping("/categories/problems")
    public ResponseEntity<MainAffirmationResponseDto> updateUserProblems(
            @Valid @RequestBody UserCategoryRequestDto requestDto,
            Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        userService.updateUserProblems(userId, requestDto.getCategoryIds());

        // 문제 수정 이후 새로운 확언 문구 생성
        MainAffirmationResponseDto affirmations = affirmationService.generateMainAffirmation(userId);
        return ResponseEntity.ok(affirmations);
    }

    // (3) 톤 조회 (톤 수정 페이지용)
    @GetMapping("/categories/tones")
    public ResponseEntity<?> getUserTones(Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        var userTones = userService.getUserTones(userId);
        return ResponseEntity.ok(userTones);
    }

    // (4) 톤 수정하기 - 톤 저장
    @PutMapping("/categories/tones")
    public ResponseEntity<MainAffirmationResponseDto> updateUserTones(
            @Valid @RequestBody ToneUpdateRequestDto requestDto,
            Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        userService.updateUserTone(userId, requestDto.getToneName());
        // 톤 수정 이후 새로운 확언 문구 생성
        MainAffirmationResponseDto affirmations = affirmationService.generateMainAffirmation(userId);
        return ResponseEntity.ok(affirmations);
    }

    // 회원 탈퇴
    @DeleteMapping("/withdraw")
    public ResponseEntity<String> withdrawUser(Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        userService.withdrawUser(userId);
        return ResponseEntity.ok("회원 탈퇴가 완료되었습니다.");
    }
}