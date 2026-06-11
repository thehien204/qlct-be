package com.family.spend.controller;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
@RequestMapping("/api/send-messenger")
public class MessengerController {

    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping
    public ResponseEntity<Map<String, Object>> sendReminder(@RequestBody Map<String, Object> payload) {
        String message = (String) payload.get("message");
        String pageAccessToken = (String) payload.get("pageAccessToken");
        String pageId = (String) payload.get("pageId");
        String recipientId = (String) payload.get("recipientId");
        Boolean testMode = (Boolean) payload.getOrDefault("testMode", false);

        Map<String, Object> response = new HashMap<>();

        if (testMode || pageAccessToken == null || pageAccessToken.trim().isEmpty()) {
            response.put("success", true);
            response.put("status", "simulated");
            response.put("message", "Simulated message sent successfully (Sandbox Mode).");
            return ResponseEntity.ok(response);
        }

        try {
            String url = "https://graph.facebook.com/v12.0/me/messages?access_token=" + pageAccessToken;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> recipient = new HashMap<>();
            recipient.put("id", recipientId);

            Map<String, Object> messageBody = new HashMap<>();
            messageBody.put("text", message);

            Map<String, Object> request = new HashMap<>();
            request.put("recipient", recipient);
            request.put("message", messageBody);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            ResponseEntity<Map> fbResponse = restTemplate.postForEntity(url, entity, Map.class);

            if (fbResponse.getStatusCode() == HttpStatus.OK && fbResponse.getBody() != null) {
                response.put("success", true);
                response.put("fbResponse", fbResponse.getBody());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("error", "Received non-200 code from Facebook Graph API: " + fbResponse.getStatusCode());
                return ResponseEntity.status(502).body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Facebook Graph API Exception: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
