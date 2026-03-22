package com.sample.marketplace.repositories;

import com.sample.marketplace.models.User;
import com.sample.marketplace.models.enums.Role;
import com.sample.marketplace.models.enums.UserStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailAndRole(String email, Role role);

    boolean existsByEmailAndRole(String email, Role role);

    long countByRole(Role role);

    long countByStatus(UserStatus status);

    List<User> findAllByRole(Role role);

    List<User> findAllByStatus(UserStatus status);

    List<User> findAllByRoleAndStatus(Role role, UserStatus status);
}
