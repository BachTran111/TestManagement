package app.NganHangDe.Service;

import okhttp3.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class HFService {
    private static final String API_URL = "https://router.huggingface.co/nebius/v1/chat/completions";
    private static final String API_KEY = "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";

    // Tạo OkHttpClient với timeout lớn hơn (60s connect, 60s write, 120s read)
    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .build();

    private final ObjectMapper mapper = new ObjectMapper();

    /*******
     * Gửi message tới API chatCompletion Hugging Face mới
     * @param userMessage nội dung câu hỏi
     * @return nội dung phản hồi của API
     * @throws IOException lỗi khi gọi API hoặc xử lý dữ liệu
     *******/
    public String getAnswerSuggestion(String userMessage) throws IOException {
        String jsonBody = mapper.writeValueAsString(Map.of(
                "model", "deepseek-ai/DeepSeek-V3-0324-fast",
                "messages", List.of(Map.of("role", "user", "content", userMessage)),
                "stream", false
        ));

        RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Lỗi gọi API: " + response.code() + " - " + response.message());
            }

            String responseBody = response.body().string();
            JsonNode root = mapper.readTree(responseBody);

            JsonNode choices = root.get("choices");
            if (choices != null && choices.isArray() && choices.size() > 0) {
                JsonNode messageNode = choices.get(0).get("message");
                if (messageNode != null) {
                    return messageNode.get("content").asText();
                }
            }

            return "Không nhận được phản hồi hợp lệ từ API";
        }
    }
}
