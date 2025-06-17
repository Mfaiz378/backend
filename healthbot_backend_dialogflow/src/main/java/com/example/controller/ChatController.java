package com.example.controller;

import com.example.model.Intent;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.util.*;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    private List<Intent> intents;

    @PostConstruct
    public void loadIntents() {
        try {
            InputStream is = new ClassPathResource("intents.json").getInputStream();
            ObjectMapper mapper = new ObjectMapper();
            intents = mapper.readValue(is, new TypeReference<List<Intent>>() {});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PostMapping
    public Map<String, String> chat(@RequestBody Map<String, String> request) {
        String message = request.get("message");

        for (Intent intent : intents) {
            for (String pattern : intent.getPatterns()) {
                if (message.toLowerCase().contains(pattern.toLowerCase())) {
                    List<String> responses = intent.getResponses();
                    String reply = responses.get(new Random().nextInt(responses.size()));
                    return Map.of("reply", reply);
                }
            }
        }

        return Map.of("reply", "Sorry, I didn't understand that.");
    }
}
