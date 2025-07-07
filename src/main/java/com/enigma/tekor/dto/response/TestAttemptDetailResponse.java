package com.enigma.tekor.dto.response;

import com.enigma.tekor.constant.TestAttemptStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TestAttemptDetailResponse {
    private UUID id;
    private String testPackageName;
    private Date startTime;
    private Long remainingDuration;
    private List<QuestionResponse> questions;
    private List<UserAnswerResponse> userAnswers;
}
