package com.example.techtitans.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
public class WebhookController {

    private final Map<String, String> registeredWebhooks = new HashMap<>();

    // ADDED THIS GETTER:
    public Map<String, String> getRegisteredWebhooks() {
        return registeredWebhooks;
    }

    @PostMapping("/webhooks")
    public ResponseEntity<Map<String, String>> registerWebhook(@RequestBody Map<String, String> request) {
        String url = request.get("url");
        String webhookId = "wh-" + UUID.randomUUID().toString().substring(0, 6);
        registeredWebhooks.put(webhookId, url);

        Map<String, String> response = new HashMap<>();
        response.put("webhook_id", webhookId);
        response.put("url", url);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/integrations")
    public ResponseEntity<Map<String, Object>> registerIntegration(@RequestBody Map<String, Object> request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(request);
    }
}