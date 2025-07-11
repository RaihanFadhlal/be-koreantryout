package com.enigma.tekor.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReadyTestPackage {

    private String transactionId;
    private TestPackageResponse testPackage;  
    private LocalDateTime purchaseDate;
    
}
