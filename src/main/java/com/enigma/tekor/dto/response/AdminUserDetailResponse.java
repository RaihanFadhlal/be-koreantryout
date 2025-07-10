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
public class AdminUserDetailResponse {
    private UUID id;
    private String fullName;
    private String username;
    private String email;
    private String imageUrl;
    private Boolean isVerified;
    private LocalDateTime createdAt;
    private List<TransactionSummaryResponse> transactions;
}
