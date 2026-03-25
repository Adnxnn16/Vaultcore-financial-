package com.vaultcore.controller;

import com.vaultcore.dto.ApiResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.vaultcore.aspect.NoAudit;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final WebClient webClient;

    @Value("${KEYCLOAK_ISSUER_URI:http://localhost:9090/realms/vaultcore}")
    private String keycloakIssuerUri;

    @Value("${KEYCLOAK_CLIENT_ID:vaultcore-app}")
    private String keycloakClientId;

    @Value("${KEYCLOAK_CLIENT_SECRET:vaultcore-secret}")
    private String keycloakClientSecret;

    public AuthController(WebClient webClient) {
        this.webClient = webClient;
    }

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password
    ) {}

    public record KeycloakTokenResponse(
            String access_token,
            Integer expires_in,
            String refresh_token,
            Integer refresh_expires_in,
            String token_type
    ) {}

    public record RegisterRequest(
            @NotBlank @Size(max = 60) String firstName,
            @NotBlank @Size(max = 60) String lastName,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8, max = 128) String password
    ) {}

    public record KeycloakAccessTokenResponse(String access_token) {}

    public record KeycloakCredential(String type, String value, Boolean temporary) {}

    public record KeycloakCreateUserRequest(
            String username,
            String email,
            String firstName,
            String lastName,
            Boolean enabled,
            Boolean emailVerified,
            List<KeycloakCredential> credentials
    ) {}

    @NoAudit
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest request) {
        String tokenEndpoint = keycloakIssuerUri + "/protocol/openid-connect/token";
        String adminUsersEndpoint = keycloakIssuerUri.replace("/realms/vaultcore", "/admin/realms/vaultcore/users");

        KeycloakAccessTokenResponse adminToken = webClient.post()
                .uri(tokenEndpoint)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "client_credentials")
                        .with("client_id", keycloakClientId)
                        .with("client_secret", keycloakClientSecret))
                .retrieve()
                .bodyToMono(KeycloakAccessTokenResponse.class)
                .timeout(Duration.ofSeconds(10))
                .onErrorResume(ex -> Mono.empty())
                .blockOptional()
                .orElse(null);

        if (adminToken == null || adminToken.access_token() == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(ApiResponse.<Void>builder()
                            .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                            .error("REGISTRATION_UNAVAILABLE")
                            .message("Unable to initialize registration")
                            .timestamp(LocalDateTime.now())
                            .data(null)
                            .build());
        }

        KeycloakCreateUserRequest createUser = new KeycloakCreateUserRequest(
                request.email().toLowerCase(),
                request.email().toLowerCase(),
                request.firstName().trim(),
                request.lastName().trim(),
                true,
                true,
                List.of(new KeycloakCredential("password", request.password(), false))
        );

        try {
            webClient.post()
                    .uri(adminUsersEndpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(h -> h.setBearerAuth(adminToken.access_token()))
                    .bodyValue(createUser)
                    .retrieve()
                    .toBodilessEntity()
                    .timeout(Duration.ofSeconds(10))
                    .block();
        } catch (WebClientResponseException ex) {
            if (ex.getStatusCode() == HttpStatus.CONFLICT) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(ApiResponse.<Void>builder()
                                .status(HttpStatus.CONFLICT.value())
                                .error("EMAIL_EXISTS")
                                .message("An account with this email already exists")
                                .timestamp(LocalDateTime.now())
                                .data(null)
                                .build());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<Void>builder()
                            .status(HttpStatus.BAD_REQUEST.value())
                            .error("REGISTRATION_FAILED")
                            .message("Registration failed. Please verify your details and try again.")
                            .timestamp(LocalDateTime.now())
                            .data(null)
                            .build());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Registration successful", null));
    }

    @NoAudit
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        String tokenEndpoint = keycloakIssuerUri + "/protocol/openid-connect/token";

        KeycloakTokenResponse tokens = webClient.post()
                .uri(tokenEndpoint)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "password")
                        .with("client_id", keycloakClientId)
                        .with("client_secret", keycloakClientSecret)
                        .with("username", request.email())
                        .with("password", request.password()))
                .retrieve()
                .bodyToMono(KeycloakTokenResponse.class)
                .timeout(Duration.ofSeconds(10))
                .onErrorResume(ex -> Mono.empty())
                .blockOptional()
                .orElse(null);

        if (tokens == null || tokens.refresh_token() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.<Map<String, Object>>builder()
                            .status(HttpStatus.UNAUTHORIZED.value())
                            .error("AUTH_FAILED")
                            .message("Keycloak login failed")
                            .timestamp(LocalDateTime.now())
                            .data(null)
                            .build());
        }

        setRefreshCookie(response, tokens.refresh_token(), tokens.refresh_expires_in());

        return ResponseEntity.ok(ApiResponse.ok("Login successful",
                Map.of("accessToken", tokens.access_token(), "expiresIn", tokens.expires_in())));
    }

    @NoAudit
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Map<String, Object>>> refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String refreshToken = readCookieValue(request, "refresh_token");
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.<Map<String, Object>>builder()
                            .status(HttpStatus.UNAUTHORIZED.value())
                            .error("REFRESH_TOKEN_MISSING")
                            .message("Refresh token cookie missing")
                            .timestamp(LocalDateTime.now())
                            .data(null)
                            .build());
        }

        String tokenEndpoint = keycloakIssuerUri + "/protocol/openid-connect/token";

        KeycloakTokenResponse tokens = webClient.post()
                .uri(tokenEndpoint)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "refresh_token")
                        .with("client_id", keycloakClientId)
                        .with("client_secret", keycloakClientSecret)
                        .with("refresh_token", refreshToken))
                .retrieve()
                .bodyToMono(KeycloakTokenResponse.class)
                .timeout(Duration.ofSeconds(10))
                .onErrorResume(ex -> Mono.empty())
                .blockOptional()
                .orElse(null);

        if (tokens == null || tokens.refresh_token() == null) {
            clearRefreshCookie(response);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.<Map<String, Object>>builder()
                            .status(HttpStatus.UNAUTHORIZED.value())
                            .error("REFRESH_FAILED")
                            .message("Keycloak refresh failed")
                            .timestamp(LocalDateTime.now())
                            .data(null)
                            .build());
        }

        setRefreshCookie(response, tokens.refresh_token(), tokens.refresh_expires_in());

        return ResponseEntity.ok(ApiResponse.ok("Token refreshed",
                Map.of("accessToken", tokens.access_token(), "expiresIn", tokens.expires_in())));
    }

    @NoAudit
    @DeleteMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String refreshToken = readCookieValue(request, "refresh_token");

        if (refreshToken != null && !refreshToken.isBlank()) {
            String revokeEndpoint = keycloakIssuerUri + "/protocol/openid-connect/revoke";

            webClient.post()
                    .uri(revokeEndpoint)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData("client_id", keycloakClientId)
                            .with("client_secret", keycloakClientSecret)
                            .with("token", refreshToken))
                    .retrieve()
                    .toBodilessEntity()
                    .timeout(Duration.ofSeconds(10))
                    .onErrorResume(ex -> Mono.empty())
                    .block();
        }

        clearRefreshCookie(response);

        return ResponseEntity.ok(ApiResponse.ok("Logged out", null));
    }

    private void setRefreshCookie(HttpServletResponse response, String refreshToken, Integer refreshExpiresInSeconds) {
        int maxAgeSeconds = refreshExpiresInSeconds != null ? refreshExpiresInSeconds : 7 * 24 * 60 * 60;
        Cookie cookie = new Cookie("refresh_token", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(maxAgeSeconds);
        response.addCookie(cookie);
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("refresh_token", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private String readCookieValue(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName())) return cookie.getValue();
        }
        return null;
    }
}
