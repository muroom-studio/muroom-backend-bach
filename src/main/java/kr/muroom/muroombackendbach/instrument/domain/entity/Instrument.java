package kr.muroom.muroombackendbach.instrument.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kr.muroom.muroombackendbach.common.domain.AuditableEntity;
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
@Table(name = "instruments")
public class Instrument extends AuditableEntity {

  @Id
  @Tsid
  @Column(name = "instrument_id")
  private Long id;

  @Column(nullable = false, length = 50, unique = true)
  private String code;

  @Column(nullable = false, length = 50)
  private String description;
}