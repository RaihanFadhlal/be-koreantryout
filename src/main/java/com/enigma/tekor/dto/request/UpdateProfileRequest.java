package com.enigma.tekor.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProfileRequest {
    @NotBlank(message = "Full name is required")
    @Pattern(regexp = "^[a-zA-Z\\s]{2,}$",
            message = "Full name must be at least 2 characters, letters only (no numbers or symbols)")
    private String fullName;
}
