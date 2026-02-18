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
        "local removed = redis.call('ZREM', KEYS[1], ARGV[1]); " +
            "if removed == 1 then " +
            "  redis.call('SREM', KEYS[3], ARGV[1]); " +
            "  local newVal = redis.call('DECR', KEYS[2]); " +
            "  if newVal < 0 then redis.call('SET', KEYS[2], 0); end; " +
            "end; " +
            "return removed;"
    );
    return script;
  }
}
