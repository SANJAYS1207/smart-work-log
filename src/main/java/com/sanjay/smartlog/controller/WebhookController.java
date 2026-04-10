package com.sanjay.smartlog.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanjay.smartlog.service.ConversationStateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Receives incoming Telegram webhook updates.
 *
 * Telegram POSTs a JSON Update object to this endpoint whenever a user
 * sends a message to your bot.
 *
 * Register with:
 *   https://api.telegram.org/bot<TOKEN>/setWebhook?url=https://<your-domain>/webhook
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class WebhookController {

    private final ConversationStateService conversationStateService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody String payload) {
        try {
            JsonNode root = objectMapper.readTree(payload);
            JsonNode message = root.path("message");

            if (message.isMissingNode()) {
                // Could be edited_message, callback_query, etc. — ignore
                return ResponseEntity.ok("ok");
            }

            String chatId = message.path("chat").path("id").asText();
            String text = message.path("text").asText(null);

            if (chatId.isBlank() || text == null) {
                return ResponseEntity.ok("ok");
            }

            log.info("Webhook received: chatId={} text={}", chatId, text);
            conversationStateService.handleIncoming(chatId, text);

        } catch (Exception e) {
            log.error("Error processing webhook payload: {}", e.getMessage());
        }

        // Always return 200 to Telegram (prevents retries)
        return ResponseEntity.ok("ok");
    }
}
