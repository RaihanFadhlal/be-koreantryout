package com.enigma.tekor.dto.request;

import jakarta.validation.constraints.NotNull;
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
public class ChangePasswordRequest {
    @NotNull(message="Current password cannot empty")
    private String currentPassword;

    @NotNull(message="New password cannot empty")
    private String newPassword;

    @NotNull(message="Confirm new password cannot empty")
    private String confirmNewPassword;
}