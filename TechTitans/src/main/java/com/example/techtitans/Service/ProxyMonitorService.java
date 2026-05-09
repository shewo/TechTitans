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
    private ProxyRepository proxyRepository;

    @Autowired
    private CheckHistoryRepository historyRepository;

    @Scheduled(fixedDelay = 15000)
    public void runMonitoringCycle() {
        List<Proxy> proxies = proxyRepository.findAll();
        List<CheckHistory> historyBatch = new ArrayList<>();

        for (Proxy proxy : proxies) {
            String newStatus = "up"; // Placeholder for Member 3's logic

            proxy.setStatus(newStatus);
            proxy.setLastCheckedAt(Instant.now());

            CheckHistory history = new CheckHistory();
            // Match this to your Proxy entity's ID method (getId or getProxyId)
            history.setProxyId(proxy.getId());
            history.setStatus(newStatus);
            history.setCheckedAt(Instant.now());
            historyBatch.add(history);
        }

        proxyRepository.saveAll(proxies);
        historyRepository.saveAll(historyBatch);
    }
}