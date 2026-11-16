package iam.mintmate.ai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {
    private static final String DEFAULT_SYSTEM_PROMPT = """
            You are Mint Mate, a friendly personal finance assistant.

            Help users understand their finances and provide clear,
            practical financial insights.
            """;

    @Value("${mintmate.ai.system-prompt:}")
    private String systemPrompt;

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        String prompt = (systemPrompt == null || systemPrompt.isBlank())
                ? DEFAULT_SYSTEM_PROMPT
                : systemPrompt;

        return builder
                .defaultSystem(prompt)
                .build();
    }
}
