package com.example.techtitans.Service;

import com.example.techtitans.Entity.Alert;
import com.example.techtitans.controller.WebhookController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class WebhookService {

    @Autowired
    private WebhookController webhookController;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void sendAlertFired(Alert alert) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("event", "alert.fired");
        payload.put("alert_id", alert.getAlertId());
        payload.put("fired_at", alert.getFiredAt().toString());
        payload.put("failure_rate", alert.getFailureRate());
        payload.put("total_proxies", alert.getTotalProxies());
        payload.put("failed_proxies", alert.getFailedProxies());
        payload.put("failed_proxy_ids", alert.getFailedProxyIds());
        payload.put("threshold", alert.getThreshold());
        payload.put("message", alert.getMessage());

        dispatchToAll(payload);
    }

    public void sendAlertResolved(Alert alert) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("event", "alert.resolved");
        payload.put("alert_id", alert.getAlertId());
        payload.put("resolved_at", alert.getResolvedAt().toString());

        dispatchToAll(payload);
    }

    private void dispatchToAll(Map<String, Object> payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            Map<String, String> webhooks = webhookController.getRegisteredWebhooks();

            for (String url : webhooks.values()) {
                // Run asynchronously so it doesn't block the monitoring thread
                CompletableFuture.runAsync(() -> sendWithRetry(url, json));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendWithRetry(String url, String jsonBody) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        boolean success = false;
        while (!success) {
            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                int status = response.statusCode();
                // Rule: Retry on 500, 502, 503, 504. Otherwise, consider it complete.
                if (status == 500 || status == 502 || status == 503 || status == 504) {
                    Thread.sleep(1000);
                } else {
                    success = true;
                }
            } catch (Exception e) {
                try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
            }
        }
    }
}