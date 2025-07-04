package com.enigma.tekor.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class TransactionDetailResponse {

    private String id;
    private String midtransOrderId;
    private BigDecimal amount;
    private String status;
    private LocalDateTime createdAt;
    private TestPackageResponse testPackage;
    private BundleResponse bundle;
    
}
