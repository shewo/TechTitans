package com.example.techtitans.Service;

import com.example.techtitans.Entity.Alert;
import java.util.*;

public class SlackFormatter {

    public static Map<String, Object> formatAlertFired(Alert alert) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("username", "ProxyWatch");
        payload.put("text", "🚨 Proxy Pool Alert Fired - Failure Rate: " + 
                    String.format("%.1f%%", alert.getFailureRate() * 100));

        Map<String, Object> attachment = new HashMap<>();
        attachment.put("color", "#FF4444");
        attachment.put("footer", "ProxyMaze Alert System");
        attachment.put("ts", System.currentTimeMillis() / 1000);

        List<Map<String, String>> fields = new ArrayList<>();
        fields.add(createField("Alert ID", alert.getAlertId()));
        fields.add(createField("Failure Rate", String.format("%.1f%%", alert.getFailureRate() * 100)));
        fields.add(createField("Failed Proxies", alert.getFailedProxies() + " / " + alert.getTotalProxies()));
        fields.add(createField("Threshold", String.format("%.1f%%", alert.getThreshold() * 100)));
        fields.add(createField("Failed IDs", String.join(", ", alert.getFailedProxyIds())));
        fields.add(createField("Fired At", alert.getFiredAt().toString()));

        attachment.put("fields", fields);

        List<Map<String, Object>> attachments = new ArrayList<>();
        attachments.add(attachment);
        payload.put("attachments", attachments);

        return payload;
    }

    public static Map<String, Object> formatAlertResolved(Alert alert) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("username", "ProxyWatch");
        payload.put("text", "✅ Proxy Pool Alert Resolved");

        Map<String, Object> attachment = new HashMap<>();
        attachment.put("color", "#44FF44");
        attachment.put("footer", "ProxyMaze Alert System");
        attachment.put("ts", System.currentTimeMillis() / 1000);

        List<Map<String, String>> fields = new ArrayList<>();
        fields.add(createField("Alert ID", alert.getAlertId()));
        fields.add(createField("Resolved At", alert.getResolvedAt().toString()));

        attachment.put("fields", fields);

        List<Map<String, Object>> attachments = new ArrayList<>();
        attachments.add(attachment);
        payload.put("attachments", attachments);

        return payload;
    }

    private static Map<String, String> createField(String title, String value) {
        Map<String, String> field = new HashMap<>();
        field.put("title", title);
        field.put("value", value);
        return field;
    }
}
