package com.enigma.tekor.dto.response;

import java.time.LocalDateTime;

import com.enigma.tekor.constant.TestAttemptStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TestAttemptResponse {
    private String id;
    private String userId;
    private String packageId;
    private String transactionId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime finishTime;
    private Float score;
    private TestAttemptStatus status;
    private String aiEvaluationResult;
    private LocalDateTime createdAt;
    private String testPackageName;
}
