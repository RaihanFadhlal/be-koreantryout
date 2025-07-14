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
public class TransactionResponse {
    private String orderId;
    private String redirectUrl;
    private String transactionStatus;
    private BigDecimal amount;
    private String packageName;
    private String bundleName;
    private LocalDateTime createdAt;
}
