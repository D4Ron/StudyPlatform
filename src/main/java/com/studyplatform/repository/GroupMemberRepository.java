package com.studyplatform.repository;

import com.studyplatform.entity.GroupMember;
import com.studyplatform.enums.GroupRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, UUID> {

    List<GroupMember> findByUserId(UUID userId);

    List<GroupMember> findByGroupId(UUID groupId);

    Optional<GroupMember> findByUserIdAndGroupId(UUID userId, UUID groupId);

    boolean existsByUserIdAndGroupId(UUID userId, UUID groupId);

    long countByGroupId(UUID groupId);

    @Query("SELECT gm FROM GroupMember gm WHERE gm.group.id = :groupId AND gm.role = :role")
    List<GroupMember> findByGroupIdAndRole(UUID groupId, GroupRole role);
}
