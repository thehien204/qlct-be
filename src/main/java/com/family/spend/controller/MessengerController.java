package com.family.spend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class MessengerController {

    private static final Logger log = LoggerFactory.getLogger(MessengerController.class);

    @Value("${facebook.page.access.token}")
    private String defaultAccessToken;

    @Value("${facebook.page.id}")
    private String defaultPageId;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping("/send-messenger")
    public Map<String, Object> sendMessage(@RequestBody Map<String, Object> payload) {
        Map<String, Object> response = new HashMap<>();

        String message = (String) payload.get("message");
        String recipientId = (String) payload.get("recipientId");
        String pageAccessToken = (String) payload.get("pageAccessToken");
        String pageId = (String) payload.get("pageId");
        Boolean testMode = (Boolean) payload.get("testMode");

        if (message == null || recipientId == null) {
            response.put("success", false);
            response.put("error", "Thiếu nội dung tin nhắn hoặc ID người nhận (PSID).");
            return response;
        }

        String activeToken = (pageAccessToken != null && !pageAccessToken.trim().isEmpty()) ? pageAccessToken : defaultAccessToken;

        // If testMode is true or token is missing, perform simulation
        if ((testMode != null && testMode) || activeToken == null || activeToken.trim().isEmpty()) {
            response.put("success", true);
            response.put("simulated", true);
            response.put("message", "Mô phỏng: Gửi tin nhắn thành công qua webhook/FB API!");
            response.put("sentMessage", message);
            response.put("recipient", recipientId);
            return response;
        }

        try {
            String urlString = "https://graph.facebook.com/v19.0/me/messages?access_token=" + activeToken;
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // Construct Facebook Graph API body
            Map<String, Object> recipient = new HashMap<>();
            recipient.put("id", recipientId);

            Map<String, Object> messageObj = new HashMap<>();
            messageObj.put("text", message);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("recipient", recipient);
            requestBody.put("message", messageObj);

            String bodyJson = objectMapper.writeValueAsString(requestBody);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(bodyJson.getBytes("utf-8"));
            }

            int responseCode = conn.getResponseCode();
            StringBuilder resultResponse = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(
                    responseCode >= 200 && responseCode < 300 ? conn.getInputStream() : conn.getErrorStream(), "utf-8"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    resultResponse.append(line);
                }
            }

            Map<String, Object> result = objectMapper.readValue(resultResponse.toString(), Map.class);
            if (responseCode == HttpURLConnection.HTTP_OK) {
                response.put("success", true);
                response.put("simulated", false);
                response.put("data", result);
            } else {
                response.put("success", false);
                Map<String, Object> errorMap = (Map<String, Object>) result.get("error");
                response.put("error", errorMap != null ? errorMap.get("message") : "Lỗi khi gửi yêu cầu đến API Facebook.");
                response.put("fullError", result);
            }

        } catch (Exception e) {
            log.error("Facebook Send API Error: " + e.getMessage(), e);
            response.put("success", false);
            response.put("error", "Lỗi hệ thống khi gửi tin nhắn Facebook: " + e.getMessage());
        }

        return response;
    }
}
