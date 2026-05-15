package com.studyplatform.repository;

import com.studyplatform.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NoteRepository extends JpaRepository<Note, UUID> {
    List<Note> findByAuthorIdOrderByUpdatedAtDesc(UUID authorId);
    List<Note> findByGroupIdAndSharedWithGroupTrueOrderByUpdatedAtDesc(UUID groupId);
}
