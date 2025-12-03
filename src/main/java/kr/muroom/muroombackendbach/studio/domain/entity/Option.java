package kr.muroom.muroombackendbach.studio.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import kr.muroom.muroombackendbach.common.domain.AuditableEntity;
import kr.muroom.muroombackendbach.studio.domain.enums.OptionCategory;
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
@Table(name = "options")
public class Option extends AuditableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "option_id_seq_gen")
  @SequenceGenerator(name = "option_id_seq_gen", sequenceName = "option_id_seq", allocationSize = 1)
  @Column(name = "option_id")
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(name = "category", nullable = false)
  OptionCategory category;

  @Column(nullable = false, length = 50, unique = true)
  private String code;

  @Column(nullable = false, length = 50)
  private String description;

  @Column(name = "icon_image_key", nullable = false)
  private String iconImageKey;
}