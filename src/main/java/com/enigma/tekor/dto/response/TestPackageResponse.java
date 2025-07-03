package com.enigma.tekor.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TestPackageResponse {
    private String id;
    private String name;
    private String description;
    private String imageUrl;
    private Double price;
    private Double discountPrice;
}
