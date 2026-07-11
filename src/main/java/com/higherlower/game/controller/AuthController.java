package com.higherlower.game.controller;

import com.higherlower.game.config.GoogleTokenVerifier;
import com.higherlower.game.dto.request.GoogleTokenRequest;
import com.higherlower.game.dto.request.LoginRequest;
import com.higherlower.game.dto.request.RegisterRequest;
import com.higherlower.game.entity.User;
import com.higherlower.game.repository.UserRepository;
import com.higherlower.game.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired private UserRepository userRepo;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private PasswordEncoder encoder;
    @Autowired private GoogleTokenVerifier googleVerifier;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest dto) {
        if (userRepo.existsByEmail(dto.email()))
            return ResponseEntity.status(409).body(Map.of("error", "Email already registered"));

        User u = new User();
        u.setEmail(dto.email());
        u.setPasswordHash(encoder.encode(dto.password()));
        u.setDisplayName(dto.displayName());
        userRepo.save(u);

        return ResponseEntity.ok(Map.of(
            "token", jwtUtil.generate(u.getId(), u.getEmail()),
            "email", u.getEmail(),
            "displayName", u.getDisplayName()
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest dto) {
        var userOpt = userRepo.findByEmail(dto.email());
        if (userOpt.isEmpty() || userOpt.get().getPasswordHash() == null
                || !encoder.matches(dto.password(), userOpt.get().getPasswordHash())) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid email or password"));
        }
        User u = userOpt.get();
        return ResponseEntity.ok(Map.of(
            "token", jwtUtil.generate(u.getId(), u.getEmail()),
            "email", u.getEmail(),
            "displayName", u.getDisplayName()
        ));
    }

    @PostMapping("/google")
    public ResponseEntity<?> googleAuth(@RequestBody GoogleTokenRequest dto) {
        var payload = googleVerifier.verify(dto.idToken());
        String email = payload.getEmail();

        User u = userRepo.findByEmail(email).orElseGet(() -> {
            User nu = new User();
            nu.setEmail(email);
            nu.setGoogleId(payload.getSubject());
            nu.setDisplayName((String) payload.get("name"));
            return userRepo.save(nu);
        });

        return ResponseEntity.ok(Map.of(
            "token", jwtUtil.generate(u.getId(), u.getEmail()),
            "email", u.getEmail(),
            "displayName", u.getDisplayName()
        ));
    }
}
