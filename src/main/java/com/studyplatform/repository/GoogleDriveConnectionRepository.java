package com.studyplatform.repository;

import com.studyplatform.entity.GoogleDriveConnection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface GoogleDriveConnectionRepository extends JpaRepository<GoogleDriveConnection, UUID> {

    Optional<GoogleDriveConnection> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);

    void deleteByUserId(UUID userId);
}
