package com.enigma.tekor.dto.request;

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
public class TestAttemptRequest {

    private String id;
    private String transactionId; 
    private Date endTime;
    private Float score;
    private TestAttemptStatus status;
    private String aiEvaluationResult;
    
}
