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

        List<Proxy> downProxies = allProxies.stream()
                .filter(p -> "down".equals(p.getStatus()))
                .collect(Collectors.toList());

        double failureRate = (double) downProxies.size() / allProxies.size();

        Alert activeAlert = alertRepository.findAll().stream()
                .filter(a -> "active".equals(a.getStatus()))
                .findFirst()
                .orElse(null);

        if (failureRate >= 0.20) {
            if (activeAlert == null) {
                Alert newAlert = new Alert();
                newAlert.setAlertId("alert-" + UUID.randomUUID().toString().substring(0, 8));
                newAlert.setStatus("active");
                newAlert.setFailureRate(Math.round(failureRate * 100.0) / 100.0);
                newAlert.setTotalProxies(allProxies.size());
                newAlert.setFailedProxies(downProxies.size());
                newAlert.setFailedProxyIds(downProxies.stream().map(Proxy::getId).collect(Collectors.toList()));
                newAlert.setThreshold(0.20);
                newAlert.setFiredAt(Instant.now());
                newAlert.setMessage("Proxy pool failure rate exceeded threshold");

                alertRepository.save(newAlert);

                // BOOM! FIRE THE WEBHOOK
                webhookService.sendAlertFired(newAlert);
            } else {
                activeAlert.setFailureRate(Math.round(failureRate * 100.0) / 100.0);
                activeAlert.setFailedProxies(downProxies.size());
                activeAlert.setFailedProxyIds(downProxies.stream().map(Proxy::getId).collect(Collectors.toList()));
                alertRepository.save(activeAlert);
            }
        } else {
            if (activeAlert != null) {
                activeAlert.setStatus("resolved");
                activeAlert.setResolvedAt(Instant.now());
                alertRepository.save(activeAlert);

                // BOOM! RESOLVE THE WEBHOOK
                webhookService.sendAlertResolved(activeAlert);
            }
        }
    }
}