package kr.muroom.muroombackendbach.subway.domain.entity;

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
@Table(name = "subway_lines")
public class SubwayLine extends AuditableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "subway_line_id_seq_gen")
  @SequenceGenerator(name = "subway_line_id_seq_gen", sequenceName = "subway_line_id_seq",
      allocationSize = 1)
  @Column(name = "subway_line_id")
  private Long id;

  @Column(nullable = false, unique = true)
  private String name;

  @Column(nullable = false)
  private String color;

  @Column(nullable = false)
  private String description;
}
