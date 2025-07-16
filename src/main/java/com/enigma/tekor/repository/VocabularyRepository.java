package com.enigma.tekor.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.enigma.tekor.constant.VocabularyCategories;
import com.enigma.tekor.entity.Vocabulary;

@Repository
public interface VocabularyRepository extends JpaRepository<Vocabulary, UUID> {
    Page<Vocabulary> findByVocabularyCategories(VocabularyCategories category, Pageable pageable);
    Boolean existsByKoreanWord(String koreanWord);
}
