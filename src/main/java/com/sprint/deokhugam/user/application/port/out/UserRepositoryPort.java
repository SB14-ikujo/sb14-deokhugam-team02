package com.sprint.deokhugam.user.application.port.out;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import com.sprint.deokhugam.user.domain.User;

public interface UserRepositoryPort extends JpaRepository<User, UUID> {

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    Optional<User> findByEmailAndIsDeletedFalse(String email);

    Optional<User> findByIdAndIsDeletedFalse(UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.id = :id AND u.isDeleted = false")
    Optional<User> findByIdAndIsDeletedFalseForUpdate(@Param("id") UUID id);

    @Modifying
    @Query("delete from User u where u.isDeleted = true and u.updatedAt < :cutoff")
    int deleteSoftDeletedUsersBefore(@Param("cutoff") Instant cutoff);
}
