package kr.muroom.muroombackendbach.studio.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import kr.muroom.muroombackendbach.common.domain.SoftDeletableEntity;
import kr.muroom.muroombackendbach.common.util.tsid.Tsid;
import kr.muroom.muroombackendbach.instrument.domain.entity.Instrument;
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
@Table(name = "studio_forbidden_instruments")
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE studio_forbidden_instruments SET deleted_at = NOW() WHERE studio_forbidden_instrument_id = ?")
public class StudioForbiddenInstrument extends SoftDeletableEntity {

  @Id
  @Tsid
  @Column(name = "studio_forbidden_instrument_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "studio_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
  private Studio studio;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "instrument_id", nullable = false)
  private Instrument instrument;
}
