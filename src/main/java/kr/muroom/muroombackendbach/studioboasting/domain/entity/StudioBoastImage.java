package kr.muroom.muroombackendbach.studioboasting.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
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
@Table(name = "studio_boast_images", indexes = {
    @Index(name = "idx_image_studio_boast_id", columnList = "studio_boast_id")
})
public class StudioBoastImage extends CreatedDateEntity {

  @Id
  @Tsid
  @Column(name = "studio_boast_image_id")
  private Long id;

  @Column(length = 1024, nullable = false)
  private String imageFileKey;

  @Column
  private Integer sequence;

  @Column(nullable = false)
  private Long studioBoastId;
}
