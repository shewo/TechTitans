package com.example.techtitans.dto;

public class ConfigDTO {

    private int checkIntervalSeconds;
    private int requestTimeoutMs;

    // Default constructor is strictly required by Spring/Jackson to parse incoming JSON
    public ConfigDTO() {}

    public ConfigDTO(int checkIntervalSeconds, int requestTimeoutMs) {
        this.checkIntervalSeconds = checkIntervalSeconds;
        this.requestTimeoutMs = requestTimeoutMs;
    }

    // --- Getters and Setters ---
    public int getCheckIntervalSeconds() {
        return checkIntervalSeconds;
    }

    public void setCheckIntervalSeconds(int checkIntervalSeconds) {
        this.checkIntervalSeconds = checkIntervalSeconds;
    }

    public int getRequestTimeoutMs() {
        return requestTimeoutMs;
    }

    public void setRequestTimeoutMs(int requestTimeoutMs) {
        this.requestTimeoutMs = requestTimeoutMs;
    }
}