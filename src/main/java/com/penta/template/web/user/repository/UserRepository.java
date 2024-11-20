package com.penta.template.web.user.repository;

import com.penta.template.web.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // TODO
    Optional<User> findByUserId(String userId);

    @Query("select u.refreshToken from User u where u.userId = :userId")
    String findByRefreshTokenByUserId(@Param("userId") String userId);


}
