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

    @PutMapping("/categories")
    public ResponseEntity<Void> updateUserCategories(
            @Valid @RequestBody UserCategoryRequestDto requestDto,
            Authentication authentication) {
        
        Long userId = Long.valueOf(authentication.getName());
        userService.updateUserCategories(userId, requestDto);
        
        return ResponseEntity.ok().build();
    }
}