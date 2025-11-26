package kr.muroom.muroombackendbach.user.domain.repository;

import kr.muroom.muroombackendbach.user.domain.entity.Musician;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MusicianRepository extends JpaRepository<Musician, Long> {
    boolean existsByNickname(String nickname);
    Optional<Musician> findByNameAndPhoneNumber(String name,  String phoneNumber);
}
