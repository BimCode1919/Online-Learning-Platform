package org.oln.onlinelearningplatform.service.aiagent.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.oln.onlinelearningplatform.service.aiagent.YouTubeService;


@Service
public class YouTubeServiceImpl implements YouTubeService {

    private final String RAPID_API_KEY = "afcf68bc40msh2d431ed0a1f0c71p179d71jsn5e230b61cd33";
    private final String RAPID_API_HOST = "youtube-transcript3.p.rapidapi.com";

    @Override
    public String getTranscript(String videoId) {
        try {
            // URL phải khớp với Request URL trong ảnh (để ý có thêm chữ /api/)
            String url = "https://youtube-transcript3.p.rapidapi.com/api/transcript?videoId=" + videoId;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("x-rapidapi-key", RAPID_API_KEY)
                    .header("x-rapidapi-host", RAPID_API_HOST)
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            // Kiểm tra log nếu vẫn lỗi
            if (response.statusCode() != 200) {
                System.out.println("Response body lỗi: " + response.body());
                throw new RuntimeException("API YouTube lỗi: " + response.statusCode());
            }

            return parseTranscript(response.body());

        } catch (Exception e) {
            throw new RuntimeException("Lỗi lấy phụ đề từ RapidAPI: " + e.getMessage());
        }
    }

    private String parseTranscript(String jsonResponse) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(jsonResponse);

            // Theo ảnh Results: JSON trả về có mảng tên là "transcript"
            JsonNode transcriptNode = root.get("transcript");

            if (transcriptNode != null && transcriptNode.isArray()) {
                StringBuilder sb = new StringBuilder();
                for (JsonNode item : transcriptNode) {
                    // Trong ảnh, mỗi phần tử có trường "text"
                    if (item.has("text")) {
                        sb.append(item.get("text").asText()).append(" ");
                    }
                }
                return sb.toString().trim();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
