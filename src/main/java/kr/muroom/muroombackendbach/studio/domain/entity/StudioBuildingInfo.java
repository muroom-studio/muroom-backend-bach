package kr.muroom.muroombackendbach.studio.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import kr.muroom.muroombackendbach.studio.domain.enums.FloorType;
import kr.muroom.muroombackendbach.studio.domain.enums.ParkingFeeType;
import kr.muroom.muroombackendbach.studio.domain.enums.RestroomType;
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

  @Enumerated(EnumType.STRING)
  @Column(length = 50)
  private FloorType floorType;

  @Column
  private Integer floorNumber;

  @Enumerated(EnumType.STRING)
  @Column(length = 50)
  private RestroomType restroomType;

  @Column
  private Boolean isParkingAvailable;

  @Column(length = 50)
  private ParkingFeeType parkingFeeType;

  @Column(length = 50)
  private String parkingFeeInfo; // 매월 3만원

  @Column
  private Integer parkingSpots;

  @Column(length = 50)
  private String parkingLocationName;

  @Column
  private String parkingLocationAddress;

  @Column
  private Boolean isLodgingAvailable;

  @Column
  private Boolean hasFireInsurance;

  public void assignStudio(Studio studio) {
    this.studio = studio;
  }
}