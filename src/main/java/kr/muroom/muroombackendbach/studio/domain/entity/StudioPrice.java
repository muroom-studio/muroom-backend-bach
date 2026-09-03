package kr.muroom.muroombackendbach.studio.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import kr.muroom.muroombackendbach.common.domain.SoftDeletableEntity;
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
@Table(name = "studio_prices")
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE studio_prices SET deleted_at = NOW() WHERE studio_id = ?")
public class StudioPrice extends SoftDeletableEntity {

  @Id
  @Column(name = "studio_id")
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @MapsId
  @JoinColumn(name = "studio_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
  private Studio studio;

  @Column
  private Integer minPrice;

  @Column
  private Integer maxPrice;

  public void update(Integer minPrice, Integer maxPrice) {
    this.minPrice = minPrice;
    this.maxPrice = maxPrice;
  }
}
