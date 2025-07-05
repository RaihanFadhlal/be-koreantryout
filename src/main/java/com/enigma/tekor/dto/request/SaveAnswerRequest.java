package com.enigma.tekor.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SaveAnswerRequest {

    @NotNull(message = "Question ID is required")
    private UUID questionId;

    @NotNull(message = "Option ID is required")
    private UUID optionId;

    @NotNull(message = "Remaining time is required")
    private Long remainingTimeInSeconds;

}
