package com.enigma.tekor.dto.response;



import java.util.Date;

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
    private Date startTime;
    private Date endTime;
    private Float score;
    private TestAttemptStatus status;
    private String aiEvaluationResult;
    private Date createdAt;
    
}
