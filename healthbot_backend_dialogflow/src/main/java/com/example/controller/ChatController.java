package com.example.controller;

import com.example.model.Intent;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private List<Intent> intents;

    @PostConstruct
    public void loadIntents() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File("src/main/resources/intents.json");
        intents = mapper.readValue(file, new TypeReference<List<Intent>>() {});
    }

    @PostMapping
    public String chat(@RequestParam String message) {
        for (Intent intent : intents) {
            for (String pattern : intent.getPatterns()) {
                if (message.toLowerCase().contains(pattern.toLowerCase())) {
                    List<String> responses = intent.getResponses();
                    return responses.get(new Random().nextInt(responses.size()));
                }
            }
        }
        return "Sorry, I didn't understand that.";
    }
}
