package com.example.techtitans.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProxyCreateRequest {

    private List<String> proxies;
    private Boolean replace;

    public List<String> getProxies() { return proxies; }
    public void setProxies(List<String> proxies) { this.proxies = proxies; }
    public Boolean getReplace() { return replace; }
    public void setReplace(Boolean replace) { this.replace = replace; }
}