package com.studyplatform.repository;

import com.studyplatform.entity.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {
    List<ChatMessage> findByGroupIdOrderBySentAtDesc(UUID groupId, Pageable pageable);
    List<ChatMessage> findByGroupIdOrderBySentAtAsc(UUID groupId);
}
