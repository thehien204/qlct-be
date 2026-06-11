package com.family.spend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
@RequestMapping("/api/gemini")
public class GeminiController {

    @Value("${gemini.api.key:}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/advice")
    public ResponseEntity<Map<String, Object>> getAdvice(@RequestBody Map<String, Object> payload) {
        List<?> expenses = (List<?>) payload.getOrDefault("expenses", Collections.emptyList());
        List<?> members = (List<?>) payload.getOrDefault("members", Collections.emptyList());
        List<?> debts = (List<?>) payload.getOrDefault("debts", Collections.emptyList());
        String month = (String) payload.get("month");

        // Try to call Gemini API if key is present
        if (apiKey != null && !apiKey.trim().isEmpty() && !apiKey.startsWith("${")) {
            try {
                String prompt = buildPrompt(expenses, members, debts, month);
                String responseText = callGeminiApi(prompt);
                Map<String, Object> parsedResponse = parseJson(responseText);
                if (parsedResponse != null) {
                    return ResponseEntity.ok(parsedResponse);
                }
            } catch (Exception e) {
                System.err.println("Error calling Gemini API: " + e.getMessage());
            }
        }

        // Fallback local advice generator
        return ResponseEntity.ok(generateLocalAdvice(expenses, members, debts, month));
    }

    private String buildPrompt(List<?> expenses, List<?> members, List<?> debts, String month) {
        return "Bạn là một Trợ Lý Cố Vấn Tiết Kiệm tài chính cho gia đình. Hãy phân tích danh sách chi tiêu sau của gia đình trong tháng và đưa ra lời khuyên tài chính, kèm theo các tin nhắn nhắc nợ dễ thương cho từng thành viên.\n" +
                "Tháng phân tích: " + (month != null ? month : "Tháng hiện tại") + "\n" +
                "Danh sách thành viên: " + members.toString() + "\n" +
                "Danh sách chi tiêu: " + expenses.toString() + "\n" +
                "Danh sách các khoản nợ cần đối soát đối trừ: " + debts.toString() + "\n" +
                "Hãy trả về một đối tượng JSON duy nhất có cấu trúc chính xác như sau, KHÔNG được chứa thẻ ```json hay bất kỳ văn bản giải thích nào khác ngoài chuỗi JSON:\n" +
                "{\n" +
                "  \"summary\": \"Tóm tắt tình hình chi tiêu của gia đình trong tháng một cách ấm áp, động viên (Ví dụ: Tháng này cả nhà tiêu hết..., chúc mừng cả nhà tiết kiệm...)\",\n" +
                "  \"categoriesAdvice\": \"Phân tích hạng mục chi tiêu nhiều nhất và đưa ra lời khuyên tối ưu chi tiết\",\n" +
                "  \"savingTips\": \"1. Mẹo 1\\n2. Mẹo 2\\n3. Mẹo 3\",\n" +
                "  \"reminders\": [\n" +
                "    {\n" +
                "      \"fromName\": \"Tên người nợ\",\n" +
                "      \"toName\": \"Tên người nhận\",\n" +
                "      \"amount\": 150000,\n" +
                "      \"funnyMessage\": \"Lời nhắc hài hước, vui vẻ, thân thiện (ví dụ: Alo alo Cún yêu ơi, tháng này tiêu chung hết 150k, mau ting ting cho Mẹ nhé! 😘)\",\n" +
                "      \"politeMessage\": \"Lời nhắc lịch sự, tự động trang trọng\",\n" +
                "      \"urgentMessage\": \"Lời nhắc hối thúc vui nhộn (ví dụ: SOS! Trả nợ ngay 150k cho Mẹ kẻo tối cơm mất thịt nha! 🚨)\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
    }

    private String callGeminiApi(String prompt) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> contentMap = new HashMap<>();
        Map<String, Object> partMap = new HashMap<>();
        partMap.put("text", prompt);
        contentMap.put("parts", Collections.singletonList(partMap));
        requestBody.put("contents", Collections.singletonList(contentMap));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            Map body = response.getBody();
            List candidates = (List) body.get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                Map candidate = (Map) candidates.get(0);
                Map content = (Map) candidate.get("content");
                if (content != null) {
                    List parts = (List) content.get("parts");
                    if (parts != null && !parts.isEmpty()) {
                        Map part = (Map) parts.get(0);
                        return (String) part.get("text");
                    }
                }
            }
        }
        throw new RuntimeException("Failed to get content from Gemini response");
    }

    private Map<String, Object> parseJson(String text) {
        try {
            // Clean text if it contains markdown JSON codeblocks
            String clean = text.trim();
            if (clean.startsWith("```")) {
                clean = clean.replaceAll("^```(json)?\\s*", "");
                clean = clean.replaceAll("\\s*```$", "");
            }
            clean = clean.trim();

            // Simple parser: since we want to avoid dependency on complex JSON parser libraries that might not be in pom.xml,
            // we can parse using Jackson ObjectMapper which is definitely in pom.xml (spring-boot-starter-web uses it)
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(clean, Map.class);
        } catch (Exception e) {
            System.err.println("Jackson parsing error: " + e.getMessage());
            return null;
        }
    }

    private Map<String, Object> generateLocalAdvice(List<?> expenses, List<?> members, List<?> debts, String month) {
        double totalAmount = 0;
        Map<String, Double> categoryMap = new HashMap<>();
        
        for (Object obj : expenses) {
            Map<?, ?> map = (Map<?, ?>) obj;
            Number amountNum = (Number) map.get("amount");
            double amount = amountNum != null ? amountNum.doubleValue() : 0.0;
            totalAmount += amount;

            String catId = (String) map.get("categoryId");
            if (catId != null) {
                categoryMap.put(catId, categoryMap.getOrDefault(catId, 0.0) + amount);
            }
        }

        String topCategoryName = "Chi tiêu chung";
        double topCategoryAmount = 0;
        Map<String, String> defaultCategoriesMap = new HashMap<>();
        defaultCategoriesMap.put("food", "Ăn uống & Chợ búa 🍲");
        defaultCategoriesMap.put("utilities", "Điện, Nước & Internet ⚡");
        defaultCategoriesMap.put("education", "Học tập & Giáo dục 📚");
        defaultCategoriesMap.put("shopping", "Sắm sửa & Đồ gia dụng 🛒");
        defaultCategoriesMap.put("health", "Y tế & Sức khỏe 🏥");
        defaultCategoriesMap.put("travel", "Xăng xe & Đi lại 🚗");
        defaultCategoriesMap.put("entertainment", "Vui chơi & Giải trí 🎮");
        defaultCategoriesMap.put("others", "Chi phí khác 💸");

        for (Map.Entry<String, Double> entry : categoryMap.entrySet()) {
            if (entry.getValue() > topCategoryAmount) {
                topCategoryAmount = entry.getValue();
                topCategoryName = defaultCategoriesMap.getOrDefault(entry.getKey(), entry.getKey());
            }
        }

        String formattedTotal = formatVND(totalAmount);
        String formattedTop = formatVND(topCategoryAmount);

        String summary = "Gia đình mình chi tiêu cực kỳ khoa học và tiết kiệm với tổng số tiền " + formattedTotal + ". Hãy duy trì phong độ ấm áp và sòng phẳng này nhé! 🌸";
        if (totalAmount > 15000000) {
            summary = "Tháng này gia đình mình chi tiêu khá nhiều với tổng cộng " + formattedTotal + ". Tuy nhiên nhờ cùng nhau san sẻ nên mọi gánh nặng đều nhẹ đi rất nhiều, cả nhà tuyệt vời lắm! ✨";
        }

        String categoriesAdvice = "Khoản chi nổi bật nhất của gia đình mình là dành cho \"" + topCategoryName + "\" với tổng cộng " + formattedTop + ". Đây là nhu cầu hoàn toàn thiết thực, việc chia đều sẽ giúp các thành viên cảm giác rất thoải mái và bớt áp lực hơn.";
        if (topCategoryAmount > 5000000) {
            categoriesAdvice = "Gia đình đang dồn lực khá lớn cho nhóm \"" + topCategoryName + "\" (" + formattedTop + "). Để tối ưu, cả nhà có thể lập kế hoạch dự chi cụ thể trước mỗi tuần, ưu tiên tự nấu ăn tại nhà hoặc sắm sửa đồ gia dụng theo đợt khuyến mãi lớn.";
        }

        String savingTips = "1. **Sắp xếp mua sắm sỉ**: Mua chung các mặt hàng gia dụng, thực phẩm khô theo lốc lớn tại siêu thị để được hưởng mức chiết khấu tốt.\n2. **Tết kiệm điện gia đình**: Điều chỉnh nhiệt độ điều hòa ở mức 26 độ C và tắt hẳn các thiết bị điện khi ra ngoài.\n3. **Họp mặt gia đình định kỳ**: Dành khoảng 10 phút cuối tháng để cùng nhìn lại sổ tay chi tiêu này, khen ngợi thành viên tiết kiệm tài giỏi nhất!";

        List<Map<String, Object>> remindersList = new ArrayList<>();
        for (int i = 0; i < debts.size(); i++) {
            Map<?, ?> d = (Map<?, ?>) debts.get(i);
            String fromId = (String) d.get("fromId");
            String toId = (String) d.get("toId");
            Number amountNum = (Number) d.get("amount");
            double amount = amountNum != null ? amountNum.doubleValue() : 0.0;

            String fromName = findMemberName(members, fromId);
            String toName = findMemberName(members, toId);
            String amountStr = formatVND(amount);

            Map<String, Object> rem = new HashMap<>();
            rem.put("fromName", fromName);
            rem.put("toName", toName);
            rem.put("amount", amount);
            rem.put("funnyMessage", "Alo alo " + fromName + " thân yêu! Trái Đất quay quanh Mặt Trời, còn khoản nợ " + amountStr + " thì nên quay về ví của " + toName + " ngay và luôn nhé! Ting ting liền tay nào! 😘");
            rem.put("politeMessage", "Chào " + fromName + ". Đây là lời nhắc tự động hỗ trợ tính toán tài chính chung trong gia đình. Khoản đối soát tháng này của bạn cần chuyển trả cho " + toName + " là " + amountStr + ". Cảm ơn bạn.");
            rem.put("urgentMessage", "SOS! 🚨 " + fromName + " ơi, " + toName + " đang réo gọi khoản nợ " + amountStr + " kìa! Trả ngay kẻo tối nay cơm mất thịt, canh mất muối nha! 🍲⚡");
            remindersList.add(rem);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("summary", "💡 (Cố vấn cục bộ) " + summary);
        result.put("categoriesAdvice", categoriesAdvice);
        result.put("savingTips", savingTips);
        result.put("debtAdvice", debts.isEmpty() ? "Hiện tại cán cân tài chính đang rất đẹp. Việc thanh toán các khoản chia tiền sớm sẽ giúp mọi người giữ tâm lý ấm áp!" : "Gia đình chúng ta đang có giao dịch cần tất toán đối soát.");
        result.put("reminders", remindersList);
        return result;
    }

    private String findMemberName(List<?> members, String id) {
        for (Object obj : members) {
            Map<?, ?> m = (Map<?, ?>) obj;
            if (id != null && id.equals(m.get("id"))) {
                return (String) m.get("name");
            }
        }
        return "Thành viên";
    }

    private String formatVND(double amount) {
        return String.format(Locale.US, "%,.0f", amount).replace(",", ".") + " VND";
    }
}
