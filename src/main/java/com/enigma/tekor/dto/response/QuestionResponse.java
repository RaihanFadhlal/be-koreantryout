package com.enigma.tekor.dto.response;

import com.enigma.tekor.constant.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuestionResponse {
    private UUID id;
    private String questionText;
    private String imageUrl;
    private String audioUrl;
    private QuestionType questionType;
    private List<OptionResponse> options;
}
