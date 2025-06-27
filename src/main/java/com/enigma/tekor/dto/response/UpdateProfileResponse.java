package com.enigma.tekor.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProfileResponse {
    private Long id;
    private String fullName;
    private String username;
    private String email;
    private String imageUrl;
    private Boolean isVerified;
    
}
