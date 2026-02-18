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

  private static final String ZSET_PREFIX = "fav:U";
  private static final String COUNT_PREFIX = "favcnt:STUDIO:";

  public void addFavorite(Long studioId, Long musicianId) {
    studioService.isExistingStudioId(studioId);

    String zsetKey = zsetKey(musicianId);
    String countKey = countKey(studioId);

    // Redis 원자 처리 (ZADD NX + (added면) INCR)
    redisTemplate.execute(
        addFavoriteScript,
        List.of(zsetKey, countKey),
        String.valueOf(Instant.now().toEpochMilli()),
        studioId.toString()
    );
  }

  public void removeFavorite(Long studioId, Long musicianId) {
    studioService.isExistingStudioId(studioId);

    String zsetKey = zsetKey(musicianId);
    String countKey = countKey(studioId);

    // ZREM + (removed면) DECR (0 이하 방지)
    redisTemplate.execute(
        removeFavoriteScript,
        List.of(zsetKey, countKey),
        studioId.toString()
    );
  }

  @Transactional(readOnly = true)
  public PageImpl<StudioInfo> getFavoriteStudios(Long musicianId, Pageable pageable) {
    String zsetKey = zsetKey(musicianId);

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

  private String zsetKey(Long musicianId) {
    return ZSET_PREFIX + musicianId + ":STUDIO";
  }

  private String countKey(Long studioId) {
    return COUNT_PREFIX + studioId;
  }
}
