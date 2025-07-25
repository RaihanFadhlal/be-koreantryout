package com.enigma.tekor.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubmitAttemptResponse {
    private Integer totalCorrect;
    private Integer totalIncorrect;
    private Float score;
    private LocalDateTime completionTime;
}
