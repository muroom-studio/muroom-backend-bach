package kr.muroom.muroombackendbach.subway.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import kr.muroom.muroombackendbach.common.domain.SoftDeletableEntity;
import kr.muroom.muroombackendbach.common.util.tsid.Tsid;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "subway_stations_nearby_studios")
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE subway_stations_nearby_studios SET deleted_at = NOW() WHERE subway_station_nearby_studio_id = ?")
public class SubwayStationNearbyStudio extends SoftDeletableEntity {

  @Id
  @Tsid
  @Column(name = "subway_station_nearby_studio_id")
  private Long id;

  @Column(name = "sequence", nullable = false)
  private Integer sequence;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "subway_station_id", nullable = false)
  private SubwayStation subwayStation;

  @Column(name = "studio_id", nullable = false)
  private Long studioId;

  public void updateSequence(Integer sequence) {
    this.sequence = sequence;
  }
}
