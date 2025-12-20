package kr.muroom.muroombackendbach.faq.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "faq_categories")
public class FaqCategory {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "faq_category_id_seq_gen")
  @SequenceGenerator(
      name = "faq_category_id_seq_gen",
      sequenceName = "faq_category_id_seq",
      allocationSize = 1
  )
  @Column(name = "faq_category_id")
  private Long id;

  @Column(name = "code", nullable = false, length = 50)
  private String code;

  @Column(name = "name", nullable = false, length = 50)
  private String name;

  @Column(name = "is_active", nullable = false)
  private Boolean isActive;
}