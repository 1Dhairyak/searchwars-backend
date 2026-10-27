package com.higherlower.game.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.higherlower.game.config.GoogleTokenVerifier;
import com.higherlower.game.dto.request.GoogleTokenRequest;
import com.higherlower.game.dto.request.LoginRequest;
import com.higherlower.game.dto.request.RegisterRequest;
import com.higherlower.game.entity.User;
import com.higherlower.game.repository.UserRepository;
import com.higherlower.game.service.PlayerNameService;
import com.higherlower.game.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    @Autowired private UserRepository userRepo;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private PasswordEncoder encoder;
    @Autowired private GoogleTokenVerifier googleVerifier;
    @Autowired private PlayerNameService playerNameService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequest dto) {
        String email = normalize(dto.email());
        if (userRepo.existsByEmailIgnoreCase(email))
            return ResponseEntity.status(409).body(Map.of("error", "Email already registered"));

        User u = new User();
        u.setEmail(email);
        u.setPasswordHash(encoder.encode(dto.password()));
        u.setDisplayName(dto.displayName() == null ? null : dto.displayName().trim());
        userRepo.save(u);

        return ResponseEntity.ok(authBody(u, dto.guestToken()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest dto) {
        var userOpt = userRepo.findByEmailIgnoreCase(normalize(dto.email()));
        if (userOpt.isEmpty() || userOpt.get().getPasswordHash() == null
                || !encoder.matches(dto.password(), userOpt.get().getPasswordHash())) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid email or password"));
        }
        return ResponseEntity.ok(authBody(userOpt.get(), dto.guestToken()));
    }

    @PostMapping("/google")
    public ResponseEntity<?> googleAuth(@RequestBody @Valid GoogleTokenRequest dto) {
        GoogleIdToken.Payload payload;
        try {
            payload = googleVerifier.verify(dto.idToken());
        } catch (RuntimeException e) {
            log.warn("Google token rejected: {}", e.getMessage());
            return ResponseEntity.status(401).body(Map.of("error", "Invalid Google token"));
        }
        if (payload.getEmail() == null || !Boolean.TRUE.equals(payload.getEmailVerified())) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid Google token"));
        }
        String email = normalize(payload.getEmail());

        User u = userRepo.findByEmailIgnoreCase(email).orElseGet(() -> {
            User nu = new User();
            nu.setEmail(email);
            nu.setGoogleId(payload.getSubject());
            nu.setDisplayName((String) payload.get("name"));
            return userRepo.save(nu);
        });

        return ResponseEntity.ok(authBody(u, dto.guestToken()));
    }

    private static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    /** HashMap because displayName / playerName may be null (Map.of rejects nulls). */
    private Map<String, Object> authBody(User u, String guestToken) {
        Map<String, Object> body = new HashMap<>();
        body.put("token", jwtUtil.generate(u.getId(), u.getEmail()));
        body.put("email", u.getEmail());
        body.put("displayName", u.getDisplayName());
        body.put("playerName", playerNameService.adoptGuestName(u.getId(), guestToken).orElse(null));
        return body;
    }
}
