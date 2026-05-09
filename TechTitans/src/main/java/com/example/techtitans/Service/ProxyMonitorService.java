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

@Service
public class ProxyMonitorService {

    @Autowired
    private AlertService alertService;

    @Autowired
    private ProxyRepository proxyRepository;

    @Autowired
    private CheckHistoryRepository historyRepository;

    @Autowired
    private NetworkProber networkProber;

    // ADDED THIS: Connect to the config
    @Autowired
    private ConfigController configController;

    private Instant lastRun = Instant.MIN;

    // Run every 1 second, but gate it dynamically
    @Scheduled(fixedDelay = 1000)
    public void runMonitoringCycle() {
        int intervalSeconds = configController.getCurrentConfig().getCheckIntervalSeconds();
        int timeoutMs = configController.getCurrentConfig().getRequestTimeoutMs();

        // If the interval hasn't passed yet, skip this cycle
        if (Instant.now().isBefore(lastRun.plusSeconds(intervalSeconds))) {
            return;
        }
        lastRun = Instant.now();

        List<Proxy> proxies = proxyRepository.findAll();
        List<CheckHistory> historyBatch = new ArrayList<>();

        for (Proxy proxy : proxies) {
            // Use the DYNAMIC timeout instead of hardcoded 3000!
            String newStatus = networkProber.probe(proxy.getUrl(), timeoutMs);

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
            historyBatch.add(history);
        }

        proxyRepository.saveAll(proxies);
        historyRepository.saveAll(historyBatch);

        alertService.evaluatePoolHealth();
    }
}