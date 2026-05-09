package com.example.techtitans.Service;

import com.example.techtitans.Entity.Proxy;
import com.example.techtitans.Entity.CheckHistory;
import com.example.techtitans.Repository.ProxyRepository;
import com.example.techtitans.Repository.CheckHistoryRepository;
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
    private NetworkProber networkProber; // Bringing in the real ping tool!

    // Runs continuously in the background
    @Scheduled(fixedDelayString = "#{@systemConfigRepository.findById(1).orElse(new com.example.techtitans.Entity.SystemConfig()).getCheckIntervalSeconds() * 1000}")
    public void runMonitoringCycle() {
        List<Proxy> proxies = proxyRepository.findAll();
        List<CheckHistory> historyBatch = new ArrayList<>();

        for (Proxy proxy : proxies) {
            // Use the real prober to ping the URL! (Using a default 3000ms timeout)
            String newStatus = networkProber.probe(proxy.getUrl(), 3000);

            // Update consecutive failures if it's down
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

        // Trigger the Alert Engine to check the math!
        alertService.evaluatePoolHealth();
    }
}