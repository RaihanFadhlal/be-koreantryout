package com.enigma.tekor.repository;

import com.enigma.tekor.constant.VocabularyCategories;
import com.enigma.tekor.entity.Vocabulary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VocabularyRepository extends JpaRepository<Vocabulary, UUID> {
    List<Vocabulary> findByVocabularyCategories(VocabularyCategories category);
}
