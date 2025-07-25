package com.enigma.tekor.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateTestPackageRequest {
    private String name;
    private String description;
    private Double price;
    private Double discountPrice;
    private MultipartFile image;
}
