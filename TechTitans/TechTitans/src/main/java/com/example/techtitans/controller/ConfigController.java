package com.example.techtitans.controller;

import com.example.techtitans.dto.ConfigDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/config")
public class ConfigController {

    private ConfigDTO currentConfig = new ConfigDTO(15, 3000);

    public ConfigDTO getCurrentConfig() { return currentConfig; }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getConfig() {
        Map<String, Object> response = new HashMap<>();
        response.put("check_interval_seconds", currentConfig.getCheckIntervalSeconds());
        response.put("request_timeout_ms", currentConfig.getRequestTimeoutMs());
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> updateConfig(@RequestBody Map<String, Integer> request) {
        if (request.containsKey("check_interval_seconds")) {
            currentConfig.setCheckIntervalSeconds(request.get("check_interval_seconds"));
        }
        if (request.containsKey("request_timeout_ms")) {
            currentConfig.setRequestTimeoutMs(request.get("request_timeout_ms"));
        }
        return getConfig();
    }
}