package com.family.spend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class GeminiController {

    private static final Logger log = LoggerFactory.getLogger(GeminiController.class);

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping("/gemini/advice")
    public Map<String, Object> getAdvice(@RequestBody Map<String, Object> payload) {
        List<Map<String, Object>> expenses = (List<Map<String, Object>>) payload.get("expenses");
        List<Map<String, Object>> members = (List<Map<String, Object>>) payload.get("members");
        List<Map<String, Object>> debts = (List<Map<String, Object>>) payload.get("debts");
        String month = (String) payload.get("month");

        if (expenses == null || members == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", "Thiếu thông tin chi tiêu hoặc thành viên gia đình.");
            return err;
        }

        // Fallback or use Gemini API
        if (geminiApiKey == null || geminiApiKey.trim().isEmpty()) {
            log.info("Gemini API Key missing, falling back to smart local advice.");
            Map<String, Object> advice = generateSmartFallbackAdvice(expenses, members, debts, month);
            advice.put("summary", "💡 (Cố vấn Cục bộ) " + advice.get("summary"));
            return advice;
        }

        try {
            return callGeminiApi(expenses, members, debts, month);
        } catch (Exception e) {
            log.error("Failed to call Gemini API, falling back to local advice: " + e.getMessage(), e);
            Map<String, Object> advice = generateSmartFallbackAdvice(expenses, members, debts, month);
            advice.put("summary", "💡 (Cố vấn Khôi phục Cục bộ) " + advice.get("summary"));
            return advice;
        }
    }

    private Map<String, Object> callGeminiApi(List<Map<String, Object>> expenses,
                                              List<Map<String, Object>> members,
                                              List<Map<String, Object>> debts,
                                              String month) throws Exception {
        // Build the text prompt
        StringBuilder membersDesc = new StringBuilder();
        for (Map<String, Object> m : members) {
            membersDesc.append(m.get("name")).append(" (Vai trò: ").append(m.get("role")).append("), ");
        }

        List<Map<String, Object>> formattedExpenses = new ArrayList<>();
        for (Map<String, Object> e : expenses) {
            Map<String, Object> fe = new HashMap<>();
            fe.put("tiêu_đề", e.get("title"));
            fe.put("số_tiền", e.get("amount"));
            fe.put("ngày", e.get("date"));
            fe.put("ghi_chú", e.get("notes") != null ? e.get("notes") : "");

            String paidById = (String) e.get("paidById");
            fe.put("người_mua", getMemberName(members, paidById));

            List<String> bIds = (List<String>) e.get("beneficiaryIds");
            List<String> bNames = new ArrayList<>();
            if (bIds != null) {
                for (String id : bIds) {
                    bNames.add(getMemberName(members, id));
                }
            }
            fe.put("mua_cho_ai", String.join(", ", bNames));
            fe.put("mã_danh_mục", e.get("categoryId"));
            formattedExpenses.add(fe);
        }

        List<Map<String, Object>> formattedDebts = new ArrayList<>();
        if (debts != null) {
            for (Map<String, Object> d : debts) {
                Map<String, Object> fd = new HashMap<>();
                fd.put("người_nợ", getMemberName(members, (String) d.get("fromId")));
                fd.put("người_nhận", getMemberName(members, (String) d.get("toId")));
                fd.put("số_tiền", d.get("amount"));
                formattedDebts.add(fd);
            }
        }

        String prompt = "Hãy là một Cố vấn Tài chính Gia đình thông minh, vui vẻ và am hiểu văn hóa gia đình Việt Nam.\n" +
                "Tôi có dữ liệu chi tiêu hàng tháng (" + (month != null ? month : "tất cả các tháng") + ") của gia đình:\n" +
                "Các thành viên: " + membersDesc + "\n" +
                "Chi tiết các khoản chi tiêu: " + objectMapper.writeValueAsString(formattedExpenses) + "\n" +
                "Các khoản nợ đang cần thanh toán chia tiền tháng này: " + objectMapper.writeValueAsString(formattedDebts) + "\n\n" +
                "Hãy phân tích chi tiết dữ liệu này và trả về phản hồi định dạng JSON chính xác khớp với schema cấu trúc sau:\n" +
                "{\n" +
                "  \"summary\": \"Tóm tắt ngắn gọn (1-2 câu) về tình hình chi tiêu của gia đình trong tháng này, khen ngợi hoặc động viên một cách dí dỏm bằng tiếng Việt ấm áp.\",\n" +
                "  \"categoriesAdvice\": \"Phân tích xem danh mục nào gia đình chi nhiều nhất (ví dụ: Ăn uống, mua sắm,...), mức độ hợp lý và lời khuyên cụ thể để tối ưu hóa danh mục đó.\",\n" +
                "  \"debtAdvice\": \"Lời khuyên giải quyết các khoản chia tiền (nợ) hiện tại giữa các thành viên sao cho êm thấm, vui vẻ nhất.\",\n" +
                "  \"savingTips\": \"3 mẹo tiết kiệm tiền thiết thực cho gia đình dựa trên đặc trưng chi tiêu thực tế của họ ở trên.\",\n" +
                "  \"reminders\": [\n" +
                "    {\n" +
                "      \"fromName\": \"Tên người nợ\",\n" +
                "      \"toName\": \"Tên người nhận\",\n" +
                "      \"amount\": 100000,\n" +
                "      \"funnyMessage\": \"Tin nhắn nhắc nợ thiết kế riêng siêu hài hước, dễ thương, không gây căng thẳng.\",\n" +
                "      \"politeMessage\": \"Tin nhắn nhắc nợ lịch sự, trang trọng, ấm áp tình cảm gia đình.\",\n" +
                "      \"urgentMessage\": \"Tin nhắn nhắc nợ hối thúc kiểu tinh nghịch dọa dẫm vui vẻ (ví dụ: không trả tiền bố cắt cơm tối).\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        // Call Gemini REST API
        String urlString = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + geminiApiKey;
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        // Build the request body for Gemini REST API
        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", prompt);

        Map<String, Object> parts = new HashMap<>();
        parts.put("parts", Collections.singletonList(textPart));

        Map<String, Object> contents = new HashMap<>();
        contents.put("contents", Collections.singletonList(parts));

        // Enforce JSON output in configuration
        Map<String, Object> genConfig = new HashMap<>();
        genConfig.put("responseMimeType", "application/json");
        contents.put("generationConfig", genConfig);

        String payloadJson = objectMapper.writeValueAsString(contents);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(payloadJson.getBytes("utf-8"));
        }

        int responseCode = conn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "utf-8"))) {
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    errorResponse.append(line);
                }
                throw new IOException("Gemini API error: Code " + responseCode + " " + errorResponse.toString());
            }
        }

        StringBuilder responseBody = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
            String line;
            while ((line = br.readLine()) != null) {
                responseBody.append(line);
            }
        }

        // Parse response body
        Map<String, Object> apiResponse = objectMapper.readValue(responseBody.toString(), Map.class);
        List<Map<String, Object>> candidates = (List<Map<String, Object>>) apiResponse.get("candidates");
        if (candidates == null || candidates.isEmpty()) {
            throw new Exception("No candidates returned from Gemini API");
        }
        Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
        List<Map<String, Object>> responseParts = (List<Map<String, Object>>) content.get("parts");
        if (responseParts == null || responseParts.isEmpty()) {
            throw new Exception("No parts in candidates response content");
        }
        String textResult = (String) responseParts.get(0).get("text");

        return objectMapper.readValue(textResult.trim(), Map.class);
    }

    private String getMemberName(List<Map<String, Object>> members, String id) {
        if (id == null) return "Không rõ";
        for (Map<String, Object> m : members) {
            if (id.equals(m.get("id"))) {
                return (String) m.get("name");
            }
        }
        return "Không rõ";
    }

    private Map<String, Object> generateSmartFallbackAdvice(List<Map<String, Object>> expenses,
                                                            List<Map<String, Object>> members,
                                                            List<Map<String, Object>> debts,
                                                            String month) {
        double totalAmount = 0;
        Map<String, Double> categoryMap = new HashMap<>();
        for (Map<String, Object> e : expenses) {
            double amount = 0;
            Object amtObj = e.get("amount");
            if (amtObj instanceof Number) {
                amount = ((Number) amtObj).doubleValue();
            } else if (amtObj instanceof String) {
                try {
                    amount = Double.parseDouble((String) amtObj);
                } catch (Exception ex) {}
            }
            totalAmount += amount;

            String catId = (String) e.get("categoryId");
            if (catId == null) catId = "others";
            categoryMap.put(catId, categoryMap.getOrDefault(catId, 0.0) + amount);
        }

        String topCategoryName = "Chi tiêu chung";
        double topCategoryAmount = 0;

        Map<String, String> categoryLabels = new HashMap<>();
        categoryLabels.put("food", "Ăn uống & Chợ búa 🍲");
        categoryLabels.put("utilities", "Điện, Nước & Internet ⚡");
        categoryLabels.put("education", "Học tập & Giáo dục 📚");
        categoryLabels.put("shopping", "Sắm sửa & Đồ gia dụng 🛒");
        categoryLabels.put("health", "Y tế & Sức khỏe 🏥");
        categoryLabels.put("travel", "Xăng xe & Đi lại 🚗");
        categoryLabels.put("entertainment", "Vui chơi & Giải trí 🎮");
        categoryLabels.put("others", "Chi phí khác 💸");

        for (Map.Entry<String, Double> entry : categoryMap.entrySet()) {
            if (entry.getValue() > topCategoryAmount) {
                topCategoryAmount = entry.getValue();
                topCategoryName = categoryLabels.getOrDefault(entry.getKey(), entry.getKey());
            }
        }

        Locale vietnam = new Locale("vi", "VN");
        NumberFormat vndFormat = NumberFormat.getCurrencyInstance(vietnam);
        // Clean currency output formatting
        String totalAmountStr = formatVnd(totalAmount);
        String topCategoryAmountStr = formatVnd(topCategoryAmount);

        String summary = "Cả nhà đã cùng nhau chi tiêu tổng cộng " + totalAmountStr + " trong " +
                (month != null ? "tháng " + month.substring(Math.max(0, month.length() - 2)) : "thời gian qua") + ". Hãy tiếp tục đồng lòng quản lý tài chính thật tốt nhé!";
        if (totalAmount > 15000000) {
            summary = "Tháng này gia đình mình chi tiêu khá mặn với tổng cộng " + totalAmountStr + ". Tuy nhiên nhờ cùng nhau san sẻ nên mọi gánh nặng đều nhẹ đi rất nhiều, cả nhà tuyệt vời lắm! ✨";
        } else if (totalAmount > 0) {
            summary = "Gia đình mình chi tiêu cực kỳ khoa học và tiết kiệm với tổng số tiền " + totalAmountStr + ". Hãy duy trì phong độ ấm áp và sòng phẳng này nhé! 🌸";
        }

        String categoriesAdvice = "Khoản chi nổi bật nhất của gia đình mình là dành cho \"" + topCategoryName + "\" với tổng cộng " + topCategoryAmountStr + ". Đây là nhu cầu hoàn toàn thiết thực, việc chia đều sẽ giúp các thành viên cảm giác rất thoải mái và bớt áp lực hơn.";
        if (topCategoryAmount > 5000000) {
            categoriesAdvice = "Gia đình đang dồn lực khá lớn cho nhóm \"" + topCategoryName + "\" (" + topCategoryAmountStr + "). Để tối ưu, cả nhà có thể lập kế hoạch dự chi cụ thể trước mỗi tuần, ưu tiên tự nấu ăn tại nhà hoặc sắm sửa đồ gia dụng theo đợt khuyến mãi lớn để tiết giảm từ 10-15%.";
        }

        String debtAdvice = "Hiện tại cán cân tài chính đang rất đẹp. Việc thanh toán các khoản chia tiền sớm sẽ giúp mọi người giữ tâm lý ấm áp và sẵn sàng cho các kế hoạch chi tiêu chung tiếp theo!";
        if (debts != null && !debts.isEmpty()) {
            debtAdvice = "Gia đình chúng ta đang có " + debts.size() + " giao dịch cần tất toán đối soát. Đề xuất cả nhà sử dụng hình thức chuyển khoản nhanh qua mã QR để giải quyết dứt điểm các khoản nợ nần vui vẻ trước hạn!";
        }

        String savingTips = "1. **Sắp xếp mua sắm sỉ**: Nên mua chung các mặt hàng gia dụng, thực phẩm khô theo lốc lớn tại siêu thị để được hưởng mức chiết khấu tốt.\n" +
                "2. **Tận dụng các gói điện gia đình**: Điều chỉnh nhiệt độ điều hòa ở mức 26 độ C và tắt hẳn các thiết bị điện khi ra ngoài để tiết kiệm tối thiểu 10% hóa đơn tháng này.\n" +
                "3. **Họp mặt gia đình định kỳ**: Dành khoảng 10 phút cuối tháng để cùng nhìn lại sổ tay chi tiêu này, khen ngợi thành viên tiết kiệm tài giỏi nhất!";

        List<Map<String, Object>> remindersList = new ArrayList<>();
        if (debts != null) {
            for (Map<String, Object> d : debts) {
                String fromId = (String) d.get("fromId");
                String toId = (String) d.get("toId");
                double amount = 0;
                Object amtObj = d.get("amount");
                if (amtObj instanceof Number) {
                    amount = ((Number) amtObj).doubleValue();
                } else if (amtObj instanceof String) {
                    try {
                        amount = Double.parseDouble((String) amtObj);
                    } catch (Exception ex) {}
                }

                String fromName = getMemberName(members, fromId);
                String toName = getMemberName(members, toId);
                String amountStr = formatVnd(amount);

                Map<String, Object> reminder = new HashMap<>();
                reminder.put("fromName", fromName);
                reminder.put("toName", toName);
                reminder.put("amount", amount);
                reminder.put("funnyMessage", "Alo alo " + fromName + " thân yêu! Trái Đất quay quanh Mặt Trời, còn khoản nợ " + amountStr + " thì nên quay về ví của " + toName + " ngay và luôn nhé! Ting ting liền tay, thắt chặt tình thân nào! 😘");
                reminder.put("politeMessage", "Chào " + fromName + ". Đây là lời nhắc tự động hỗ trợ tính toán tài chính chung trong gia đình. Khoản đối soát tháng này của bạn cần chuyển trả cho " + toName + " là " + amountStr + ". Trân trọng cảm ơn sự san sẻ ấm áp của bạn.");
                reminder.put("urgentMessage", "SOS! 🚨 " + fromName + " ơi, " + toName + " đang réo gọi khoản nợ " + amountStr + " kìa! Trả ngay kẻo tối nay cơm mất thịt, canh mất muối, mạng internet gia đình đột ngột chuyển sang chế độ 'mất kết nối' nha! 🍲⚡");
                remindersList.add(reminder);
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("summary", summary);
        response.put("categoriesAdvice", categoriesAdvice);
        response.put("debtAdvice", debtAdvice);
        response.put("savingTips", savingTips);
        response.put("reminders", remindersList);
        return response;
    }

    private String formatVnd(double amount) {
        return String.format("%,.0f", amount).replace(",", ".") + " VND";
    }
}
