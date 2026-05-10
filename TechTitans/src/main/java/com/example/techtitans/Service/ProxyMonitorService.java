package com.example.techtitans.Service;

import com.example.techtitans.Entity.Proxy;
import com.example.techtitans.Entity.CheckHistory;
import com.example.techtitans.Repository.ProxyRepository;
import com.example.techtitans.Repository.CheckHistoryRepository;
import com.example.techtitans.controller.ConfigController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProxyMonitorService {

    @Autowired private AlertService alertService;
    @Autowired private ProxyRepository proxyRepository;
    @Autowired private CheckHistoryRepository historyRepository;
    @Autowired private NetworkProber networkProber;
    @Autowired private ConfigController configController;

    private Instant lastRun = Instant.MIN;

    // Run frequently (every 500ms) to respect config changes immediately
    @Scheduled(fixedDelay = 500)
    public void runMonitoringCycle() {
        // Read config fresh each cycle - respects immediate changes
        int intervalSeconds = configController.getCurrentConfig().getCheckIntervalSeconds();
        int timeoutMs = configController.getCurrentConfig().getRequestTimeoutMs();

        // Check if enough time has passed since last run
        if (Instant.now().isBefore(lastRun.plusSeconds(intervalSeconds))) {
            return;
        }
        lastRun = Instant.now();

        List<Proxy> proxies = proxyRepository.findAll();
        if (proxies.isEmpty()) return;

        // Check all proxies in parallel for speed
        List<CheckHistory> finalHistoryBatch = proxies.parallelStream().map(proxy -> {
            String newStatus = networkProber.probe(proxy.getUrl(), timeoutMs);

            // Update consecutive failures tracking
            if ("down".equals(newStatus)) {
                proxy.setConsecutiveFailures(proxy.getConsecutiveFailures() + 1);
            } else {
                proxy.setConsecutiveFailures(0);
            }
            proxy.setStatus(newStatus);
            proxy.setLastCheckedAt(Instant.now());

            CheckHistory history = new CheckHistory();
            history.setProxyId(proxy.getId());
            history.setStatus(newStatus);
            history.setCheckedAt(Instant.now());
            return history;
        }).collect(Collectors.toList());

        proxyRepository.saveAll(proxies);
        historyRepository.saveAll(finalHistoryBatch);

        // Evaluate alerts after each cycle
        alertService.evaluatePoolHealth();
    }
}