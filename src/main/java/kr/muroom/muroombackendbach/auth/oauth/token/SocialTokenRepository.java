package kr.muroom.muroombackendbach.auth.oauth.token;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SocialTokenRepository extends CrudRepository<SocialToken, String> {

  void deleteByUserId(Long userId);
}
