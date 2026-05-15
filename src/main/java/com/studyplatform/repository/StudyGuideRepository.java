package com.studyplatform.repository;

import com.studyplatform.entity.StudyGuide;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StudyGuideRepository extends JpaRepository<StudyGuide, UUID> {

    List<StudyGuide> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<StudyGuide> findByTopicIdOrderByCreatedAtDesc(UUID topicId);
}
