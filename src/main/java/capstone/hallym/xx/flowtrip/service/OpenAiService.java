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

        Map<String, Object> systemMessage = Map.of(
                "role", "system",
                "content", "당신은 강원도 여행 추천, 관광 혼잡도 분석, 여행 코스 설계 전문가입니다. 후보 데이터 범위 안에서 추천하고, 응답은 JSON 형식으로만 작성합니다."
        );

        Map<String, Object> userMessage = Map.of(
                "role", "user",
                "content", prompt
        );

        Map<String, Object> body = Map.of(
                "model", "gpt-4o-mini",
                "messages", List.of(systemMessage, userMessage),
                "temperature", 0.25
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                Map.class
        );

        Map responseBody = response.getBody();

        if (responseBody == null) {
            return "{}";
        }

        List choices = (List) responseBody.get("choices");

        if (choices == null || choices.isEmpty()) {
            return "{}";
        }

        Map firstChoice = (Map) choices.get(0);
        Map messageMap = (Map) firstChoice.get("message");

        if (messageMap == null) {
            return "{}";
        }

        String result = (String) messageMap.get("content");

        if (result == null) {
            return "{}";
        }

        System.out.println("===== GPT RESPONSE =====");
        System.out.println(result);

        return result;
    }

    private String buildPrompt(TravelRequestDto dto,
                               String themeCandidates,
                               String placeCandidates) {

        StringBuilder sb = new StringBuilder();

        sb.append("당신은 강원도 여행 추천 전문가이자 관광 혼잡도 분석 전문가입니다.\n\n");

        sb.append("아래 사용자 정보와 데이터베이스 후보 정보만을 사용해 다음 작업을 수행하세요.\n");
        sb.append("1. 가장 적합한 여행 테마 1개 선택\n");
        sb.append("2. 대표 추천 장소 1개 선택\n");
        sb.append("3. 날짜, 주말 여부, 성수기 가능성, 장소 특성, 인기 가능성을 고려한 예상 혼잡도 설명 생성\n");
        sb.append("4. 혼잡도 회피를 위한 추천 방문 시간 제안\n");
        sb.append("5. 혼잡할 경우 대체 장소 제안\n");
        sb.append("6. 네이버 로컬 검색에 사용할 식당/카페/숙소/관광지 검색 키워드 생성\n");
        sb.append("7. 추천 테마에 어울리는 일반적인 여행 동선 생성\n\n");

        sb.append("중요 규칙:\n");
        sb.append("- selectedThemeName과 selectedThemeId는 반드시 [후보 테마] 안에서만 선택하세요.\n");
        sb.append("- recommendedPlaceName과 recommendedPlaceId는 반드시 [후보 장소] 안에서만 선택하세요.\n");
        sb.append("- 후보에 없는 대표 장소를 만들지 마세요.\n");
        sb.append("- [후보 장소]가 \"없음\"이면 recommendedPlaceName과 recommendedPlaceId는 빈 문자열로 작성하세요.\n");
        sb.append("- alternativePlaceName은 가능하면 후보 장소 안에서 고르세요.\n");
        sb.append("- 특이사항을 반드시 반영하세요.\n");
        sb.append("- 검색 키워드는 네이버 로컬 검색에 바로 넣을 수 있는 짧은 한국어 문장으로 작성하세요.\n");
        sb.append("- restaurantKeywords, cafeKeywords, hotelKeywords, attractionKeywords는 각각 3개 이상 작성하세요.\n");
        sb.append("- travelRoute는 3개 이상 5개 이하의 짧은 코스 단계로 작성하세요.\n");
        sb.append("- congestionLevel은 반드시 \"낮음\", \"보통\", \"높음\" 중 하나로만 작성하세요.\n");
        sb.append("- congestionScore는 0부터 100 사이 숫자로 작성하세요.\n");
        sb.append("- JSON 이외의 설명, 마크다운, 코드블록을 절대 출력하지 마세요.\n\n");

        sb.append("[사용자 정보]\n");
        sb.append("- 여행 시작일: ").append(dto.getStartDate()).append("\n");
        sb.append("- 여행 종료일: ").append(dto.getEndDate()).append("\n");
        sb.append("- 액티비티 정도: ").append(dto.getActivityLevel()).append("\n");
        sb.append("- 동행 유형: ").append(dto.getCompanion()).append("\n");
        sb.append("- 이동수단: ").append(dto.getTransport()).append("\n");
        sb.append("- 여행 무드: ").append(dto.getMoodGroup()).append("\n");
        sb.append("- 특이사항: ").append(nullSafe(dto.getSpecialRequest())).append("\n");
        sb.append("- 혼잡한 장소 회피 선호: ").append(Boolean.TRUE.equals(dto.getAvoidCrowdedPlaces()) ? "예" : "아니오").append("\n");
        sb.append("- 선호 방문 시간대: ").append(nullSafe(dto.getPreferredTime())).append("\n");
        sb.append("- 여행 스타일: ").append(nullSafe(dto.getTravelStyle())).append("\n");
        sb.append("- 예산 수준: ").append(dto.getBudgetLevel() == null ? "" : dto.getBudgetLevel()).append("\n\n");

        sb.append("[후보 테마]\n");
        sb.append(themeCandidates).append("\n\n");

        sb.append("[후보 장소]\n");
        sb.append(placeCandidates).append("\n\n");

        sb.append("반드시 아래 JSON 구조 그대로 응답하세요.\n");
        sb.append("{\n");
        sb.append("  \"selectedThemeName\": \"\",\n");
        sb.append("  \"selectedThemeId\": \"\",\n");
        sb.append("  \"recommendedPlaceName\": \"\",\n");
        sb.append("  \"recommendedPlaceId\": \"\",\n");
        sb.append("  \"reason\": \"\",\n");
        sb.append("  \"caution\": \"\",\n");
        sb.append("  \"alternativePlaceName\": \"\",\n");
        sb.append("  \"alternativePlaceId\": \"\",\n");
        sb.append("  \"congestionScore\": 0,\n");
        sb.append("  \"congestionLevel\": \"\",\n");
        sb.append("  \"congestionReason\": \"\",\n");
        sb.append("  \"recommendedVisitTime\": \"\",\n");
        sb.append("  \"weatherAdvice\": \"\",\n");
        sb.append("  \"demandSignal\": \"\",\n");
        sb.append("  \"seasonSignal\": \"\",\n");
        sb.append("  \"weekendSignal\": \"\",\n");
        sb.append("  \"weatherSignal\": \"\",\n");
        sb.append("  \"alternativeGuide\": \"\",\n");
        sb.append("  \"weekend\": false,\n");
        sb.append("  \"peakSeason\": false,\n");
        sb.append("  \"restaurantKeywords\": [\"\", \"\", \"\"],\n");
        sb.append("  \"cafeKeywords\": [\"\", \"\", \"\"],\n");
        sb.append("  \"hotelKeywords\": [\"\", \"\", \"\"],\n");
        sb.append("  \"attractionKeywords\": [\"\", \"\", \"\"],\n");
        sb.append("  \"travelRoute\": [\"\", \"\", \"\"]\n");
        sb.append("}\n");

        return sb.toString();
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }
}