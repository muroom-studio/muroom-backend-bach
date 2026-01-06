package kr.muroom.muroombackendbach.faq.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kr.muroom.muroombackendbach.common.util.tsid.Tsid;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@SQLDelete(sql = "UPDATE faq_categories SET is_active = false WHERE faq_category_id = ?")
@SQLRestriction("is_active = true")
@Entity
@Table(name = "faq_categories")
public class FaqCategory {

  @Id
  @Tsid
  @Column(name = "faq_category_id")
  private Long id;

  @Column(name = "code", nullable = false, length = 50)
  private String code;

  @Column(name = "name", nullable = false, length = 50)
  private String name;

  @Column(name = "is_active", nullable = false)
  private boolean isActive;

  @Builder.Default
  @Column(name = "sequence", nullable = false)
  private Integer sequence = 0;
}