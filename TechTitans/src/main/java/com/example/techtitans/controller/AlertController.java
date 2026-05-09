package com.example.techtitans.controller;

import com.example.techtitans.Entity.Alert;
import com.example.techtitans.Repository.AlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/alerts")
public class AlertController {

    @Autowired
    private AlertRepository alertRepository;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllAlerts() {
        List<Alert> alerts = alertRepository.findAll();
        List<Map<String, Object>> response = new ArrayList<>();

        for (Alert a : alerts) {
            Map<String, Object> map = new HashMap<>();
            map.put("alert_id", a.getAlertId());
            map.put("status", a.getStatus());
            map.put("failure_rate", a.getFailureRate());
            map.put("total_proxies", a.getTotalProxies());
            map.put("failed_proxies", a.getFailedProxies());
            map.put("failed_proxy_ids", a.getFailedProxyIds());
            map.put("threshold", a.getThreshold());
            map.put("fired_at", a.getFiredAt());
            map.put("resolved_at", a.getResolvedAt());
            map.put("message", a.getMessage());
            response.add(map);
        }
        return ResponseEntity.ok(response);
    }
}