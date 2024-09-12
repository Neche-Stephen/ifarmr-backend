package org.ifarmr.payload.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.ifarmr.enums.Gender;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRegisterRequest {

    @NotBlank(message = "Full Name is required")
    @Pattern(regexp = "^[a-zA-Z ]+$", message = "Full Name must contain only alphabets and spaces")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Username is required")
    @Pattern(regexp = "^(?=.*[a-zA-Z])A(?=.*\\d)[a-zA-Z\\d]+$", message = "Username must contain at least one alphabet and one number, and only alphabets and numbers are allowed")
    private String userName;
//testing it
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$%*?&])[A-Za-z\\d@$%*?&]{8,}$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character")
    @NotBlank(message = "Password is required")
    private String password;

    @NotNull(message = "Gender is required")
    private Gender gender;

}

