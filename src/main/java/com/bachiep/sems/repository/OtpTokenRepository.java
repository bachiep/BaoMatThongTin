package com.bachiep.sems.repository;

import com.bachiep.sems.entity.OtpToken;
import com.bachiep.sems.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {
    Optional<OtpToken> findByTokenAndUser(String token, User user);
    void deleteByUser(User user);
}
