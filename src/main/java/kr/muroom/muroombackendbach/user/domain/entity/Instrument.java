package kr.muroom.muroombackendbach.user.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import kr.muroom.muroombackendbach.common.domain.AuditableEntity;
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
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "instrument_id_seq_generator")
  @SequenceGenerator(name = "instrument_id_seq_generator", sequenceName = "instrument_id_seq",
      allocationSize = 1)
  @Column(name = "instrument_id")
  private Long id;

  @Column(nullable = false, length = 50, unique = true)
  private String code;

  @Column(nullable = false, length = 50)
  private String description;
}