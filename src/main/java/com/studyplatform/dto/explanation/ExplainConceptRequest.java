package com.studyplatform.dto.explanation;

import com.studyplatform.enums.DetailLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ExplainConceptRequest {

    @NotBlank(message = "Concept is required")
    private String concept;

    @NotNull(message = "Detail level is required")
    private DetailLevel detailLevel;
}
