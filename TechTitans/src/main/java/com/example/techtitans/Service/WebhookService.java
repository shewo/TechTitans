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
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class WebhookService {

    @Autowired
    private WebhookController webhookController;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // Track total successful webhook deliveries
    private static final AtomicInteger webhookDeliveryCount = new AtomicInteger(0);

    public static int getWebhookDeliveryCount() {
        return webhookDeliveryCount.get();
    }

    public static void incrementWebhookDeliveryCount() {
        webhookDeliveryCount.incrementAndGet();
    }

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

        // Dispatch to regular webhooks (blocking to maintain order)
        dispatchToAllWebhooks(payload);
        
        // Dispatch to integrations (Slack/Discord)
        dispatchToIntegrations(alert, "alert.fired");
    }

    public void sendAlertResolved(Alert alert) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("event", "alert.resolved");
        payload.put("alert_id", alert.getAlertId());
        payload.put("resolved_at", alert.getResolvedAt().toString());

        // Dispatch to regular webhooks (blocking to maintain order)
        dispatchToAllWebhooks(payload);
        
        // Dispatch to integrations (Slack/Discord)
        dispatchToIntegrations(alert, "alert.resolved");
    }

    private void dispatchToAllWebhooks(Map<String, Object> payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            Map<String, String> webhooks = webhookController.getRegisteredWebhooks();

            for (String url : webhooks.values()) {
                // Block here to ensure order - don't use CompletableFuture
                sendWithRetry(url, json);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void dispatchToIntegrations(Alert alert, String eventType) {
        try {
            Map<String, Map<String, Object>> integrations = webhookController.getRegisteredIntegrations();
            
            for (Map<String, Object> integration : integrations.values()) {
                String type = (String) integration.get("type");
                String webhookUrl = (String) integration.get("webhook_url");
                List<String> events = (List<String>) integration.get("events");
                
                if (events == null || !events.contains(eventType)) {
                    continue;
                }
                
                Map<String, Object> payload = null;
                if ("slack".equals(type)) {
                    if ("alert.fired".equals(eventType)) {
                        payload = SlackFormatter.formatAlertFired(alert);
                    } else {
                        payload = SlackFormatter.formatAlertResolved(alert);
                    }
                } else if ("discord".equals(type)) {
                    if ("alert.fired".equals(eventType)) {
                        payload = DiscordFormatter.formatAlertFired(alert);
                    } else {
                        payload = DiscordFormatter.formatAlertResolved(alert);
                    }
                }
                
                if (payload != null) {
                    String json = objectMapper.writeValueAsString(payload);
                    sendWithRetry(webhookUrl, json);
                }
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
        int retries = 0;
        while (!success && retries < 60) { // Max 60 seconds of retries
            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                int status = response.statusCode();
                // Rule: Retry on 500, 502, 503, 504. Otherwise, consider it complete.
                if (status == 500 || status == 502 || status == 503 || status == 504) {
                    Thread.sleep(1000);
                    retries++;
                } else {
                    success = true;
                    // Increment delivery count on any successful HTTP response
                    incrementWebhookDeliveryCount();
                }
            } catch (Exception e) {
                try { 
                    Thread.sleep(1000); 
                    retries++;
                } catch (InterruptedException ignored) {}
            }
        }
    }
}