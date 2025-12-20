package kr.muroom.muroombackendbach.user.domain.repository;

import java.util.Optional;
import kr.muroom.muroombackendbach.user.domain.entity.Musician;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MusicianRepository extends JpaRepository<Musician, Long> {

  boolean existsByNickname(String nickname);

  Optional<Musician> findByNameAndPhoneNumber(String name, String phoneNumber);

  boolean existsByPhoneNumber(String phone);

}
