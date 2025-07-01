package com.enigma.tekor.dto.response;

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
public class BundleResponse {
    private UUID id;
    private String name;
    private String description;
    private BigDecimal price;
    private List<PackageInBundleResponse> packages;
}
