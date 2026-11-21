package iam.mintmate.ai.ai;

import iam.mintmate.ai.transaction.TransactionTools;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * Thin application service that forwards user messages to the configured AI chat client.
 * The service keeps the rest of the codebase insulated from the client API details.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AiAssistantService {

    private final ChatClient chatClient;
    private final TransactionTools transactionTools;


    /**
     * Sends a single user message to the model and returns the assistant's text response.
     * Any prompt shaping, memory, or safety behavior is delegated to the configured client.
     */
    public String chat(String message) {
        long startTime = System.currentTimeMillis();
        log.info("Processing AI chat request");

        String response = chatClient.prompt()
                .user(message)
                .tools(transactionTools)
                .call()
                .content();

        long durationMs = System.currentTimeMillis() - startTime;
        log.info("Completed AI chat request in {} ms", durationMs);
        return response;
    }
}
