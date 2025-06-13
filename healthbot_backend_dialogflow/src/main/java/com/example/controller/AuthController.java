package com.example.controller;

import com.example.model.User;
import com.example.model.VerificationToken;
import com.example.repository.TokenRepository;
// import com.example.repository.Tokenrepository;
import com.example.repository.UserRepository;
import com.example.service.EmailService;
import java.util.HashMap;
import java.util.Map;
import com.example.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@CrossOrigin(origins = "https://healthbotplus.netlify.app")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private UserRepository userRepository;

    // ✅ 1. Signup Endpoint
    @PostMapping("/signup")
    public String signup(@RequestBody User user) {
        User savedUser = userService.register(user);
        savedUser.setEnabled(false); // disable account initially
        userRepository.save(savedUser);

        // Generate Token
        String token = UUID.randomUUID().toString();
        VerificationToken vt = new VerificationToken(token, savedUser, LocalDateTime.now().plusMinutes(15));
        tokenRepository.save(vt);

        // Send Email
        String link = "https://backend-production-87a2.up.railway.app/api/auth/verify?token=" + token;
        emailService.sendSimpleEmail(savedUser.getEmail(), "Verify your email", "Click to verify: " + link);

        return "Registration successful! Please check your email to verify your account.";
    }

    // ✅ 2. Login Endpoint
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> payload) {
        Map<String, Object> response = new HashMap<>();
        String identifier = payload.get("identifier");
        String password = payload.get("password");
        User loggedInUser = userService.login(identifier, password);
        if (loggedInUser == null) {
            response.put("verified", false);
            response.put("message", "Invalid credentials.");
            return response;
        }
        if (!loggedInUser.isEnabled()) {
            response.put("verified", false);
            response.put("message", "Please verify your email before logging in.");
            return response;
        }
        response.put("verified", true);
        response.put("message", "Login successful!");
        return response;
    }

    // ✅ 3. Verify Email Endpoint
    @GetMapping("/verify")
    public ResponseEntity<?> verifyEmail(@RequestParam("token") String token) {
        VerificationToken verificationToken = tokenRepository.findByToken(token);
        if (verificationToken == null) {
            return ResponseEntity.badRequest().body("Invalid verification token.");
        }
        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Verification token has expired.");
        }
        User user = verificationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);
        // Optionally, delete the token after verification
        tokenRepository.delete(verificationToken);
        return ResponseEntity.ok("Email verified successfully! You can now log in.");
    }

    // WARNING: For development/testing only! Remove before deploying to production.
    @DeleteMapping("/delete-all")
    public ResponseEntity<?> deleteAll() {
        tokenRepository.deleteAll();
        userRepository.deleteAll();
        return ResponseEntity.ok("All users and verification tokens deleted.");
    }
}
