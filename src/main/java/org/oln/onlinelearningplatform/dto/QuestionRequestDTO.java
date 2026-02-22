package org.oln.onlinelearningplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionRequestDTO {
    private String questionText;
    private List<OptionRequestDTO> options;


}
