package kr.muroom.muroombackendbach.studio.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import kr.muroom.muroombackendbach.common.domain.SoftDeletableEntity;
import kr.muroom.muroombackendbach.studio.domain.enums.FloorType;
import kr.muroom.muroombackendbach.studio.domain.enums.ParkingFeeType;
import kr.muroom.muroombackendbach.studio.domain.enums.RestroomGender;
import kr.muroom.muroombackendbach.studio.domain.enums.RestroomLocation;
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
@Table(name = "studio_building_info")
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE studio_building_info SET deleted_at = NOW() WHERE studio_id = ?")
public class StudioBuildingInfo extends SoftDeletableEntity {

  @Id
  @Column(name = "studio_id")
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @MapsId
  @JoinColumn(name = "studio_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
  private Studio studio;

  @Enumerated(EnumType.STRING)
  @Column(length = 50)
  private FloorType floorType;

  @Column
  private Integer floorNumber;

  @Column
  private Boolean hasRestroom;

  @Enumerated(EnumType.STRING)
  @Column(length = 50)
  private RestroomLocation restroomLocation;

  @Enumerated(EnumType.STRING)
  @Column(length = 50)
  private RestroomGender restroomGender;

  @Enumerated(EnumType.STRING)
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

  public void update(
      FloorType floorType, Integer floorNumber, Boolean hasRestroom,
      RestroomLocation restroomLocation, RestroomGender restroomGender,
      ParkingFeeType parkingFeeType, String parkingFeeInfo, Integer parkingSpots,
      String parkingLocationName, String parkingLocationAddress,
      Boolean isLodgingAvailable, Boolean hasFireInsurance
  ) {
    this.floorType = floorType;
    this.floorNumber = floorNumber;
    this.hasRestroom = hasRestroom;
    this.restroomLocation = restroomLocation;
    this.restroomGender = restroomGender;
    this.parkingFeeType = parkingFeeType;
    this.parkingFeeInfo = parkingFeeInfo;
    this.parkingSpots = parkingSpots;
    this.parkingLocationName = parkingLocationName;
    this.parkingLocationAddress = parkingLocationAddress;
    this.isLodgingAvailable = isLodgingAvailable;
    this.hasFireInsurance = hasFireInsurance;
  }
}