package com.studyplatform.dto.note;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.UUID;

@Data
public class CreateNoteRequest {
    @NotBlank(message = "Title is required")
    private String title;
    @NotBlank(message = "Content is required")
    private String content;
    private UUID groupId;
    private boolean sharedWithGroup;
}
