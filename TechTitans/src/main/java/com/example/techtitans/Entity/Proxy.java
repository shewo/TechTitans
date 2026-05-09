package com.example.techtitans.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;

@Entity
@Table(name = "proxies")
@Data // This generates getId, setId, etc. automatically
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE proxies SET is_deleted = true WHERE id=?")
@SQLRestriction("is_deleted=false")
public class Proxy {

    @Id
    private String id; // This must be "id" for getId() to work

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

    // Explicit getters for clarity
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
}