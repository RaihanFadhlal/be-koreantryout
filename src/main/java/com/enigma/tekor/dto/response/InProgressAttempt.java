package com.enigma.tekor.dto.response;

import java.util.Date;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InProgressAttempt {

    private String attemptId;
    private TestPackageResponse testPackage;
    private Date startTime;
    private Long remainingDuration; 
    
}
