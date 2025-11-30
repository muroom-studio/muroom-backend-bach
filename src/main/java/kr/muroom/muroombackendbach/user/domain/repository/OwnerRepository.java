package kr.muroom.muroombackendbach.user.domain.repository;

import java.util.Optional;
import kr.muroom.muroombackendbach.user.domain.entity.Owner;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OwnerRepository extends JpaRepository<Owner, Long> {

  boolean existsByNickname(String nickname);

  boolean existsByEmail(String email);

  Optional<Owner> findByEmail(String nickname);
}
