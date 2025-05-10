package com.IKov.AuthService.repository;

import com.IKov.AuthService.entity.Profile.Profile;
import com.IKov.AuthService.entity.Profile.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("""
        UPDATE Profile p
        SET p.status = :status
        WHERE p.email = :email
    """)
    void updateProfileStatus(@Param("email") String email,
                             @Param("status") Status status);

    List<Profile> findAllByTag(String tag);

    Profile findByTag(String tag);
}
