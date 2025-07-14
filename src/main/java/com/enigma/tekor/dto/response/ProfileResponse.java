package com.enigma.tekor.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileResponse {
    private UUID id;
    private String fullName;
    private String username;
    private String email;
    private String imageUrl;
    private Boolean isVerified;
    private LocalDateTime createdAt;
}
