package com.example.techtitans.controller;

import com.example.techtitans.Entity.Proxy;
import com.example.techtitans.Repository.ProxyRepository;
import com.example.techtitans.Service.ProxyService;
import com.example.techtitans.dto.ProxyCreateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/proxies")
public class ProxyController {

    @Autowired
    private ProxyRepository proxyRepository;

    @Autowired
    private ProxyService proxyService; // Connecting to Member 2's logic!

    // Chapter 04: Building the Pool
    @PostMapping
    public ResponseEntity<Map<String, Object>> ingestProxies(@RequestBody ProxyCreateRequest request) {

        // 1. Pass the incoming JSON to the Service your teammate built
        List<Proxy> savedProxies = proxyService.loadProxies(request);

        // 2. Format the response to exactly match Chapter 4 of the PDF
        Map<String, Object> response = new HashMap<>();
        response.put("accepted", savedProxies.size());
        response.put("proxies", savedProxies);

        // 3. Return a 201 Created status (Required by the rules!)
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Chapter 05: The Watchtower
    @GetMapping
    public ResponseEntity<Map<String, Object>> getWatchtower() {

        // 1. Fetch all proxies from the database
        List<Proxy> allProxies = proxyRepository.findAll();

        // 2. Calculate the stats
        long upCount = allProxies.stream().filter(p -> "up".equals(p.getStatus())).count();
        long downCount = allProxies.stream().filter(p -> "down".equals(p.getStatus())).count();
        double failureRate = allProxies.isEmpty() ? 0.0 : (double) downCount / allProxies.size();

        // 3. Format the Watchtower response exactly as requested in Chapter 5
        Map<String, Object> response = new HashMap<>();
        response.put("total", allProxies.size());
        response.put("up", upCount);
        response.put("down", downCount);

        // Note: Because you set snake_case in application.properties,
        // "failureRate" will automatically turn into "failure_rate" in the JSON output!
        response.put("failureRate", failureRate);
        response.put("proxies", allProxies);

        return ResponseEntity.ok(response);
    }
}