package kr.muroom.muroombackendbach.owner.domain.repository;

import java.util.Optional;
import kr.muroom.muroombackendbach.owner.domain.entity.Owner;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OwnerRepository extends JpaRepository<Owner, Long> {

  boolean existsByNickname(String nickname);

  boolean existsByEmail(String email);

  Optional<Owner> findByEmail(String email);

  Optional<Owner> findByPhoneNumber(String phoneNumber);

  Boolean existsByPhoneNumber(String phoneNumber);
}
