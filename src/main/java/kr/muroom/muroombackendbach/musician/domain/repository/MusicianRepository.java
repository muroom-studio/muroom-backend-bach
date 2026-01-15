package kr.muroom.muroombackendbach.musician.domain.repository;

import kr.muroom.muroombackendbach.musician.domain.entity.Musician;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MusicianRepository extends JpaRepository<Musician, Long> {

  boolean existsByNickname(String nickname);

  boolean existsByPhoneNumber(String phone);

}
