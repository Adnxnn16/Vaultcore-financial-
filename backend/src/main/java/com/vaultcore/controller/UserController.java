package com.vaultcore.controller;

import com.vaultcore.dto.UserResponse;
import com.vaultcore.entity.User;
import com.vaultcore.repository.UserRepository;
import com.vaultcore.aspect.NoAudit;
import com.vaultcore.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserRepository userRepository;
    private final AccountService accountService;

    public UserController(UserRepository userRepository, AccountService accountService) {
        this.userRepository = userRepository;
        this.accountService = accountService;
    }

    @NoAudit
    @GetMapping("/me")
    @Transactional
    public ResponseEntity<UserResponse> getMe(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return ResponseEntity.status(401).build();
        }

        String keycloakId = jwt.getSubject();
        String username = jwt.getClaimAsString("preferred_username");
        String email = jwt.getClaimAsString("email");
        String name = jwt.getClaimAsString("name");
        
        if (name == null) name = username;

        log.info("Identity Sync requested for: {} ({})", username, keycloakId);

        // 1. Try finding by Keycloak ID
        Optional<User> userOpt = userRepository.findByKeycloakId(keycloakId);
        
        // 2. Fallback to Username (for seeded demo users who haven't synced yet)
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                log.info("Mapping existing user {} to Keycloak ID {}", username, keycloakId);
                user.setKeycloakId(keycloakId);
                userRepository.save(user);
            }
        }

        // 3. JIT Provisioning for completely new users
        if (userOpt.isEmpty()) {
            log.info("Provisioning new JIT user: {}", username);
            User newUser = User.builder()
                    .username(username)
                    .email(email != null ? email : username + "@vaultcore.com")
                    .fullName(name)
                    .keycloakId(keycloakId)
                    .role("USER")
                    .active(true)
                    .build();
            
            User savedUser = userRepository.save(newUser);
            // Create initial account with $1,000 Welcome Bonus
            accountService.createWelcomeAccount(savedUser);
            userOpt = Optional.of(savedUser);
        }

        User user = userOpt.get();
        return ResponseEntity.ok(UserResponse.builder()
                .id(user.getId().toString())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .build());
    }
}
