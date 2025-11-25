package kr.muroom.muroombackendbach.user.domain.repository;

import kr.muroom.muroombackendbach.user.domain.entity.Owner;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OwnerRepository extends JpaRepository<Owner, Long> {
    boolean existsByNickname(String nickname);
}
