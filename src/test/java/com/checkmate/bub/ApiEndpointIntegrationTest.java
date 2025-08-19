package com.checkmate.bub;

import com.checkmate.bub.config.TestConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = BubApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ApiEndpointIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // Test AuthController endpoints
    @Test
    public void testKakaoLoginUrl() throws Exception {
        mockMvc.perform(get("/auth/kakao/login-url"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_PLAIN));
    }

    @Test
    @WithMockUser
    public void testCheckAuth() throws Exception {
        mockMvc.perform(get("/auth/kakao/api/check-auth"))
                .andExpect(status().isOk())
                .andExpect(content().string("Authenticated"));
    }

    @Test
    @WithMockUser
    public void testLogout() throws Exception {
        mockMvc.perform(post("/auth/kakao/logout"))
                .andExpect(status().isOk())
                .andExpect(content().string("Logout successful"));
    }

    // Test UserController endpoints
    @Test
    @WithMockUser(username = "1")
    public void testGetUserCategories() throws Exception {
        mockMvc.perform(get("/api/users/me/categories"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "1") 
    public void testGetUserTones() throws Exception {
        mockMvc.perform(get("/api/users/me/categories/tones"))
                .andExpect(status().isOk());
    }

    // Test AffirmationController endpoints
    @Test
    @WithMockUser(username = "1")
    public void testGetMainAffirmation() throws Exception {
        mockMvc.perform(get("/api/v1/affirmations/main"))
                .andExpect(status().isOk());
    }

    // Test SpeechController endpoints
    @Test
    public void testSpeechRecognitionWithoutFile() throws Exception {
        mockMvc.perform(multipart("/api/v1/speech/recognize")
                .param("originalSentence", "테스트 문장"))
                .andExpect(status().isBadRequest());
    }

    // Test BookmarkController endpoints
    @Test
    public void testGetBookmarks() throws Exception {
        mockMvc.perform(get("/api/v1/bookmarks"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testCheckBookmarkStatus() throws Exception {
        mockMvc.perform(get("/api/v1/bookmarks/check")
                .param("sentence", "테스트 문장"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}