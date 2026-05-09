package com.example.techtitans.controller;

import com.example.techtitans.Entity.Proxy;
import com.example.techtitans.Repository.ProxyRepository;
import com.example.techtitans.Service.ProxyService;
import com.example.techtitans.dto.ProxyCreateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.techtitans.Entity.CheckHistory;
import java.util.ArrayList;
import java.util.Optional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/proxies")
public class ProxyController {

    @Autowired
    private com.example.techtitans.Repository.CheckHistoryRepository historyRepository;

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

    // ==========================================
    // CHAPTER 06: The Dossier
    // GET /proxies/{id}
    // ==========================================
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getProxyDossier(@PathVariable String id) {
        // 1. Check if the proxy exists. If not, return 404 Not Found per the rules.
        Optional<Proxy> proxyOpt = proxyRepository.findById(id);
        if (proxyOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Proxy proxy = proxyOpt.get();

        // 2. Fetch the background check history for this specific proxy
        List<CheckHistory> history = historyRepository.findByProxyIdOrderByCheckedAtAsc(id);

        // 3. Calculate uptime stats
        long totalChecks = history.size();
        long upChecks = history.stream().filter(h -> "up".equals(h.getStatus())).count();
        double uptimePercentage = totalChecks == 0 ? 0.0 : ((double) upChecks / totalChecks) * 100.0;

        // 4. Format the history array
        List<Map<String, Object>> historyResponse = new ArrayList<>();
        for (CheckHistory h : history) {
            Map<String, Object> hMap = new HashMap<>();
            hMap.put("checked_at", h.getCheckedAt());
            hMap.put("status", h.getStatus());
            historyResponse.add(hMap);
        }

        // 5. Build the final JSON matching the PDF exactly
        Map<String, Object> response = new HashMap<>();
        response.put("id", proxy.getId());
        response.put("url", proxy.getUrl());
        response.put("status", proxy.getStatus());
        response.put("last_checked_at", proxy.getLastCheckedAt());
        response.put("consecutive_failures", proxy.getConsecutiveFailures());
        response.put("total_checks", totalChecks);
        response.put("uptime_percentage", Math.round(uptimePercentage * 10.0) / 10.0); // Round to 1 decimal place
        response.put("history", historyResponse);

        return ResponseEntity.ok(response);
    }

    // ==========================================
    // CHAPTER 07: The Chronicle
    // GET /proxies/{id}/history
    // ==========================================
    @GetMapping("/{id}/history")
    public ResponseEntity<List<Map<String, Object>>> getProxyHistory(@PathVariable String id) {
        // Return 404 if the ID is totally unknown
        if (!proxyRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        List<CheckHistory> history = historyRepository.findByProxyIdOrderByCheckedAtAsc(id);
        List<Map<String, Object>> historyResponse = new ArrayList<>();

        for (CheckHistory h : history) {
            Map<String, Object> hMap = new HashMap<>();
            hMap.put("checked_at", h.getCheckedAt());
            hMap.put("status", h.getStatus());
            historyResponse.add(hMap);
        }

        return ResponseEntity.ok(historyResponse);
    }

    // ==========================================
    // CHAPTER 08: The Graveyard
    // DELETE /proxies
    // ==========================================
    @DeleteMapping
    public ResponseEntity<Void> clearPool() {
        // Because Member 2 added @SQLDelete(sql = "UPDATE proxies SET is_deleted = true...")
        // to the Proxy entity, this will safely hide the proxies from the pool
        // without accidentally wiping out the alerts or history!
        proxyRepository.deleteAll();

        // Return 204 No Content per the rules
        return ResponseEntity.noContent().build();
    }
}