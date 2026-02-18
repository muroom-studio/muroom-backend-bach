package kr.muroom.muroombackendbach.studio.application;

import kr.muroom.muroombackendbach.studioboasting.presentation.dto.response.StudioBoastDetailResponse.StudioInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Transactional
@Service
@RequiredArgsConstructor
public class StudioFavoriteService {

  private final RedisTemplate<String, String> redisTemplate;
  private final DefaultRedisScript<Long> addFavoriteScript;
  private final DefaultRedisScript<Long> removeFavoriteScript;

  private final StudioService studioService;

  private static final String ZSET_PREFIX = "fav:";
  private static final String SET_PREFIX = "favset:";
  private static final String COUNT_PREFIX = "favcnt:STUDIO:";
  private static final String STUDIO_SUFFIX = ":STUDIO";

  /**
   * subjectId: "U:{userId}" or "G:{anonymousId}"
   */
  public void addFavorite(Long studioId, String subjectId) {
    studioService.isExistingStudioId(studioId);

    Subject subject = parseSubject(subjectId);

    String zsetKey = zsetKey(subject);
    String setKey = setKey(subject);
    String countKey = countKey(studioId);

    redisTemplate.execute(
        addFavoriteScript,
        List.of(zsetKey, countKey, setKey),
        String.valueOf(Instant.now().toEpochMilli()),
        studioId.toString()
    );
  }

  public void removeFavorite(Long studioId, String subjectId) {
    studioService.isExistingStudioId(studioId);

    Subject subject = parseSubject(subjectId);

    String zsetKey = zsetKey(subject);
    String setKey = setKey(subject);
    String countKey = countKey(studioId);

    redisTemplate.execute(
        removeFavoriteScript,
        List.of(zsetKey, countKey, setKey),
        studioId.toString()
    );
  }

  @Transactional(readOnly = true)
  public PageImpl<StudioInfo> getFavoriteStudios(String subjectId, Pageable pageable) {
    Subject subject = parseSubject(subjectId);
    String zsetKey = zsetKey(subject);

    long start = pageable.getOffset();
    long end = start + pageable.getPageSize() - 1;

    Set<String> idStrings = redisTemplate.opsForZSet().reverseRange(zsetKey, start, end);
    if (idStrings == null || idStrings.isEmpty()) {
      return new PageImpl<>(List.of(), pageable, 0);
    }

    List<Long> studioIds = idStrings.stream().map(Long::valueOf).toList();

    Long total = redisTemplate.opsForZSet().size(zsetKey);
    long totalCount = (total == null) ? 0 : total;

    List<StudioInfo> studios = studioService.getStudioInfoByIds(studioIds);

    return new PageImpl<>(studios, pageable, totalCount);
  }

  @Transactional(readOnly = true)
  public boolean isFavorite(String subjectId, Long studioId) {
    if (subjectId == null || subjectId.isBlank() || studioId == null) {
      return false;
    }

    Subject subject = parseSubject(subjectId);
    String setKey = setKey(subject);

    Boolean member = redisTemplate.opsForSet().isMember(setKey, studioId.toString());
    return Boolean.TRUE.equals(member);
  }

  // ------------------------
  // Key builders
  // ------------------------

  private String zsetKey(Subject subject) {
    return ZSET_PREFIX + subject.prefix + subject.id + STUDIO_SUFFIX;
  }

  private String setKey(Subject subject) {
    return SET_PREFIX + subject.prefix + subject.id + STUDIO_SUFFIX;
  }

  private String countKey(Long studioId) {
    return COUNT_PREFIX + studioId;
  }

  // ------------------------
  // Subject parsing
  // ------------------------

  private Subject parseSubject(String subjectId) {
    // expected: "U:123" or "G:uuid"
    if (subjectId == null) {
      throw new IllegalArgumentException("subjectId is null");
    }

    int idx = subjectId.indexOf(':');
    if (idx <= 0 || idx == subjectId.length() - 1) {
      throw new IllegalArgumentException("Invalid subjectId format: " + subjectId);
    }

    String prefix = subjectId.substring(0, idx);
    String id = subjectId.substring(idx + 1);

    if (!prefix.equals("U") && !prefix.equals("G")) {
      throw new IllegalArgumentException("Invalid subject prefix: " + prefix);
    }
    if (id.isBlank()) {
      throw new IllegalArgumentException("Empty subject id");
    }

    return new Subject(prefix, id);
  }

  private record Subject(String prefix, String id) {

  }
}
