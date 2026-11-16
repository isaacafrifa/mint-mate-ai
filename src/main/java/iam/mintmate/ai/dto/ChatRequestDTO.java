package iam.mintmate.ai.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Incoming payload for a chat request.
 */
public record ChatRequestDTO(@NotBlank String message) {
}
