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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class WebhookService {

    @Autowired
    private WebhookController webhookController;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final AtomicInteger webhookDeliveryCount = new AtomicInteger(0);

    // Thread pool mapped per URL to guarantee sequential order for each receiver
    private final Map<String, ExecutorService> dispatchers = new ConcurrentHashMap<>();

    private ExecutorService getDispatcher(String url) {
        return dispatchers.computeIfAbsent(url, k -> Executors.newSingleThreadExecutor());
    }

    public static int getWebhookDeliveryCount() { return webhookDeliveryCount.get(); }
    public static void incrementWebhookDeliveryCount() { webhookDeliveryCount.incrementAndGet(); }

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

        dispatchToAllWebhooks(payload);
        dispatchToIntegrations(alert, "alert.fired");
    }

    public void sendAlertResolved(Alert alert) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("event", "alert.resolved");
        payload.put("alert_id", alert.getAlertId());
        payload.put("resolved_at", alert.getResolvedAt().toString());

        dispatchToAllWebhooks(payload);
        dispatchToIntegrations(alert, "alert.resolved");
    }

    private void dispatchToAllWebhooks(Map<String, Object> payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            Map<String, String> webhooks = webhookController.getRegisteredWebhooks();

            for (String url : webhooks.values()) {
                getDispatcher(url).submit(() -> sendWithRetry(url, json));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void dispatchToIntegrations(Alert alert, String eventType) {
        try {
            Map<String, Map<String, Object>> integrations = webhookController.getRegisteredIntegrations();

            for (Map<String, Object> integration : integrations.values()) {
                String type = (String) integration.get("type");
                String webhookUrl = (String) integration.get("webhook_url");
                @SuppressWarnings("unchecked")
                List<String> events = (List<String>) integration.get("events");

                if (events == null || !events.contains(eventType)) continue;

                Map<String, Object> payload = null;
                if ("slack".equals(type)) {
                    payload = "alert.fired".equals(eventType) ? SlackFormatter.formatAlertFired(alert) : SlackFormatter.formatAlertResolved(alert);
                } else if ("discord".equals(type)) {
                    payload = "alert.fired".equals(eventType) ? DiscordFormatter.formatAlertFired(alert) : DiscordFormatter.formatAlertResolved(alert);
                }

                if (payload != null) {
                    String json = objectMapper.writeValueAsString(payload);
                    getDispatcher(webhookUrl).submit(() -> sendWithRetry(webhookUrl, json));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void sendWithRetry(String url, String jsonBody) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        boolean success = false;
        long startTime = System.currentTimeMillis();

        // Loop for up to 60 seconds
        while (!success && (System.currentTimeMillis() - startTime) < 60000) {
            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                int status = response.statusCode();
                // Transient errors trigger a retry
                if (status == 500 || status == 502 || status == 503 || status == 504) {
                    Thread.sleep(1000);
                } else {
                    success = true;
                    // Only count true success (2xx) for metrics
                    if (status >= 200 && status < 300) {
                        incrementWebhookDeliveryCount();
                    }
                }
            } catch (Exception e) {
                try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
            }
        }
    }
}