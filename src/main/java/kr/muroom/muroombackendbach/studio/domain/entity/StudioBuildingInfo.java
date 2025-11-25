package kr.muroom.muroombackendbach.studio.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import kr.muroom.muroombackendbach.studio.domain.enums.ParkingFeeType;
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
@Table(name = "studio_building_info")
public class StudioBuildingInfo {

  @Id
  @Column(name = "studio_id")
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @MapsId
  @JoinColumn(name = "studio_id")
  private Studio studio;

  @Column(length = 100)
  private String floor;

  @Column
  private Boolean isParkingAvailable;

  @Column(length = 50)
  private ParkingFeeType parkingFeeType;

  @Column
  private Integer parkingSpots;

  @Column
  private String parkingLocationAddress;

  @Column(columnDefinition = "TEXT")
  private String parkingLocationInfo;

  @Column
  private Boolean isLodgingAvailable;

  @Column
  private Boolean hasFireInsurance;
}