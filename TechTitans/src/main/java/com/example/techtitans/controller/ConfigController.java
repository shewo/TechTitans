package com.example.techtitans.controller;

import com.example.techtitans.dto.ConfigDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/config")
public class ConfigController {

    private ConfigDTO currentConfig = new ConfigDTO(15, 3000);

    // ADDED THIS GETTER:
    public ConfigDTO getCurrentConfig() {
        return currentConfig;
    }

    @GetMapping
    public ResponseEntity<ConfigDTO> getConfig() {
        return ResponseEntity.ok(currentConfig);
    }

    @PostMapping
    public ResponseEntity<ConfigDTO> updateConfig(@RequestBody ConfigDTO newConfig) {
        this.currentConfig = newConfig;
        return ResponseEntity.ok(this.currentConfig);
    }
}