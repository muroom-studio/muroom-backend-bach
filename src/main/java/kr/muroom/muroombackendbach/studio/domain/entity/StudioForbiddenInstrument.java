package kr.muroom.muroombackendbach.studio.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import kr.muroom.muroombackendbach.common.util.tsid.Tsid;
import kr.muroom.muroombackendbach.instrument.domain.entity.Instrument;
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
@Table(name = "studio_forbidden_instruments")
public class StudioForbiddenInstrument {

  @Id
  @Tsid
  @Column(name = "studio_forbidden_instrument_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "studio_id", nullable = false)
  private Studio studio;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "instrument_id", nullable = false)
  private Instrument instrument;

  public void assignStudio(Studio studio) {
    this.studio = studio;
  }
}
