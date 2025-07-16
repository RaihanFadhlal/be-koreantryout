package com.enigma.tekor.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
public class RegisterRequest {
    @NotBlank(message = "Full name is required")
    @Pattern(regexp = "^[a-zA-Z\\s]{2,}$", 
             message = "Full name must be at least 2 characters, letters only (no numbers or symbols)")
    private String fullName;

    @NotBlank(message = "Username is required")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9]{3,}$", 
             message = "Username must be 4+ characters, start with letter, no spaces")
    private String username;

    @NotBlank(message = "Email is required")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", 
             message = "Invalid email format. Example: user@example.com")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
}
