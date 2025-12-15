package kr.muroom.muroombackendbach.user.domain.repository;

import kr.muroom.muroombackendbach.user.domain.entity.Musician;
import kr.muroom.muroombackendbach.user.domain.entity.OAuthProvider;
import kr.muroom.muroombackendbach.user.domain.entity.SocialAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {

  Optional<SocialAccount> findByProviderAndProviderUserId(OAuthProvider provider,
      String providerId);

  boolean existsByMusicianAndProviderAndProviderUserId(
      Musician musician,
      OAuthProvider provider,
      String providerId
  );

  Optional<SocialAccount> findByMusicianId(Long musicianId);
}
