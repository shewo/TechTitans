package com.example.techtitans.Service;

import com.example.techtitans.Entity.Proxy;
import com.example.techtitans.Repository.ProxyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.List;

@Service
public class ProxyMonitorService {

    @Autowired
    private ProxyRepository proxyRepository;

    @Autowired
    private NetworkProber networkProber;

    @Scheduled(fixedDelay = 10000)
    public void runMonitoringCycle() {
        List<Proxy> proxies = proxyRepository.findAll();

        // eg: timeout is 3000ms
        long currentTimeout = 3000;

        for (Proxy proxy : proxies) {
            String newStatus = networkProber.probe(proxy.getUrl(), currentTimeout);

            proxy.setStatus(newStatus);
            proxy.setLastCheckedAt(Instant.now().toString());

            if (newStatus.equals("down")) {
                proxy.setConsecutiveFailures(proxy.getConsecutiveFailures() + 1);
            } else {
                proxy.setConsecutiveFailures(0);
            }

            proxyRepository.save(proxy);
        }
        System.out.println("Monitoring cycle completed at " + Instant.now());
    }
}