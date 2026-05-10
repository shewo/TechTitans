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
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class ProxyMonitorService {

    @Autowired private AlertService alertService;
    @Autowired private ProxyRepository proxyRepository;
    @Autowired private CheckHistoryRepository historyRepository;
    @Autowired private NetworkProber networkProber;
    @Autowired private ConfigController configController;

    private Instant lastRun = Instant.MIN;

    @Scheduled(fixedDelay = 500)
    public void runMonitoringCycle() {
        int intervalSeconds = configController.getCurrentConfig().getCheckIntervalSeconds();
        int timeoutMs = configController.getCurrentConfig().getRequestTimeoutMs();

        if (Instant.now().isBefore(lastRun.plusSeconds(intervalSeconds))) {
            return;
        }
        lastRun = Instant.now();

        List<Proxy> proxies = proxyRepository.findAll();
        if (proxies.isEmpty()) return;

        // Fire all requests asynchronously
        List<CompletableFuture<CheckHistory>> futures = proxies.stream().map(proxy -> {
            return networkProber.probeAsync(proxy.getUrl(), timeoutMs).thenApply(newStatus -> {
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
            });
        }).collect(Collectors.toList());

        // Wait for all HTTP checks to finish
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // Extract the results
        List<CheckHistory> finalHistoryBatch = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        proxyRepository.saveAll(proxies);
        historyRepository.saveAll(finalHistoryBatch);

        alertService.evaluatePoolHealth();
    }
}