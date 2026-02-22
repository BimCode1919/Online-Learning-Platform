package org.oln.onlinelearningplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OptionRequestDTO {
    private String optionText;
    private boolean isCorrect;
}
