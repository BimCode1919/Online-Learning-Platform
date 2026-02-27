package org.oln.onlinelearningplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AIQuestionDTO {
    private String questionText;
    private String explanation;
    private List<AIOptionDTO> options;
}
