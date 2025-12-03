package kr.muroom.muroombackendbach.studio.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import kr.muroom.muroombackendbach.studio.domain.enums.StudioImageCategory;
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
@Table(name = "studio_images")
public class StudioImage {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "studio_image_id_seq_gen")
  @SequenceGenerator(name = "studio_image_id_seq_gen", sequenceName = "studio_image_id_seq",
      allocationSize = 1)
  @Column(name = "studio_image_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "studio_id", nullable = false)
  private Studio studio;

  @Enumerated(EnumType.STRING)
  @Column(length = 50)
  private StudioImageCategory category;

  @Column(length = 1024)
  private String imageKey;

  @Column
  private Integer sequence;

  public void assignStudio(Studio studio) {
    this.studio = studio;
  }
}