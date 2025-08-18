package com.checkmate.bub.user.controller;

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

    private final UserService userService;

    @GetMapping("/categories")
    public ResponseEntity<?> getUserCategories(Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        var userCategories = userService.getUserCategories(userId);
        return ResponseEntity.ok(userCategories);
    }
    
    @PutMapping("/categories")
    public ResponseEntity<Void> updateUserCategories(
            @Valid @RequestBody UserCategoryRequestDto requestDto,
            Authentication authentication) {
        
        Long userId = Long.valueOf(authentication.getName());
        userService.updateUserCategories(userId, requestDto);
        
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/withdraw")
    public ResponseEntity<String> withdrawUser(Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        userService.withdrawUser(userId);
        return ResponseEntity.ok("회원 탈퇴가 완료되었습니다.");
    }
}