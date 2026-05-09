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

    @Scheduled(fixedDelay = 1000)
    public void runMonitoringCycle() {
        // Fetch dynamic configuration from the ConfigController (Member 2's part)
        int intervalSeconds = configController.getCurrentConfig().getCheckIntervalSeconds();
        int timeoutMs = configController.getCurrentConfig().getRequestTimeoutMs();

        // Ensure the monitor only runs after the configured interval has passed
        if (Instant.now().isBefore(lastRun.plusSeconds(intervalSeconds))) return;
        lastRun = Instant.now();

        List<Proxy> proxies = proxyRepository.findAll();
        if (proxies.isEmpty()) return;

        // Process monitoring tasks in parallel for maximum performance
        List<CheckHistory> finalHistoryBatch = proxies.parallelStream().map(proxy -> {
            String newStatus = networkProber.probe(proxy.getUrl(), timeoutMs);

            // Update proxy entity state safely across threads
            synchronized (proxy) {
                proxy.setStatus(newStatus);
                proxy.setLastCheckedAt(Instant.now());
                if ("down".equals(newStatus)) {
                    proxy.setConsecutiveFailures(proxy.getConsecutiveFailures() + 1);
                } else {
                    proxy.setConsecutiveFailures(0);
                }
            }

            // Create a history record for each check performed
            CheckHistory history = new CheckHistory();
            history.setProxyId(proxy.getId());
            history.setStatus(newStatus);
            history.setCheckedAt(Instant.now());
            return history;
        }).collect(Collectors.toList());

        // Batch save for database efficiency
        proxyRepository.saveAll(proxies);
        historyRepository.saveAll(finalHistoryBatch);

        // Notify AlertService to check if the failure rate exceeds the 20% threshold
        alertService.evaluatePoolHealth();
    }
}