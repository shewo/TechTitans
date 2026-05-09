package com.example.techtitans.controller;

import com.example.techtitans.Entity.Alert;
import com.example.techtitans.Repository.AlertRepository;
import com.example.techtitans.Repository.CheckHistoryRepository;
import com.example.techtitans.Repository.ProxyRepository;
import com.example.techtitans.Service.WebhookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/metrics")
public class MetricsController {

    @Autowired
    private ProxyRepository proxyRepository;

    @Autowired
    private CheckHistoryRepository checkHistoryRepository;

    @Autowired
    private AlertRepository alertRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getMetrics() {

        List<Alert> allAlerts = alertRepository.findAll();
        long activeAlerts = allAlerts.stream().filter(a -> "active".equals(a.getStatus())).count();

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("total_checks", checkHistoryRepository.count());
        metrics.put("current_pool_size", proxyRepository.count());
        metrics.put("active_alerts", activeAlerts);
        metrics.put("total_alerts", allAlerts.size());

        // Track actual webhook deliveries
        metrics.put("webhook_deliveries", WebhookService.getWebhookDeliveryCount());

        return ResponseEntity.ok(metrics);
    }
}