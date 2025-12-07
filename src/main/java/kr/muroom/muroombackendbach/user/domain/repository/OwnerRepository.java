package kr.muroom.muroombackendbach.user.domain.repository;

import java.util.Optional;
import kr.muroom.muroombackendbach.user.domain.entity.Owner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OwnerRepository extends JpaRepository<Owner, Long> {

  boolean existsByNickname(String nickname);

  boolean existsByEmail(String email);

  Optional<Owner> findByEmail(String email);

  Optional<Owner> findByPhoneNumber(String phoneNumber);

  @Query(value = "SELECT nextval('owner_id_seq')", nativeQuery = true)
  Long getNextNicknameSequence();

  Boolean existsByPhoneNumber(String phoneNumber);
}
