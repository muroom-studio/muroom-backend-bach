package kr.muroom.muroombackendbach.room.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
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
@Table(name = "rooms")
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE rooms SET deleted_at = NOW() WHERE room_id = ?")
public class Room extends SoftDeletableEntity {

  @Id
  @Tsid
  @Column(name = "room_id")
  private Long id;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(nullable = false)
  private Integer sequence;

  @Column(name = "width_mm", nullable = false)
  private Integer width;

  @Column(name = "height_mm", nullable = false)
  private Integer height;

  @Column(name = "is_available")
  private Boolean isAvailable;

  @Column(name = "available_at")
  private LocalDate availableAt;

  @Column
  private Integer basePrice;

  @Column(name = "studio_id", nullable = false)
  private Long studioId;

  public void update(Integer sequence, Integer width, Integer height, Boolean isAvailable,
      LocalDate availableAt, Integer basePrice) {
    this.sequence = sequence;
    this.width = width;
    this.height = height;
    this.isAvailable = isAvailable;
    this.availableAt = availableAt;
    this.basePrice = basePrice;
  }
}
