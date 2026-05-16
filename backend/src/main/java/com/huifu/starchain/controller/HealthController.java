package com.huifu.starchain.controller;

import com.huifu.starchain.common.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/api/v1/health")
    public ApiResponse<Map<String, Object>> health() {
        var runtime = Runtime.getRuntime();
        var os = ManagementFactory.getOperatingSystemMXBean();
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("status", "UP");
        info.put("timestamp", LocalDateTime.now().toString());
        info.put("uptime", ManagementFactory.getRuntimeMXBean().getUptime() / 1000 + "s");
        info.put("memory", Map.of(
            "heapUsed", (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024 + "MB",
            "heapMax", runtime.maxMemory() / 1024 / 1024 + "MB",
            "cpuCores", runtime.availableProcessors()
        ));
        info.put("system", Map.of(
            "osName", System.getProperty("os.name"),
            "javaVersion", System.getProperty("java.version"),
            "loadAverage", String.format("%.2f", os.getSystemLoadAverage())
        ));
        return ApiResponse.ok(info);
    }
}
