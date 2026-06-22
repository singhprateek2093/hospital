package com.hospital.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Public health check. Hosting platforms (Render) ping this to know the service
 * is up before sending traffic to it. Kept outside the secured area on purpose.
 */
@RestController
public class HealthController {

    @GetMapping("/api/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "ok"));
    }
}
