package com.example.controller;

import com.example.service.DialogflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@CrossOrigin(origins = "https://healthbot-frontend.netlify.app")
@RestController
@RequestMapping("/api/auth")
public class ChatController {

    @Autowired
    private DialogflowService dialogflowService;

    @PostMapping
    public ResponseEntity<Map<String, String>> chatWithBot(@RequestBody Map<String, String> payload) {
        try {
            String message = payload.get("message");
            String projectId = "healthbot-project"; // <-- Your actual Dialogflow project ID
            String sessionId = payload.getOrDefault("sessionId", UUID.randomUUID().toString());
            String languageCode = "en";

            String response = dialogflowService.detectIntentTexts(projectId, sessionId, message, languageCode);
            Map<String, String> reply = Map.of("reply", response);
            return ResponseEntity.ok(reply);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("reply", "Error: " + e.getMessage()));
        }
    }
}
