package com.example.techtitans.Service;

import com.example.techtitans.Entity.Proxy;
import com.example.techtitans.Repository.ProxyRepository;
import com.example.techtitans.dto.ProxyCreateRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProxyService {

    private final ProxyRepository proxyRepository;

    public ProxyService(ProxyRepository proxyRepository) {
        this.proxyRepository = proxyRepository;
    }

    @Transactional
    public List<Proxy> loadProxies(ProxyCreateRequest request) {
        if (Boolean.TRUE.equals(request.getReplace())) {
            proxyRepository.deleteAll();
        }

        List<Proxy> savedProxies = new ArrayList<>();

        if (request.getProxies() != null) {
            for (String url : request.getProxies()) {
                Proxy proxy = new Proxy();
                proxy.setUrl(url);
                proxy.setId(url.substring(url.lastIndexOf('/') + 1));
                proxy.setStatus("pending");

                savedProxies.add(proxyRepository.save(proxy));
            }
        }
        return savedProxies;
    }
}