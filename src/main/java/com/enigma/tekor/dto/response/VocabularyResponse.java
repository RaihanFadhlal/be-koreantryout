package com.enigma.tekor.dto.response;

import java.util.List;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;



@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VocabularyResponse {
    private String fileName;
    private int uploadedCount;
    private List<String> categories;
    
}
