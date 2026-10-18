package com.example.controller;

import com.example.model.PasswordResetToken;
import com.example.model.User;
import com.example.model.VerificationToken;
import com.example.repository.PasswordResetTokenRepository;
import com.example.repository.TokenRepository;
import com.example.repository.UserRepository;
import com.example.service.EmailService;
import com.example.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@CrossOrigin(originPatterns = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
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

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    // 1. Signup Endpoint
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody User user) {
        if (userRepository.findByEmail(user.getEmail()) != null) {
            Map<String, String> err = new HashMap<>();
            err.put("message", "Email is already registered.");
            return ResponseEntity.badRequest().body(err);
        }

        user.setEnabled(true);
        User savedUser = userService.register(user);

        try {
            String token = UUID.randomUUID().toString();
            VerificationToken vt = new VerificationToken(token, savedUser, LocalDateTime.now().plusMinutes(15));
            tokenRepository.save(vt);

            String link = "https://healthbot-backend-service.onrender.com/api/auth/verify?token=" + token;
            emailService.sendSimpleEmail(savedUser.getEmail(), "Verify your email", "Click to verify: " + link);
        } catch (Exception e) {
            System.err.println("Verification token/email creation skipped: " + e.getMessage());
        }

        Map<String, String> res = new HashMap<>();
        res.put("message", "Registration successful! You can now log in.");
        return ResponseEntity.ok(res);
    }

    // 2. Login Endpoint
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> payload) {
        Map<String, Object> response = new HashMap<>();
        String identifier = payload.get("identifier");
        String password = payload.get("password");

        User loggedInUser = userService.login(identifier, password);
        if (loggedInUser == null) {
            response.put("verified", false);
            response.put("message", "Invalid credentials.");
            return ResponseEntity.status(401).body(response);
        }

        if (!loggedInUser.isEnabled()) {
            response.put("verified", false);
            response.put("message", "Please verify your email before logging in.");
            return ResponseEntity.status(403).body(response);
        }

        response.put("verified", true);
        response.put("message", "Login successful!");
        return ResponseEntity.ok(response);
    }

    // 3. Verify Email Endpoint
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

        tokenRepository.delete(verificationToken);
        return ResponseEntity.ok("Email verified successfully! You can now log in.");
    }

    // 4. Forgot Password Endpoint (Generate Token & Email Reset Link)
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        User user = userRepository.findByEmail(email);

        Map<String, String> res = new HashMap<>();
        if (user == null) {
            res.put("message", "If an account exists with this email, a reset link has been sent.");
            return ResponseEntity.ok(res);
        }

        try {
            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = new PasswordResetToken(token, user, LocalDateTime.now().plusMinutes(15));
            passwordResetTokenRepository.save(resetToken);

            String resetLink = "https://healthbotplus.netlify.app/reset-password?token=" + token;
            emailService.sendSimpleEmail(
                user.getEmail(),
                "Password Reset Request",
                "Click the link below to reset your password (valid for 15 minutes):\n" + resetLink
            );
        } catch (Exception e) {
            System.err.println("Password reset token/email creation skipped: " + e.getMessage());
        }

        res.put("message", "If an account exists with this email, a reset link has been sent.");
        return ResponseEntity.ok(res);
    }

    // 5. Reset Password Endpoint (Validate Token & Update Password)
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> payload) {
        String token = payload.get("token");
        String newPassword = payload.get("newPassword");

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token);
        if (resetToken == null) {
            Map<String, String> err = new HashMap<>();
            err.put("message", "Invalid or missing reset token.");
            return ResponseEntity.badRequest().body(err);
        }

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            passwordResetTokenRepository.delete(resetToken);
            Map<String, String> err = new HashMap<>();
            err.put("message", "Reset token has expired. Please request a new one.");
            return ResponseEntity.badRequest().body(err);
        }

        User user = resetToken.getUser();
        user.setPassword(newPassword);
        userRepository.save(user);

        passwordResetTokenRepository.delete(resetToken);

        Map<String, String> res = new HashMap<>();
        res.put("message", "Password has been successfully reset. You can now log in.");
        return ResponseEntity.ok(res);
    }

    // Dev utility
    @DeleteMapping("/delete-all")
    public ResponseEntity<?> deleteAll() {
        passwordResetTokenRepository.deleteAll();
        tokenRepository.deleteAll();
        userRepository.deleteAll();
        return ResponseEntity.ok("All users, verification tokens, and reset tokens deleted.");
    }
}
