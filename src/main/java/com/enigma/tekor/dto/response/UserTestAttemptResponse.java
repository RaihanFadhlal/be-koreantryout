package com.enigma.tekor.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserTestAttemptResponse {
    private List<ReadyTestPackage> readyToStart;
    private List<InProgressAttempt> inProgress;
    
}
