package kr.muroom.muroombackendbach.withdrawal.domain.repository;

import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MusicianHardDeleteDao {

  private final NamedParameterJdbcTemplate jdbc;

  public List<Long> findTargetMusicianIds(OffsetDateTime now, int limit) {
    String sql = """
        select musician_id
        from musicians
        where deleted_at is not null
          and hard_delete_at is not null
          and hard_delete_at <= :now
        order by hard_delete_at asc
        limit :limit
        """;

    MapSqlParameterSource params = new MapSqlParameterSource()
        .addValue("now", now)
        .addValue("limit", limit);

    return jdbc.query(sql, params, (rs, _) -> rs.getLong("musician_id"));
  }

  public void deleteStudioBoastCommentLikeByMusicianIds(List<Long> musicianIds) {
    if (musicianIds.isEmpty()) {
      return;
    }

    String sql = """
        delete from studio_boast_comment_likes
        where musician_id in (:musicianIds)
        """;
    jdbc.update(sql, new MapSqlParameterSource("musicianIds", musicianIds));
  }

  public void deleteStudioBoastCommentByMusicianIds(List<Long> musicianIds) {
    if (musicianIds.isEmpty()) {
      return;
    }

    String sql = """
        delete from studio_boast_comments
        where creator_user_id in (:musicianIds)
        """;
    jdbc.update(sql, new MapSqlParameterSource("musicianIds", musicianIds));
  }

  public int deleteMusicianByMusicianIds(List<Long> musicianIds) {
    if (musicianIds.isEmpty()) {
      return 0;
    }

    String sql = """
        delete from musicians
        where musician_id in (:musicianIds)
        """;
    return jdbc.update(sql, new MapSqlParameterSource("musicianIds", musicianIds));
  }

}
