package com.studyplatform.repository;

import com.studyplatform.entity.ConceptExplanation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ConceptExplanationRepository extends JpaRepository<ConceptExplanation, UUID> {

    List<ConceptExplanation> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
