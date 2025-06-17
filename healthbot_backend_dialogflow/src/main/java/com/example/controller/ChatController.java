package com.example.controller;

import com.example.model.Intent;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.*;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*") // Enable CORS for frontend access
public class ChatController {

    private List<Intent> intents;

    @PostConstruct
    public void loadIntents() {
        try {
            // Load from resources (works in local & production)
            InputStream is = new ClassPathResource("intents.json").getInputStream();
            ObjectMapper mapper = new ObjectMapper();
            intents = mapper.readValue(is, new TypeReference<List<Intent>>() {});
        } catch (Exception e) {
            e.printStackTrace();
            intents = new ArrayList<>();
        }
    }

    @PostMapping
    public Map<String, String> chat(@RequestBody Map<String, String> request) {
        String message = request.get("message").toLowerCase();
        for (Intent intent : intents) {
            for (String pattern : intent.getPatterns()) {
                if (message.contains(pattern.toLowerCase())) {
                    List<String> responses = intent.getResponses();
                    String reply = responses.get(new Random().nextInt(responses.size()));
                    return Collections.singletonMap("reply", reply);
                }
            }
        }
        return Collections.singletonMap("reply", "Sorry, I didn't understand that.");
    }
}
