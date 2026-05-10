package com.example.techtitans.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
public class WebhookController {

    private final Map<String, String> registeredWebhooks = new HashMap<>();
    private final Map<String, Map<String, Object>> registeredIntegrations = new HashMap<>();

    public Map<String, String> getRegisteredWebhooks() {
        return registeredWebhooks;
    }

    public Map<String, Map<String, Object>> getRegisteredIntegrations() {
        return registeredIntegrations;
    }

    @PostMapping("/webhooks")
    public ResponseEntity<Map<String, String>> registerWebhook(@RequestBody Map<String, String> request) {
        String url = request.get("url");
        
        // Validate URL format
        if (url == null || url.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            new URI(url);
        } catch (URISyntaxException e) {
            return ResponseEntity.badRequest().build();
        }

        String webhookId = "wh-" + UUID.randomUUID().toString().substring(0, 6);
        registeredWebhooks.put(webhookId, url);

        Map<String, String> response = new HashMap<>();
        response.put("webhook_id", webhookId);
        response.put("url", url);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/integrations")
    public ResponseEntity<Map<String, Object>> registerIntegration(@RequestBody Map<String, Object> request) {
        String type = (String) request.get("type");
        String webhookUrl = (String) request.get("webhook_url");
        
        // Validate required fields
        if (type == null || webhookUrl == null) {
            return ResponseEntity.badRequest().build();
        }
        
        if (!("slack".equals(type) || "discord".equals(type))) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            new URI(webhookUrl);
        } catch (URISyntaxException e) {
            return ResponseEntity.badRequest().build();
        }

        String integrationId = "int-" + UUID.randomUUID().toString().substring(0, 8);
        
        // Store the entire integration config
        Map<String, Object> integration = new HashMap<>(request);
        registeredIntegrations.put(integrationId, integration);

        Map<String, Object> response = new HashMap<>();
        response.put("integration_id", integrationId);
        response.putAll(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}