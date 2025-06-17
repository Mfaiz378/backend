package com.example.controller;

import com.example.model.Intent;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.util.*;

@RestController
@CrossOrigin(origins = "*") // allow frontend calls
@RequestMapping("/api/chat")
public class ChatController {

    private List<Intent> intents;

    @PostConstruct
    public void loadIntents() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("intents.json");
        if (inputStream == null) throw new RuntimeException("intents.json not found in resources");
        intents = mapper.readValue(inputStream, new TypeReference<List<Intent>>() {});
    }

    @PostMapping
    public Map<String, String> chat(@RequestBody Map<String, String> request) {
        String message = request.get("message");

        for (Intent intent : intents) {
            for (String pattern : intent.getPatterns()) {
                if (message != null && message.toLowerCase().contains(pattern.toLowerCase())) {
                    List<String> responses = intent.getResponses();
                    String reply = responses.get(new Random().nextInt(responses.size()));
                    return Map.of("reply", reply);
                }
            }
        }
        return Map.of("reply", "Sorry, I didn't understand that.");
    }
}
