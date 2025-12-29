package kr.muroom.muroombackendbach.inquiry.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import kr.muroom.muroombackendbach.common.domain.AuditableEntity;
import kr.muroom.muroombackendbach.common.util.tsid.Tsid;
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
public class InquiryCategory extends AuditableEntity {

  @Id
  @Tsid
  @Column(name = "inquiry_category_id")
  private Long id;

  @Column(nullable = false, length = 50)
  private String code;

  @Column(nullable = false, length = 50)
  private String name;

  @Column(nullable = false)
  private Boolean isActive;

  @Builder.Default
  @Column(name = "sequence", nullable = false)
  private Integer sequence = 0;
}
