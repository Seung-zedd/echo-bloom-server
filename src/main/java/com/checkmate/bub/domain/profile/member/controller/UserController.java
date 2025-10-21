package com.checkmate.bub.domain.profile.member.controller;

import com.checkmate.bub.domain.affirmation.dto.MainAffirmationResponseDto;
import com.checkmate.bub.domain.affirmation.service.AffirmationService;
import com.checkmate.bub.domain.profile.member.dto.CategorySelectionDto;
import com.checkmate.bub.domain.profile.member.dto.MyPageCategoryResponseDto;
import com.checkmate.bub.domain.profile.member.dto.ToneUpdateRequestDto;
import com.checkmate.bub.domain.profile.member.dto.UserCategoryRequestDto;
import com.checkmate.bub.domain.profile.member.service.UserService;
import com.checkmate.bub.global.util.helper.CookieHelper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class UserController {

    //* This Controller is used for my-page view.

    private final UserService userService;
    private final AffirmationService affirmationService;
    private final CookieHelper cookieHelper;

    // 현재 로그인된 사용자 정보 조회 (JavaScript에서 사용)
    @GetMapping("/info")
    public ResponseEntity<Map<String, String>> getCurrentUserInfo(Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName()); // JWT에서 userId 추출
        String nickname = userService.getUserNickname(userId); // 실제 닉네임 조회
        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("nickname", nickname);
        return ResponseEntity.ok(userInfo);
    }

    // (1) 안녕하세요, {이름} 님 페이지 - 카테고리 조회
    @GetMapping("/categories")
    public ResponseEntity<MyPageCategoryResponseDto> getUserCategories(Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        List<CategorySelectionDto> problems = userService.getUserProblems(userId);
        List<CategorySelectionDto> limitedProblems = problems.stream().limit(3).toList();
        CategorySelectionDto tone = userService.getUserTone(userId);
        MyPageCategoryResponseDto response = new MyPageCategoryResponseDto(limitedProblems, tone);
        return ResponseEntity.ok(response);
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
    public ResponseEntity<String> withdrawUser(Authentication authentication, HttpServletResponse response) {
        Long userId = Long.valueOf(authentication.getName());

        // 1. 사용자 데이터 삭제 (UserService에서 처리)
        userService.withdrawUser(userId);

        // 2. JWT 쿠키 삭제 (CookieHelper 사용)
        cookieHelper.clearAuthCookies(response);

        return ResponseEntity.ok("회원 탈퇴가 완료되었습니다.");
    }
}
