package com.enigma.tekor.dto.response;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InProgressAttempt {

    private String attemptId;
    private TestPackageResponse testPackage;
    private LocalDateTime startTime;
    private LocalDateTime finishTime;
    
}
