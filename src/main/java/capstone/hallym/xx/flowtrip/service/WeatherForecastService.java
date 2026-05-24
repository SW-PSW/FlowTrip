package capstone.hallym.xx.flowtrip.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilderFactory;

import capstone.hallym.xx.flowtrip.dto.WeatherForecastDto;

@Service
public class WeatherForecastService {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public WeatherForecastDto getForecast(Double latitude,
                                          Double longitude,
                                          LocalDate travelDate) {

        WeatherForecastDto fallback = buildFallbackForecast(travelDate);

        if (latitude == null || longitude == null || travelDate == null) {
            return fallback;
        }

        try {
            int[] grid = convertToKmaGrid(latitude, longitude);
            String url = "https://www.kma.go.kr/wid/queryDFS.jsp"
                    + "?gridx=" + grid[0]
                    + "&gridy=" + grid[1];

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return fallback;
            }

            Element data = findForecastData(response.body(), travelDate);

            if (data == null) {
                return fallback;
            }

            String weatherText = childText(data, "wfKor");
            Double temperature = parseDouble(childText(data, "temp"));
            Double temperatureMax = normalizeMissingTemperature(parseDouble(childText(data, "tmx")), temperature);
            Double temperatureMin = normalizeMissingTemperature(parseDouble(childText(data, "tmn")), temperature);
            Integer precipitationProbability = parseInteger(childText(data, "pop"));
            Double precipitationSum = parseDouble(childText(data, "r12"));

            WeatherForecastDto forecast = new WeatherForecastDto();
            forecast.setForecastDate(travelDate);
            forecast.setWeatherCodeDescription(weatherText == null || weatherText.isBlank() ? "예보 정보 없음" : weatherText);
            forecast.setTemperatureMax(temperatureMax);
            forecast.setTemperatureMin(temperatureMin);
            forecast.setPrecipitationProbability(precipitationProbability);
            forecast.setPrecipitationSum(precipitationSum);
            forecast.setSourceName("기상청 동네예보");
            forecast.setCrowdImpactScore(calculateCrowdImpactScore(
                    weatherText,
                    precipitationProbability,
                    precipitationSum,
                    temperatureMax
            ));
            forecast.setCrowdImpactReason(buildCrowdImpactReason(forecast));
            forecast.setWeatherSummary(buildWeatherSummary(forecast));

            return forecast;

        } catch (Exception e) {
            return fallback;
        }
    }

    public List<WeatherForecastDto> getForecasts(Double latitude,
                                                 Double longitude,
                                                 LocalDate startDate,
                                                 LocalDate endDate) {

        List<WeatherForecastDto> forecasts = new ArrayList<>();

        if (startDate == null) {
            return forecasts;
        }

        LocalDate safeEndDate = endDate == null || endDate.isBefore(startDate)
                ? startDate
                : endDate;

        long dayCount = ChronoUnit.DAYS.between(startDate, safeEndDate) + 1;

        for (int i = 0; i < dayCount; i++) {
            forecasts.add(getForecast(latitude, longitude, startDate.plusDays(i)));
        }

        return forecasts;
    }

    private WeatherForecastDto buildFallbackForecast(LocalDate travelDate) {
        WeatherForecastDto forecast = new WeatherForecastDto();
        forecast.setForecastDate(travelDate);
        forecast.setWeatherSummary("기상청 날씨 예보를 불러오지 못했습니다.");
        forecast.setWeatherCodeDescription("예보 정보 없음");
        forecast.setCrowdImpactScore(0);
        forecast.setCrowdImpactReason("날씨 자료가 없어 혼잡도에는 날씨 보정을 적용하지 않았습니다.");
        forecast.setSourceName("기상청 동네예보");

        return forecast;
    }

    private Element findForecastData(String xml, LocalDate travelDate) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

        Document document = factory.newDocumentBuilder()
                .parse(new InputSource(new StringReader(xml)));

        NodeList dataList = document.getElementsByTagName("data");

        if (dataList.getLength() == 0) {
            return null;
        }

        int targetDay = calculateTargetDay(travelDate);
        Element fallback = null;
        int bestHourGap = Integer.MAX_VALUE;

        for (int i = 0; i < dataList.getLength(); i++) {
            Element data = (Element) dataList.item(i);
            Integer day = parseInteger(childText(data, "day"));
            Integer hour = parseInteger(childText(data, "hour"));

            if (day == null || hour == null) {
                continue;
            }

            if (day == targetDay) {
                int hourGap = Math.abs(hour - 12);

                if (fallback == null || hourGap < bestHourGap) {
                    fallback = data;
                    bestHourGap = hourGap;
                }
            }
        }

        if (fallback != null) {
            return fallback;
        }

        return (Element) dataList.item(0);
    }

    private int calculateTargetDay(LocalDate travelDate) {
        long dayDiff = ChronoUnit.DAYS.between(LocalDate.now(), travelDate);

        if (dayDiff < 0 || dayDiff > 2) {
            return 0;
        }

        return (int) dayDiff;
    }

    private int calculateCrowdImpactScore(String weatherText,
                                          Integer precipitationProbability,
                                          Double precipitationSum,
                                          Double temperatureMax) {
        int score = 0;
        String weather = weatherText == null ? "" : weatherText;

        if (weather.contains("맑")) {
            score += 8;
        }

        if (weather.contains("비") || weather.contains("눈")) {
            score -= 10;
        }

        if (precipitationProbability != null && precipitationProbability >= 70) {
            score -= 12;
        } else if (precipitationProbability != null && precipitationProbability <= 20) {
            score += 6;
        }

        if (precipitationSum != null && precipitationSum >= 10.0) {
            score -= 10;
        }

        if (temperatureMax != null && temperatureMax >= 28.0 && temperatureMax <= 32.0) {
            score += 5;
        }

        if (temperatureMax != null && (temperatureMax >= 35.0 || temperatureMax <= -5.0)) {
            score -= 8;
        }

        return score;
    }

    private String buildWeatherSummary(WeatherForecastDto forecast) {
        StringBuilder sb = new StringBuilder();

        if (forecast.getForecastDate() != null) {
            sb.append(forecast.getForecastDate()).append(" ");
        }

        sb.append(forecast.getWeatherCodeDescription());

        if (forecast.getTemperatureMin() != null && forecast.getTemperatureMax() != null) {
            sb.append(", ")
                    .append(String.format("%.1f", forecast.getTemperatureMin()))
                    .append("~")
                    .append(String.format("%.1f", forecast.getTemperatureMax()))
                    .append("도");
        }

        if (forecast.getPrecipitationProbability() != null) {
            sb.append(", 강수확률 ")
                    .append(forecast.getPrecipitationProbability())
                    .append("%");
        }

        return sb.toString();
    }

    private String buildCrowdImpactReason(WeatherForecastDto forecast) {
        Integer score = forecast.getCrowdImpactScore();

        if (score == null || score == 0) {
            return "기상청 예보상 날씨가 혼잡도에 주는 영향은 크지 않습니다.";
        }

        StringBuilder reason = new StringBuilder("기상청 예보 기준 ");
        reason.append(forecast.getWeatherCodeDescription());

        if (forecast.getPrecipitationProbability() != null) {
            reason.append(", 강수확률 ")
                    .append(forecast.getPrecipitationProbability())
                    .append("%");
        }

        if (forecast.getTemperatureMax() != null) {
            reason.append(", 최고기온 ")
                    .append(String.format("%.1f", forecast.getTemperatureMax()))
                    .append("도");
        }

        if (score > 0) {
            reason.append("로 야외 방문 수요가 늘 수 있어 혼잡도에 가산했습니다.");
            return reason.toString();
        }

        reason.append("로 야외 방문 수요가 줄 수 있어 혼잡도에서 감산했습니다.");
        return reason.toString();
    }

    private int[] convertToKmaGrid(double latitude, double longitude) {
        double re = 6371.00877;
        double grid = 5.0;
        double slat1 = 30.0;
        double slat2 = 60.0;
        double olon = 126.0;
        double olat = 38.0;
        double xo = 43.0;
        double yo = 136.0;
        double degrad = Math.PI / 180.0;

        double reGrid = re / grid;
        double slat1Rad = slat1 * degrad;
        double slat2Rad = slat2 * degrad;
        double olonRad = olon * degrad;
        double olatRad = olat * degrad;

        double sn = Math.tan(Math.PI * 0.25 + slat2Rad * 0.5) / Math.tan(Math.PI * 0.25 + slat1Rad * 0.5);
        sn = Math.log(Math.cos(slat1Rad) / Math.cos(slat2Rad)) / Math.log(sn);

        double sf = Math.tan(Math.PI * 0.25 + slat1Rad * 0.5);
        sf = Math.pow(sf, sn) * Math.cos(slat1Rad) / sn;

        double ro = Math.tan(Math.PI * 0.25 + olatRad * 0.5);
        ro = reGrid * sf / Math.pow(ro, sn);

        double ra = Math.tan(Math.PI * 0.25 + latitude * degrad * 0.5);
        ra = reGrid * sf / Math.pow(ra, sn);

        double theta = longitude * degrad - olonRad;

        if (theta > Math.PI) {
            theta -= 2.0 * Math.PI;
        }

        if (theta < -Math.PI) {
            theta += 2.0 * Math.PI;
        }

        theta *= sn;

        int x = (int) Math.floor(ra * Math.sin(theta) + xo + 0.5);
        int y = (int) Math.floor(ro - ra * Math.cos(theta) + yo + 0.5);

        return new int[] { x, y };
    }

    private String childText(Element element, String tagName) {
        NodeList nodes = element.getElementsByTagName(tagName);

        if (nodes.getLength() == 0 || nodes.item(0) == null) {
            return null;
        }

        return nodes.item(0).getTextContent();
    }

    private Double parseDouble(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseInteger(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Double normalizeMissingTemperature(Double value, Double fallback) {
        if (value == null || value <= -900.0) {
            return fallback;
        }

        return value;
    }
}
