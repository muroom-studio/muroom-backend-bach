package kr.muroom.muroombackendbach.search.domain.entity;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import kr.muroom.muroombackendbach.common.domain.CreatedDateEntity;
import kr.muroom.muroombackendbach.common.util.tsid.Tsid;
import kr.muroom.muroombackendbach.musician.domain.entity.Musician;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "search_logs")
@AttributeOverride(name = "createdAt", column = @Column(name = "searched_at",
    nullable = false))
public class SearchLog extends CreatedDateEntity {

  @Id
  @Tsid
  @Column(name = "search_log_id")
  private Long id;

  @Column(name = "search_keyword", nullable = false)
  private String keyword;

  @Column(name = "anonymous_user_id")
  private String anonymousUserId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "musician_id")
  private Musician musician;

  public static SearchLog byMusician(Musician musician, String keyword) {
    return SearchLog.builder()
        .musician(musician)
        .keyword(keyword)
        .build();
  }

  public static SearchLog byAnonymousUser(String anonymousUserId, String keyword) {
    return SearchLog.builder()
        .anonymousUserId(anonymousUserId)
        .keyword(keyword)
        .build();
  }
}
