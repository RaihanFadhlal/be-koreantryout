package com.enigma.tekor.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserAnswerEvaluationRequest {
    private String questionType;
    private Boolean isCorrect;
}
