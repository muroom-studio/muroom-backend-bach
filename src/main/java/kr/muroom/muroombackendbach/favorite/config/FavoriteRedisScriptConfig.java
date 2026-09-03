package kr.muroom.muroombackendbach.favorite.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.script.DefaultRedisScript;

@Configuration
public class FavoriteRedisScriptConfig {

  /**
   * addFavoriteScript: - ZADD NX로 중복 없이 추가 - 실제로 추가된 경우에만 INCR로 카운트 증가 return: added(1=새로 추가, 0=이미
   * 존재)
   */
  @Bean
  public DefaultRedisScript<Long> addFavoriteScript() {
    DefaultRedisScript<Long> script = new DefaultRedisScript<>();
    script.setResultType(Long.class);
    script.setScriptText(
        // KEYS[1] = zsetKey, KEYS[2] = countKey, KEYS[3] = setKey
        "local added = redis.call('ZADD', KEYS[1], 'NX', ARGV[1], ARGV[2]); " +
            "if added == 1 then " +
            "  redis.call('SADD', KEYS[3], ARGV[2]); " +
            "  redis.call('INCR', KEYS[2]); " +
            "end; " +
            "return added;"
    );
    return script;
  }

  /**
   * removeFavoriteScript: - ZREM으로 제거 - 실제로 제거된 경우에만 DECR로 카운트 감소 - 카운트 음수 방지(0 이하 내려가면 0으로 보정)
   * return: removed(1=실제 제거, 0=원래 없음)
   */
  @Bean
  public DefaultRedisScript<Long> removeFavoriteScript() {
    DefaultRedisScript<Long> script = new DefaultRedisScript<>();
    script.setResultType(Long.class);
    script.setScriptText(
        // KEYS[1] = zsetKey, KEYS[2] = countKey, KEYS[3] = setKey
        // ARGV[1]=member(studioId)
        "local removed = redis.call('ZREM', KEYS[1], ARGV[1])\n"
            + "if removed == 1 then\n"
            + "  redis.call('SREM', KEYS[3], ARGV[1])\n"
            + "  local newVal = redis.call('DECR', KEYS[2])\n"
            + "  if newVal < 0 then redis.call('SET', KEYS[2], 0) end\n"
            + "end\n"
            + "return removed"
    );
    return script;
  }

  /**
   * migrateGuestToUserFavoriteScript:
   * - guest ZSET의 모든 멤버를 user로 merge
   * - 중복이면 count 증가 안 함
   * - score는 max(기존, 게스트)로 유지
   * - 완료 후 guest 키 삭제
   *
   * return: 새로 user에 추가된 찜 개수
   */
  @Bean
  public DefaultRedisScript<Long> migrateGuestToUserFavoriteScript() {
    DefaultRedisScript<Long> script = new DefaultRedisScript<>();
    script.setResultType(Long.class);
    script.setScriptText(
            // KEYS:
            // 1 = guestZSetKey
            // 2 = guestSetKey
            // 3 = userZSetKey
            // 4 = userSetKey
            // 5 = countKeyPrefix (예: "favcnt:STUDIO:")
            "local guestZ = KEYS[1]\n" +
                    "local guestSet = KEYS[2]\n" +
                    "local userZ = KEYS[3]\n" +
                    "local userSet = KEYS[4]\n" +
                    "local countPrefix = KEYS[5]\n" +

                    // 게스트 찜 목록 전체 가져오기
                    "local items = redis.call('ZRANGE', guestZ, 0, -1, 'WITHSCORES')\n" +
                    "local moved = 0\n" +

                    "for i = 1, #items, 2 do\n" +
                    "  local studioId = items[i]\n" +
                    "  local score = tonumber(items[i+1])\n" +

                    // 유저가 이미 가지고 있는지 확인
                    "  local exists = redis.call('SISMEMBER', userSet, studioId)\n" +

                    "  if exists == 1 then\n" +
                    "    -- 이미 존재하면 score만 최신으로 갱신\n" +
                    "    local curScore = redis.call('ZSCORE', userZ, studioId)\n" +
                    "    if curScore then\n" +
                    "      if score > tonumber(curScore) then\n" +
                    "        redis.call('ZADD', userZ, score, studioId)\n" +
                    "      end\n" +
                    "    else\n" +
                    "      -- 비정상 케이스 방어 (SET에는 있는데 ZSET에 없는 경우)\n" +
                    "      redis.call('ZADD', userZ, score, studioId)\n" +
                    "    end\n" +
                    "  else\n" +
                    "    -- 새로 추가\n" +
                    "    redis.call('SADD', userSet, studioId)\n" +
                    "    redis.call('ZADD', userZ, score, studioId)\n" +
                    "    redis.call('INCR', countPrefix .. studioId)\n" +
                    "    moved = moved + 1\n" +
                    "  end\n" +
                    "end\n" +

                    // 게스트 키 정리
                    "redis.call('DEL', guestZ)\n" +
                    "redis.call('DEL', guestSet)\n" +

                    "return moved"
    );
    return script;
  }
}
