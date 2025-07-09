package com.enigma.tekor.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuestionReviewResponse {
    private UUID questionId;
    private String questionText;
    private String questionAudio;
    private String questionImage;
    private List<OptionResponse> options;
    private UUID selectedOptionId;
    private UUID correctOptionId;
    private Boolean isCorrect;
    private String correctAnswerText;
}
