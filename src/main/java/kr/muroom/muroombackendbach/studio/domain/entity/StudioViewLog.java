package kr.muroom.muroombackendbach.studio.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import kr.muroom.muroombackendbach.common.util.tsid.Tsid;
import kr.muroom.muroombackendbach.user.domain.entity.Musician;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "studio_view_logs")
public class StudioViewLog {

  @Id
  @Tsid
  @Column(name = "studio_view_log_id")
  private Long id;

  @Column
  private String anonymousUserId;

  @CreationTimestamp
  @Column(name = "viewed_at", nullable = false, updatable = false)
  private OffsetDateTime viewedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "musician_id")
  private Musician musician;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "studio_id", nullable = false)
  private Studio studio;

  public static StudioViewLog byMusician(Musician musician, Studio studio) {
    return StudioViewLog.builder()
        .musician(musician)
        .studio(studio)
        .build();
  }

  public static StudioViewLog byAnonymousUser(String anonymousUserId, Studio studio) {
    return StudioViewLog.builder()
        .anonymousUserId(anonymousUserId)
        .studio(studio)
        .build();
  }
}
