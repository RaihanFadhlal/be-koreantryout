package com.enigma.tekor.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AIEvaluationRequest {
    private UUID testAttemptId;
    private Float score;
    private List<UserAnswerEvaluationRequest> userAnswers;
}
