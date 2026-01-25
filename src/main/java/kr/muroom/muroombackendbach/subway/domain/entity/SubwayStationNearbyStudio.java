package kr.muroom.muroombackendbach.subway.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import kr.muroom.muroombackendbach.common.domain.CreatedDateEntity;
import kr.muroom.muroombackendbach.common.util.tsid.Tsid;
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
@Table(name = "subway_stations_nearby_studios")
public class SubwayStationNearbyStudio extends CreatedDateEntity {

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
}
