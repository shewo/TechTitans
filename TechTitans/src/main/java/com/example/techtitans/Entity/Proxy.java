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
}