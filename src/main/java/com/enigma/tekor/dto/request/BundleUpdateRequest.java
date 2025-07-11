package com.enigma.tekor.dto.request;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class BundleUpdateRequest {
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal discountPrice;
}
