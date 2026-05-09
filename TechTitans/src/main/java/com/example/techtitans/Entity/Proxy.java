package com.example.techtitans.Entity;

<<<<<<< Updated upstream
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
=======
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import java.time.Instant;

@Entity
@Table(name = "proxies")
@SQLDelete(sql = "UPDATE proxies SET is_deleted = true WHERE id=?")
@SQLRestriction("is_deleted=false")
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

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getLastCheckedAt() { return lastCheckedAt; }
    public void setLastCheckedAt(Instant lastCheckedAt) { this.lastCheckedAt = lastCheckedAt; }
    public int getConsecutiveFailures() { return consecutiveFailures; }
    public void setConsecutiveFailures(int consecutiveFailures) { this.consecutiveFailures = consecutiveFailures; }
    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { this.isDeleted = deleted; }
>>>>>>> Stashed changes
}