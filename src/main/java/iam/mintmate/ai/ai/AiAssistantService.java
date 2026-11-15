package iam.mintmate.ai.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * Thin application service that forwards user messages to the configured AI chat client.
 * The service keeps the rest of the codebase insulated from the client API details.
 */
@Service
@Slf4j
public class AiAssistantService {

    private final ChatClient chatClient;

    public AiAssistantService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * Sends a single user message to the model and returns the assistant's text response.
     * Any prompt shaping, memory, or safety behavior is delegated to the configured client.
     */
    public String chat(String message) {
        long startTime = System.currentTimeMillis();
        log.info("Processing AI chat request");

        String response = chatClient.prompt()
                .user(message)
                .call()
                .content();

        long durationMs = System.currentTimeMillis() - startTime;
        log.info("Completed AI chat request in {} ms", durationMs);
        return response;
    }
}
