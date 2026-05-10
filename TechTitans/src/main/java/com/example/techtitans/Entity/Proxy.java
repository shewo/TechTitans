package com.example.techtitans.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.Instant;

@Entity
@Table(name = "proxies")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Proxy {

    @Id
    private String id;

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    private String status = "pending";

    @Column(name = "last_checked_at")
    private Instant lastCheckedAt;

    @Column(name = "consecutive_failures")
    private int consecutiveFailures = 0;
}