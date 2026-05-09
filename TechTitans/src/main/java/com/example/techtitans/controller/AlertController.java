package com.example.techtitans.controller;

import com.example.techtitans.Entity.Alert;
import com.example.techtitans.Repository.AlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/alerts")
public class AlertController {

    @Autowired
    private AlertRepository alertRepository;

    @GetMapping
    public ResponseEntity<List<Alert>> getAllAlerts() {
        // Fetch all alerts (active and resolved) from Member 4's database table
        List<Alert> alerts = alertRepository.findAll();
        return ResponseEntity.ok(alerts);
    }
}