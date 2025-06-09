package com.example.springjwt.api;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class GoogleTranslateService {

    @Value("${google.api.key}")
    private String apiKey;

    @Value("${google.translate.url}")
    private String translateUrl;

    public String translateToEnglish(String koreanText) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        JSONObject requestBody = new JSONObject();
        requestBody.put("q", koreanText);
        requestBody.put("source", "ko");
        requestBody.put("target", "en");
        requestBody.put("format", "text");

        HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);
        String urlWithKey = translateUrl + "?key=" + apiKey;

        ResponseEntity<String> response = restTemplate.postForEntity(urlWithKey, entity, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            JSONObject responseJson = new JSONObject(response.getBody());
            JSONArray translations = responseJson.getJSONObject("data").getJSONArray("translations");
            return translations.getJSONObject(0).getString("translatedText");
        } else {
            throw new RuntimeException("Google 번역 실패: " + response.getBody());
        }
    }
    public Map<String, String> translateBatch(List<String> koreanList) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        JSONArray qArray = new JSONArray();
        for (String text : koreanList) {
            qArray.put(text);
        }

        JSONObject requestBody = new JSONObject();
        requestBody.put("q", qArray);
        requestBody.put("source", "ko");
        requestBody.put("target", "en");
        requestBody.put("format", "text");

        HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);
        String urlWithKey = translateUrl + "?key=" + apiKey;

        ResponseEntity<String> response = restTemplate.postForEntity(urlWithKey, entity, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            JSONObject responseJson = new JSONObject(response.getBody());
            JSONArray translations = responseJson.getJSONObject("data").getJSONArray("translations");

            Map<String, String> result = new LinkedHashMap<>();
            for (int i = 0; i < koreanList.size(); i++) {
                result.put(koreanList.get(i), translations.getJSONObject(i).getString("translatedText"));
            }
            System.out.println("🗣 [GoogleTranslateService] 번역 응답 결과: " + response.getBody());
            return result;
        } else {
            throw new RuntimeException("Google 번역 실패: " + response.getBody());
        }
    }

}
