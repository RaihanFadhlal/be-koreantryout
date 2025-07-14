package com.enigma.tekor.dto.request;

import com.enigma.tekor.constant.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateQuestionRequest {
    private String questionText;
    private String questionDesc;
    private QuestionType questionType;
    private String imageUrl;
    private String audioUrl;
    private Integer number;
    private List<String> options;
    private String correctOption;
}