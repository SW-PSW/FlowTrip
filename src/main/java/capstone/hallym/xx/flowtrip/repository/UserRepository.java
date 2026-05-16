package capstone.hallym.xx.flowtrip.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import capstone.hallym.xx.flowtrip.entity.AppUser;

public interface UserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByUsername(String username);

    boolean existsByUsername(String username);
}