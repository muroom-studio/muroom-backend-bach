package kr.muroom.muroombackendbach.studio.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
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
public class Option {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "option_id_seq_gen")
  @SequenceGenerator(name = "option_id_seq_gen", sequenceName = "option_id_seq", allocationSize = 1)
  @Column(name = "option_id")
  private Long id;

  @Column(nullable = false, length = 50)
  private String name;

  @Column(name = "icon_image_url", nullable = false)
  private String iconImageUrl;
}