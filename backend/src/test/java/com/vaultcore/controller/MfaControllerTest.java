package com.vaultcore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.reactive.function.client.WebClient;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = MfaController.class)
public class MfaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WebClient webClient;

    @MockBean
    private StringRedisTemplate redisTemplate;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private ValueOperations<String, String> valueOperations;

    @MockBean
    private JwtDecoder jwtDecoder;

    @MockBean
    private ClientRegistrationRepository clientRegistrationRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void testVerifyMfa_Success() throws Exception {
        String tokenPayload = "{ \"sub\": \"123e4567-e89b-12d3-a456-426614174000\" }";
        String otpValue = "123456";
        String hashedOtp = "hashed-123456";
        String userId = "123e4567-e89b-12d3-a456-426614174000";

        when(valueOperations.get("otp:" + userId)).thenReturn(hashedOtp);
        when(passwordEncoder.matches(otpValue, hashedOtp)).thenReturn(true);

        MfaController.MfaVerifyRequest request = new MfaController.MfaVerifyRequest(otpValue);

        mockMvc.perform(post("/api/v1/auth/mfa/verify")
                        .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(jwt -> jwt.subject(userId)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("MFA Verified. You can now proceed with your transfer."));

        verify(redisTemplate, times(1)).delete("otp:" + userId);
        verify(valueOperations, times(1)).set(eq("mfa_verified:" + userId), eq("true"), eq(5L), any());
    }

    @Test
    void testVerifyMfa_InvalidOtp() throws Exception {
        String otpValue = "wrong-otp";
        String hashedOtp = "hashed-123456";
        String userId = "123e4567-e89b-12d3-a456-426614174000";

        when(valueOperations.get("otp:" + userId)).thenReturn(hashedOtp);
        when(passwordEncoder.matches(otpValue, hashedOtp)).thenReturn(false);

        MfaController.MfaVerifyRequest request = new MfaController.MfaVerifyRequest(otpValue);

        mockMvc.perform(post("/api/v1/auth/mfa/verify")
                        .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(jwt -> jwt.subject(userId)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("INVALID_OTP"));

        verify(redisTemplate, never()).delete(anyString());
        verify(valueOperations, never()).set(anyString(), anyString(), anyLong(), any());
    }

    @Test
    @WithMockUser(username = "testuser")
    void testVerifyMfa_NoJwt() throws Exception {
        MfaController.MfaVerifyRequest request = new MfaController.MfaVerifyRequest("123456");

        // Request WITH an authenticated user, but NOT a JWT principal
        mockMvc.perform(post("/api/v1/auth/mfa/verify")
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
