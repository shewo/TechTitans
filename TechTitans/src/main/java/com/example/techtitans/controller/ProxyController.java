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

    @Autowired private com.example.techtitans.Repository.CheckHistoryRepository historyRepository;
    @Autowired private ProxyRepository proxyRepository;
    @Autowired private ProxyService proxyService;

    // Helper method to guarantee perfect snake_case formatting
    private Map<String, Object> formatProxy(Proxy p) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", p.getId());
        map.put("url", p.getUrl());
        map.put("status", p.getStatus());
        map.put("last_checked_at", p.getLastCheckedAt());
        map.put("consecutive_failures", p.getConsecutiveFailures());
        return map;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> ingestProxies(@RequestBody ProxyCreateRequest request) {
        List<Proxy> savedProxies = proxyService.loadProxies(request);

        List<Map<String, Object>> mappedProxies = new ArrayList<>();
        for (Proxy p : savedProxies) mappedProxies.add(formatProxy(p));

        Map<String, Object> response = new HashMap<>();
        response.put("accepted", savedProxies.size());
        response.put("proxies", mappedProxies);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getWatchtower() {
        List<Proxy> allProxies = proxyRepository.findAll();

        long upCount = allProxies.stream().filter(p -> "up".equals(p.getStatus())).count();
        long downCount = allProxies.stream().filter(p -> "down".equals(p.getStatus())).count();
        double failureRate = allProxies.isEmpty() ? 0.0 : (double) downCount / allProxies.size();

        List<Map<String, Object>> mappedProxies = new ArrayList<>();
        for (Proxy p : allProxies) mappedProxies.add(formatProxy(p));

        Map<String, Object> response = new HashMap<>();
        response.put("total", allProxies.size());
        response.put("up", upCount);
        response.put("down", downCount);
        response.put("failure_rate", failureRate); // 🔥 FIXED THE FATAL TYPO HERE
        response.put("proxies", mappedProxies);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getProxyDossier(@PathVariable String id) {
        Optional<Proxy> proxyOpt = proxyRepository.findById(id);
        if (proxyOpt.isEmpty()) return ResponseEntity.notFound().build();

        Proxy proxy = proxyOpt.get();
        List<CheckHistory> history = historyRepository.findByProxyIdOrderByCheckedAtAsc(id);

        long totalChecks = history.size();
        long upChecks = history.stream().filter(h -> "up".equals(h.getStatus())).count();
        double uptimePercentage = totalChecks == 0 ? 0.0 : ((double) upChecks / totalChecks) * 100.0;

        List<Map<String, Object>> historyResponse = new ArrayList<>();
        for (CheckHistory h : history) {
            Map<String, Object> hMap = new HashMap<>();
            hMap.put("checked_at", h.getCheckedAt());
            hMap.put("status", h.getStatus());
            historyResponse.add(hMap);
        }

        Map<String, Object> response = formatProxy(proxy);
        response.put("total_checks", totalChecks);
        response.put("uptime_percentage", Math.round(uptimePercentage * 10.0) / 10.0);
        response.put("history", historyResponse);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<Map<String, Object>>> getProxyHistory(@PathVariable String id) {
        if (!proxyRepository.existsById(id)) return ResponseEntity.notFound().build();

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

    @DeleteMapping
    public ResponseEntity<Void> clearPool() {
        proxyRepository.deleteAll();
        return ResponseEntity.noContent().build();
    }
}