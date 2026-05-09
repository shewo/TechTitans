package com.example.techtitans.Entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

@Entity
@Data
public class Proxy {
    @Id
    private String proxyId; // eg: "px-001"
    private String url;
    private String status = "pending"; // Default status
    private String lastCheckedAt;
    private int consecutiveFailures = 0;
}