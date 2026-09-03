package kr.muroom.muroombackendbach.studioboasting.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Table(name = "studio_boast_likes", uniqueConstraints = {
    @UniqueConstraint(
        name = "uk_studio_boast_like_musician_id_studio_boast_id",
        columnNames = {"musician_id", "studio_boast_id"}
    )
}, indexes = {
    @Index(name = "idx_studio_boast_like_musician_id", columnList = "musician_id"),
    @Index(name = "idx_studio_boast_like_studio_boast_id", columnList = "studio_boast_id")
})
public class StudioBoastLike extends CreatedDateEntity {

  @Id
  @Tsid
  @Column(name = "studio_boast_like_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "musician_id", nullable = false,
      foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
  private Musician musician;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "studio_boast_id", nullable = false,
      foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
  private StudioBoast studioBoast;
}