package com.studyplatform.dto.note;

import lombok.Data;

@Data
public class UpdateNoteRequest {
    private String title;
    private String content;
    private Boolean sharedWithGroup;
}
