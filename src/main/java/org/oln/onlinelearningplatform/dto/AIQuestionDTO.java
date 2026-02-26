package org.oln.onlinelearningplatform.dto;

import lombok.Data;

import java.util.List;

@Data
public class AIQuestionDTO {
    private String questionText;
    private String explanation;
    private List<AIOptionDTO> options;
}
