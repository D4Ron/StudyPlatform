package com.studyplatform.repository;

import com.studyplatform.entity.StudyGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudyGroupRepository extends JpaRepository<StudyGroup, UUID> {

    Optional<StudyGroup> findByInviteCode(String inviteCode);

    boolean existsByInviteCode(String inviteCode);
}
