package com.example.techtitans.controller;

import com.example.techtitans.Entity.Proxy;
import com.example.techtitans.Repository.ProxyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/proxies")
public class ProxyController {

    @Autowired
    private ProxyRepository proxyRepository;

    // 1. add proxy (Chapter 04: Building the Pool)
    // sent touch labs from proxy
    @PostMapping
    public ResponseEntity<?> addProxy(@RequestBody Proxy proxy) {
        // new = pending
        proxy.setStatus("pending");
        proxy.setConsecutiveFailures(0);

        Proxy savedProxy = proxyRepository.save(proxy);
        return ResponseEntity.ok(savedProxy);
    }

    // 2. check proxies states (Chapter 05: The Watchtower)
    //Background Engine update Status
    @GetMapping
    public ResponseEntity<List<Proxy>> getAllProxies() {
        List<Proxy> proxies = proxyRepository.findAll();
        return ResponseEntity.ok(proxies);
    }

    // 3. Pool (Optional - For debugging)
    @GetMapping("/summary")
    public ResponseEntity<?> getPoolSummary() {
        List<Proxy> all = proxyRepository.findAll();
        long upCount = all.stream().filter(p -> p.getStatus().equals("up")).count();
        long downCount = all.stream().filter(p -> p.getStatus().equals("down")).count();

        Map<String, Object> summary = new HashMap<>();
        summary.put("total_proxies", all.size());
        summary.put("up", upCount);
        summary.put("down", downCount);
        summary.put("failure_rate", all.isEmpty() ? 0 : (double) downCount / all.size());

        return ResponseEntity.ok(summary);
    }
}