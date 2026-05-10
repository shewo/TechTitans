package com.example.techtitans.Service;

import com.example.techtitans.Entity.Alert;
import com.example.techtitans.Entity.Proxy;
import com.example.techtitans.Repository.AlertRepository;
import com.example.techtitans.Repository.ProxyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AlertService {

    @Autowired
    private ProxyRepository proxyRepository;

    @Autowired
    private AlertRepository alertRepository;

    // ADDED THIS:
    @Autowired
    private WebhookService webhookService;

    public void evaluatePoolHealth() {
        List<Proxy> allProxies = proxyRepository.findAll();
        if (allProxies.isEmpty()) return;

        // Calculate failed proxies fresh each time
        List<Proxy> downProxies = allProxies.stream()
                .filter(p -> "down".equals(p.getStatus()))
                .collect(Collectors.toList());

        double failureRate = (double) downProxies.size() / allProxies.size();
        List<String> failedProxyIds = downProxies.stream().map(Proxy::getId).collect(Collectors.toList());

        Alert activeAlert = alertRepository.findAll().stream()
                .filter(a -> "active".equals(a.getStatus()))
                .findFirst()
                .orElse(null);

        if (failureRate >= 0.20) {
            if (activeAlert == null) {
                // No active alert - fire a new one
                Alert newAlert = new Alert();
                newAlert.setAlertId("alert-" + UUID.randomUUID().toString().substring(0, 8));
                newAlert.setStatus("active");
                newAlert.setFailureRate(failureRate);
                newAlert.setTotalProxies(allProxies.size());
                newAlert.setFailedProxies(downProxies.size());
                newAlert.setFailedProxyIds(failedProxyIds);
                newAlert.setThreshold(0.20);
                newAlert.setFiredAt(Instant.now());
                newAlert.setMessage("Proxy pool failure rate exceeded threshold");

                alertRepository.save(newAlert);

                // BOOM! FIRE THE WEBHOOK
                webhookService.sendAlertFired(newAlert);
            } else {
                // Active alert exists - update it with fresh data
                activeAlert.setFailureRate(failureRate);
                activeAlert.setFailedProxies(downProxies.size());
                activeAlert.setFailedProxyIds(failedProxyIds);
                alertRepository.save(activeAlert);
            }
        } else {
            if (activeAlert != null) {
                // Breach has recovered - resolve the alert
                activeAlert.setStatus("resolved");
                activeAlert.setResolvedAt(Instant.now());
                alertRepository.save(activeAlert);

                // BOOM! RESOLVE THE WEBHOOK
                webhookService.sendAlertResolved(activeAlert);
            }
        }
    }
}