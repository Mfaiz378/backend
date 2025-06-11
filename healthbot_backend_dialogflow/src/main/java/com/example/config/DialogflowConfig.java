package com.example.config;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.dialogflow.v2.SessionsClient;
import com.google.cloud.dialogflow.v2.SessionsSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

import java.io.InputStream;

@Configuration
public class DialogflowConfig {

    @Value("${dialogflow.credentials.path}")
    private String credentialsPath;

    @Bean
    public SessionsClient sessionsClient() throws Exception {
        InputStream stream = getClass().getClassLoader().getResourceAsStream(credentialsPath.replace("classpath:", ""));
        GoogleCredentials credentials = GoogleCredentials.fromStream(stream);

        SessionsSettings sessionsSettings = SessionsSettings.newBuilder()
                .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                .build();

        return SessionsClient.create(sessionsSettings);
    }
}