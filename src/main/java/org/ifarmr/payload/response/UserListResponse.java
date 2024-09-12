package org.ifarmr.payload.response;

import lombok.*;
import org.ifarmr.enums.Gender;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserListResponse {
    private Long id;
    private String username;
    private String email;
    private Gender gender;
    private String dateJoined;
    private String lastActive;
}
