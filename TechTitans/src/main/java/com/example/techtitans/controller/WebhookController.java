package com.example.techtitans.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
public class WebhookController {

    // Temporary memory to hold registered webhooks.
    // Member 4 will use these URLs later to send the actual alerts.
    private final Map<String, String> registeredWebhooks = new HashMap<>();

    // ==========================================
    // CHAPTER 10: The Messenger
    // POST /webhooks
    // ==========================================
    @PostMapping("/webhooks")
    public ResponseEntity<Map<String, String>> registerWebhook(@RequestBody Map<String, String> request) {
        String url = request.get("url");

        // Generate a random ID like "wh-123"
        String webhookId = "wh-" + UUID.randomUUID().toString().substring(0, 6);
        registeredWebhooks.put(webhookId, url);

        Map<String, String> response = new HashMap<>();
        response.put("webhook_id", webhookId);
        response.put("url", url);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ==========================================
    // CHAPTER 11: The Integration Layer
    // POST /integrations
    // ==========================================
    @PostMapping("/integrations")
    public ResponseEntity<Map<String, Object>> registerIntegration(@RequestBody Map<String, Object> request) {
        // Torch Labs just wants us to accept the Slack/Discord configuration payload.
        // We return 201 Created and echo the payload back.
        return ResponseEntity.status(HttpStatus.CREATED).body(request);
    }
}