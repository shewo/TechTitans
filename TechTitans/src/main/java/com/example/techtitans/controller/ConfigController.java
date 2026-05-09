package com.example.techtitans.controller;

import com.example.techtitans.dto.ConfigDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/config")
public class ConfigController {

    // Temporary memory: Initialize with some default values to pass initial GET tests.
    // Later, Member 2 will help you replace this with a database call.
    private ConfigDTO currentConfig = new ConfigDTO(15, 3000);

    @GetMapping
    public ResponseEntity<ConfigDTO> getConfig() {
        return ResponseEntity.ok(currentConfig);
    }

    @PostMapping
    public ResponseEntity<ConfigDTO> updateConfig(@RequestBody ConfigDTO newConfig) {
        // Update the temporary memory
        this.currentConfig = newConfig;

        // Return 200 OK with the newly applied configuration
        return ResponseEntity.ok(this.currentConfig);
    }
}