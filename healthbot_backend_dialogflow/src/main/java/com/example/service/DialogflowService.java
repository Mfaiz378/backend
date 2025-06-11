package com.example.service;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.dialogflow.v2.*;

import java.io.FileInputStream;

import org.springframework.context.annotation.Configuration;

@Configuration
public class DialogflowService {

       public String detectIntentTexts(String projectId, String sessionId, String text, String languageCode)
        throws Exception {
    GoogleCredentials credentials = GoogleCredentials
            .fromStream(new FileInputStream("C:/Users/NASEEM/Downloads/Healthbot/healthbot_backend_dialogflow/src/main/resources/credentials.json"));
    SessionsSettings sessionsSettings = SessionsSettings.newBuilder()
            .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
            .build();

    try (SessionsClient sessionsClient = SessionsClient.create(sessionsSettings)) {
        SessionName session = SessionName.of(projectId, sessionId);

        QueryInput queryInput;
        if (text == null || text.trim().isEmpty()) {
            // Trigger welcome event
            EventInput eventInput = EventInput.newBuilder()
                    .setName("WELCOME")
                    .setLanguageCode(languageCode)
                    .build();
            queryInput = QueryInput.newBuilder().setEvent(eventInput).build();
        } else {
            TextInput textInput = TextInput.newBuilder().setText(text).setLanguageCode(languageCode).build();
            queryInput = QueryInput.newBuilder().setText(textInput).build();
        }

        DetectIntentRequest request = DetectIntentRequest.newBuilder()
                .setSession(session.toString())
                .setQueryInput(queryInput)
                .build();

        DetectIntentResponse response = sessionsClient.detectIntent(request);
        return response.getQueryResult().getFulfillmentText();
    }
}
}
