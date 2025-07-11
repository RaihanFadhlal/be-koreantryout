package com.enigma.tekor.dto.request;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class BundleRequest {
    private String name;
    private String description;
    private String imageUrl;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private List<UUID> packageIds;
}
