package kr.muroom.muroombackendbach.inquiry.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
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
@Table(name = "inquiry_categories")
@SQLDelete(sql = "UPDATE inquiry_categories SET is_active = false WHERE inquiry_category_id = ?")
@SQLRestriction("is_active = true")
public class InquiryCategory {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inquiry_category_id_seq_gen")
  @SequenceGenerator(name = "inquiry_category_id_seq_gen", sequenceName =
      "inquiry_category_id_seq", allocationSize = 1)
  @Column(name = "inquiry_category_id")
  private Long id;

  @Column(nullable = false, length = 50)
  private String code;

  @Column(nullable = false, length = 50)
  private String name;

  @Column(nullable = false)
  private Boolean isActive;

  @Column(nullable = false, columnDefinition = "TIMESTAMPTZ")
  private OffsetDateTime createdAt;

  @Column(nullable = false, columnDefinition = "TIMESTAMPTZ")
  private OffsetDateTime updatedAt;
}
