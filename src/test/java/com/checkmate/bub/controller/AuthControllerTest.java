package com.checkmate.bub.controller;

import com.checkmate.bub.auth.controller.AuthController;
import com.checkmate.bub.auth.service.AuthService;
import com.checkmate.bub.util.EnvironmentUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private EnvironmentUtil environmentUtil;

    @Test
    public void testGetKakaoLoginUrl() throws Exception {
        when(authService.getClientId()).thenReturn("test-client-id");
        when(authService.getRedirectUri()).thenReturn("http://localhost:8080/auth/kakao/callback");

        mockMvc.perform(get("/auth/kakao/login-url"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_PLAIN_VALUE))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("test-client-id")));
    }

    @Test
    public void testCheckAuth() throws Exception {
        mockMvc.perform(get("/auth/kakao/api/check-auth"))
                .andExpect(status().isOk())
                .andExpect(content().string("Authenticated"));
    }

    @Test
    public void testLogout() throws Exception {
        when(environmentUtil.isLocalEnvironment()).thenReturn(true);

        mockMvc.perform(post("/auth/kakao/logout"))
                .andExpect(status().isOk())
                .andExpect(content().string("Logout successful"));
    }
}