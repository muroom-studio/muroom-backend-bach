package kr.muroom.muroombackendbach.room.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.LocalDate;
import kr.muroom.muroombackendbach.common.domain.SoftDeletableEntity;
import kr.muroom.muroombackendbach.studio.domain.entity.Studio;
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
@Table(name = "rooms")
public class Room extends SoftDeletableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "room_id_seq_gen")
  @SequenceGenerator(name = "room_id_seq_gen", sequenceName = "room_id_seq", allocationSize = 1)
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

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "studio_id", nullable = false)
  private Studio studio;

  public void assignStudio(Studio studio) {
    this.studio = studio;
  }
}
