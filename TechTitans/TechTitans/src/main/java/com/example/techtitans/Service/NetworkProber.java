package com.example.techtitans.Service;

import org.springframework.stereotype.Component;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Component
public class NetworkProber {

    public String probe(String url, long timeoutMs) {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofMillis(timeoutMs))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofMillis(timeoutMs))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Rule: 2xx means UP, 5xx or others often mean DOWN
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return "up";
            } else {
                return "down";
            }
        } catch (Exception e) {
            // Timeout or Connection error when DOWN
            return "down";
        }
    }
}