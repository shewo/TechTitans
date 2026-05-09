package com.example.techtitans.Service;

import org.springframework.stereotype.Component;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Component
public class NetworkProber {

    // Standard User-Agent to mimic a real browser and bypass basic bot detection
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36";

    public String probe(String url, long timeoutMs) {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofMillis(timeoutMs))
                    .followRedirects(HttpClient.Redirect.NORMAL) // Follow redirects to get accurate status
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofMillis(timeoutMs))
                    .header("User-Agent", USER_AGENT)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // According to challenge rules: 2xx is UP, anything else is DOWN
            return (response.statusCode() >= 200 && response.statusCode() < 300) ? "up" : "down";
        } catch (Exception e) {
            // Log exception if needed and return down for timeouts/connection errors
            return "down";
        }
    }
}