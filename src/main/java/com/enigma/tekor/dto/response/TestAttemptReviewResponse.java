package com.enigma.tekor.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TestAttemptReviewResponse {
    private UUID testAttemptId;
    private String testPackageName;
    private LocalDateTime finishTime;
    private List<QuestionReviewResponse> questions;
}
