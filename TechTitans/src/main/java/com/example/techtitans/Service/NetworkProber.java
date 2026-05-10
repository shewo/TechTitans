package com.example.techtitans.Service;

import org.springframework.stereotype.Component;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

@Component
public class NetworkProber {

    // Reuse a single client to prevent socket exhaustion
    private final HttpClient client = HttpClient.newBuilder().build();

    public CompletableFuture<String> probeAsync(String url, long timeoutMs) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMillis(timeoutMs))
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                .thenApply(response -> {
                    int code = response.statusCode();
                    // 2xx is UP, everything else (3xx, 4xx, 5xx) is DOWN
                    if (code >= 200 && code < 300) {
                        return "up";
                    } else {
                        return "down";
                    }
                })
                .exceptionally(e -> "down"); // Timeouts and connection errors go here
    }
}