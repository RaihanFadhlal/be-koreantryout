package com.enigma.tekor.entity;

import java.util.UUID;

import com.enigma.tekor.constant.VocabularyCategories;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "vocabularies")
public class Vocabulary {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "korean_word", nullable = false)
    private String koreanWord;

    @Column(nullable = false)
    private String translation;

    @Column(name = "vocabulary_category", nullable = false)
    private VocabularyCategories vocabularyCategories;
}
