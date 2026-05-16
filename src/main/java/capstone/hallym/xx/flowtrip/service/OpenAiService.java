package capstone.hallym.xx.flowtrip.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import capstone.hallym.xx.flowtrip.dto.TravelRequestDto;

@Service
public class OpenAiService {

    @Value("${openai.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public String requestRecommendation(TravelRequestDto dto,
                                        String themeCandidates,
                                        String placeCandidates) {

        String prompt = buildPrompt(dto, themeCandidates, placeCandidates);

        System.out.println("===== GPT PROMPT =====");
        System.out.println(prompt);

        String url = "https://api.openai.com/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> message = Map.of(
                "role", "user",
                "content", prompt
        );

        Map<String, Object> body = Map.of(
                "model", "gpt-4o-mini",
                "messages", List.of(message),
                "temperature", 0.4
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                Map.class
        );

        Map responseBody = response.getBody();
        List choices = (List) responseBody.get("choices");
        Map firstChoice = (Map) choices.get(0);
        Map messageMap = (Map) firstChoice.get("message");

        String result = (String) messageMap.get("content");

        System.out.println("===== GPT RESPONSE =====");
        System.out.println(result);

        return result;
    }

    private String buildPrompt(TravelRequestDto dto,
                               String themeCandidates,
                               String placeCandidates) {

        StringBuilder sb = new StringBuilder();

        sb.append("당신은 강원도 여행 추천 전문가입니다.\n\n");
        sb.append("아래 사용자 정보와 데이터베이스 후보 정보만을 사용해 ")
          .append("가장 적합한 여행 테마 1개와 대표 장소 1개를 추천하세요.\n\n");

        sb.append("규칙:\n");
        sb.append("- 반드시 후보 데이터 안에서만 선택하세요.\n");
        sb.append("- 후보에 없는 장소나 테마를 만들지 마세요.\n");
        sb.append("- 특이사항을 반드시 반영하세요.\n");
        sb.append("- recommendedPlaceName은 반드시 1개만 작성하세요.\n");
        sb.append("- [후보 장소]가 \"없음\"이면 recommendedPlaceName과 recommendedPlaceId는 반드시 빈 문자열로 작성하세요.\n");
        sb.append("- 테마 설명(themeSummary)에 등장하는 장소명을 recommendedPlaceName으로 사용하지 마세요.\n");
        sb.append("- JSON 형식으로만 응답하세요.\n\n");

        sb.append("[사용자 정보]\n");
        sb.append("- 여행 시작일: ").append(dto.getStartDate()).append("\n");
        sb.append("- 여행 종료일: ").append(dto.getEndDate()).append("\n");
        sb.append("- 액티비티 정도: ").append(dto.getActivityLevel()).append("\n");
        sb.append("- 동행 유형: ").append(dto.getCompanion()).append("\n");
        sb.append("- 이동수단: ").append(dto.getTransport()).append("\n");
        sb.append("- 여행 무드: ").append(dto.getMoodGroup()).append("\n");
        sb.append("- 특이사항: ").append(dto.getSpecialRequest()).append("\n\n");

        sb.append("[후보 테마]\n");
        sb.append(themeCandidates).append("\n\n");

        sb.append("[후보 장소]\n");
        sb.append(placeCandidates).append("\n\n");

        sb.append("반드시 아래 JSON 형식으로만 응답하세요.\n");
        sb.append("{\n");
        sb.append("  \"selectedThemeName\": \"\",\n");
        sb.append("  \"selectedThemeId\": \"\",\n");
        sb.append("  \"recommendedPlaceName\": \"\",\n");
        sb.append("  \"recommendedPlaceId\": \"\",\n");
        sb.append("  \"reason\": \"\",\n");
        sb.append("  \"caution\": \"\",\n");
        sb.append("  \"alternativePlaceName\": \"\",\n");
        sb.append("  \"alternativePlaceId\": \"\"\n");
        sb.append("}\n");

        return sb.toString();
    }
}