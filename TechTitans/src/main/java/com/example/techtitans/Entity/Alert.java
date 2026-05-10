package com.example.techtitans.Entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "alerts")
public class Alert {

    @Id
    @Column(name = "alert_id")
    private String alertId;

    @Column(nullable = false)
    private String status = "active";

    @Column(name = "failure_rate")
    private double failureRate;

    @Column(name = "total_proxies")
    private int totalProxies;

    @Column(name = "failed_proxies")
    private int failedProxies;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "alert_failed_proxies", joinColumns = @JoinColumn(name = "alert_id"))
    @Column(name = "proxy_id")
    private List<String> failedProxyIds;

    private double threshold = 0.20;

    @Column(name = "fired_at", nullable = false)
    private Instant firedAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    private String message;

    // Getters and Setters
    public String getAlertId() { return alertId; }
    public void setAlertId(String alertId) { this.alertId = alertId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public double getFailureRate() { return failureRate; }
    public void setFailureRate(double failureRate) { this.failureRate = failureRate; }
    public int getTotalProxies() { return totalProxies; }
    public void setTotalProxies(int totalProxies) { this.totalProxies = totalProxies; }
    public int getFailedProxies() { return failedProxies; }
    public void setFailedProxies(int failedProxies) { this.failedProxies = failedProxies; }
    public List<String> getFailedProxyIds() { return failedProxyIds; }
    public void setFailedProxyIds(List<String> failedProxyIds) { this.failedProxyIds = failedProxyIds; }
    public double getThreshold() { return threshold; }
    public void setThreshold(double threshold) { this.threshold = threshold; }
    public Instant getFiredAt() { return firedAt; }
    public void setFiredAt(Instant firedAt) { this.firedAt = firedAt; }
    public Instant getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(Instant resolvedAt) { this.resolvedAt = resolvedAt; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}