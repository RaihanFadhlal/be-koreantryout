package com.enigma.tekor.dto.response;

import com.enigma.tekor.constant.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionSummaryResponse {
    private String transactionId;
    private TransactionStatus status;
    private BigDecimal amount;
    private String purchasedItemName;
    private LocalDateTime transactionDate;
}
