package org.ifarmr.payload.response;


import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ifarmr.enums.Gender;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDetailsResponse {
    private String message;
    private UserDetails userDetails;
}

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
class UserDetails {

    private Long id;

    private String fullName;

    private String username;

    private String email;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private String displayPhoto;

    private String businessName;
}