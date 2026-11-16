package iam.mintmate.ai.ai;

import iam.mintmate.ai.dto.ChatRequestDTO;
import iam.mintmate.ai.dto.ChatResponseDTO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * HTTP entry point for chat requests.
 * The controller only handles transport concerns and delegates the actual AI call to the service.
 */
@RestController
@RequestMapping("/api/ai")
public class AiAssistantController {
    private final AiAssistantService aiAssistantService;

    public AiAssistantController(AiAssistantService aiAssistantService) {
        this.aiAssistantService = aiAssistantService;
    }

    @PostMapping("/chat")
    public ChatResponseDTO chat(@Valid @RequestBody ChatRequestDTO request) {
        String response = aiAssistantService.chat(request.message());
        return new ChatResponseDTO(response);
    }
}
