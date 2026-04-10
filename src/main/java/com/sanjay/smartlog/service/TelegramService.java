package com.sanjay.smartlog.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles all outbound communication to Telegram Bot API.
 */
@Slf4j
@Service
public class TelegramService {

    @Value("${telegram.bot.token}")
    private String botToken;

    private final RestTemplate restTemplate = new RestTemplate();

    private String baseUrl() {
        return "https://api.telegram.org/bot" + botToken;
    }

    /**
     * Sends a plain text message to a Telegram chat.
     */
    public void sendMessage(String chatId, String text) {
        String url = baseUrl() + "/sendMessage";
        Map<String, Object> body = new HashMap<>();
        body.put("chat_id", chatId);
        body.put("text", text);
        body.put("parse_mode", "HTML");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForEntity(url, request, String.class);
            log.info("Message sent to chatId={}", chatId);
        } catch (Exception e) {
            log.error("Failed to send message to chatId={}: {}", chatId, e.getMessage());
        }
    }

    /**
     * Sends a document (e.g., Excel file) to a Telegram chat.
     *
     * @param chatId   Telegram chat ID
     * @param filename Filename shown in Telegram (e.g., "WorkLog_April_2026.xlsx")
     * @param data     Raw bytes of the file
     */
    public void sendDocument(String chatId, String filename, byte[] data) {
        String url = baseUrl() + "/sendDocument";

        ByteArrayResource fileResource = new ByteArrayResource(data) {
            @Override
            public String getFilename() {
                return filename;
            }
        };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("chat_id", chatId);
        body.add("document", fileResource);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForEntity(url, request, String.class);
            log.info("Document '{}' sent to chatId={}", filename, chatId);
        } catch (Exception e) {
            log.error("Failed to send document to chatId={}: {}", chatId, e.getMessage());
        }
    }
}
