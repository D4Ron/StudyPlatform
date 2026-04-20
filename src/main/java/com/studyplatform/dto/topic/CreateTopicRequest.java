package com.studyplatform.dto.topic;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateTopicRequest {

    @NotBlank(message = "Topic name is required")
    @Size(max = 100, message = "Topic name must be under 100 characters")
    private String name;

    @Size(max = 500, message = "Specificity must be under 500 characters")
    private String specificity;
}
