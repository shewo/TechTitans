package com.example.techtitans.Entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "check_history", indexes = {
        @Index(name = "idx_proxy_id", columnList = "proxy_id"),
        @Index(name = "idx_checked_at", columnList = "checked_at")
})
public class CheckHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "proxy_id", nullable = false)
    private String proxyId;

    @Column(nullable = false)
    private String status;

    @Column(name = "checked_at", nullable = false)
    private Instant checkedAt;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getProxyId() { return proxyId; }
    public void setProxyId(String proxyId) { this.proxyId = proxyId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCheckedAt() { return checkedAt; }
    public void setCheckedAt(Instant checkedAt) { this.checkedAt = checkedAt; }
}