package com.enigma.tekor.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TestAttemptDetailResponse {
    private UUID id;
    private String testPackageName;
    private LocalDateTime startTime;
    private LocalDateTime finishTime;
    private List<QuestionResponse> questions;
    private List<UserAnswerResponse> userAnswers;
}
