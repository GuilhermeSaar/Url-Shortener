package com.urlshortener.user.repository;

import com.urlshortener.user.model.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserModel, Long> {

    Optional<UserModel> findByEmail(String email);

    boolean existsByEmail(String email);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE UserModel u SET u.tokenVersion = u.tokenVersion + 1 WHERE u.id = :id AND u.tokenVersion = :version")
    int incrementTokenVersion(@Param("id") Long id, @Param("version") Long version);
}
