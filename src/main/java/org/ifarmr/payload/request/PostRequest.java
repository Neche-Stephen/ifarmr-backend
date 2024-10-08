package org.ifarmr.payload.request;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostRequest {

    @NotBlank(message = "Title is required!")
    private String title;

    @NotBlank(message = "Content is required!")
    private String content;

    private MultipartFile file;
}
