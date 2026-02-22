package kr.muroom.muroombackendbach.studio.application.query;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import kr.muroom.muroombackendbach.common.util.SubjectParser;
import kr.muroom.muroombackendbach.common.util.SubjectParser.Subject;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudioFavoriteQueryService {

  private final RedisTemplate<String, String> redisTemplate;

  public boolean isFavoriteStudio(Long studioId, String subjectId) {
    Subject subject = SubjectParser.parse(subjectId);
    String setKey = "favset:" + subject.prefix() + subject.id() + ":STUDIO";
    Boolean member = redisTemplate.opsForSet().isMember(setKey, studioId.toString());
    return Boolean.TRUE.equals(member);
  }

  public Set<Long> getFavoriteStudioIds(String subjectId) {
    Subject subject = SubjectParser.parse(subjectId);
    String setKey = "favset:" + subject.prefix() + subject.id() + ":STUDIO";

    Set<String> members = redisTemplate.opsForSet().members(setKey);
    if (members == null || members.isEmpty()) {
      return Collections.emptySet();
    }

    Set<Long> ids = new HashSet<>(members.size());
    for (String m : members) {
      ids.add(Long.valueOf(m));
    }
    return ids;
  }
}
