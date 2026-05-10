package com.example.techtitans.Service;

import com.example.techtitans.Entity.Alert;
import java.util.*;

public class DiscordFormatter {

    public static Map<String, Object> formatAlertFired(Alert alert) {
        Map<String, Object> payload = new HashMap<>();

        Map<String, Object> embed = new HashMap<>();
        embed.put("title", "🚨 Proxy Pool Alert Fired");
        embed.put("description", "Failure rate has exceeded threshold: " + 
                    String.format("%.1f%%", alert.getFailureRate() * 100));
        embed.put("color", 16711680); // Red: #FF0000

        List<Map<String, Object>> fields = new ArrayList<>();
        fields.add(createField("Alert ID", alert.getAlertId(), true));
        fields.add(createField("Failure Rate", String.format("%.1f%%", alert.getFailureRate() * 100), true));
        fields.add(createField("Failed Proxies", alert.getFailedProxies() + " / " + alert.getTotalProxies(), true));
        fields.add(createField("Threshold", String.format("%.1f%%", alert.getThreshold() * 100), true));
        fields.add(createField("Failed IDs", String.join(", ", alert.getFailedProxyIds()), false));
        fields.add(createField("Fired At", alert.getFiredAt().toString(), true));

        embed.put("fields", fields);

        Map<String, Object> footer = new HashMap<>();
        footer.put("text", "ProxyMaze Alert System");
        embed.put("footer", footer);

        List<Map<String, Object>> embeds = new ArrayList<>();
        embeds.add(embed);
        payload.put("embeds", embeds);

        return payload;
    }

    public static Map<String, Object> formatAlertResolved(Alert alert) {
        Map<String, Object> payload = new HashMap<>();

        Map<String, Object> embed = new HashMap<>();
        embed.put("title", "✅ Proxy Pool Alert Resolved");
        embed.put("description", "The proxy pool has recovered below the failure threshold");
        embed.put("color", 65280); // Green: #00FF00

        List<Map<String, Object>> fields = new ArrayList<>();
        fields.add(createField("Alert ID", alert.getAlertId(), true));
        fields.add(createField("Resolved At", alert.getResolvedAt().toString(), true));

        embed.put("fields", fields);

        Map<String, Object> footer = new HashMap<>();
        footer.put("text", "ProxyMaze Alert System");
        embed.put("footer", footer);

        List<Map<String, Object>> embeds = new ArrayList<>();
        embeds.add(embed);
        payload.put("embeds", embeds);

        return payload;
    }

    private static Map<String, Object> createField(String name, String value, boolean inline) {
        Map<String, Object> field = new HashMap<>();
        field.put("name", name);
        field.put("value", value);
        field.put("inline", inline);
        return field;
    }
}
