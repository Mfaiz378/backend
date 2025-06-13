package com.example.controller;

import com.example.service.DialogflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

//CrossOrigin(origins = "https://healthbotplus.netlify.app")
// ChatController.java
@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    private List<Intent> intents;

    @PostConstruct
    public void loadIntents() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        InputStream is = getClass().getResourceAsStream("/intents.json");
        intents = Arrays.asList(mapper.readValue(is, Intent[].class));
    }

    @PostMapping
    public Map<String, String> chat(@RequestBody Map<String, String> payload) {
        String userMessage = payload.get("message").toLowerCase();
        for (Intent intent : intents) {
            for (String pattern : intent.getPatterns()) {
                if (userMessage.contains(pattern.toLowerCase())) {
                    String reply = intent.getResponses().get(0); // Choose the first response
                    return Map.of("reply", reply);
                }
            }
        }
        return Map.of("reply", "Sorry, I didn't understand. Please try again.");
    }
}
