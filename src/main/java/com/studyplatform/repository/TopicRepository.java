package com.studyplatform.repository;

import com.studyplatform.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TopicRepository extends JpaRepository<Topic, UUID> {

    List<Topic> findByUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<Topic> findByUserIdAndNameIgnoreCase(UUID userId, String name);

    boolean existsByUserIdAndNameIgnoreCase(UUID userId, String name);
}
