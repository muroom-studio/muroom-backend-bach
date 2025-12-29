package kr.muroom.muroombackendbach.report.domain.entity;

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

@Entity
@Table(name = "report_reasons")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ReportReason extends AuditableEntity {

  @Id
  @Tsid
  @Column(name = "report_reason_id")
  private Long id;

  @Column(length = 50, nullable = false, unique = true)
  private String code;

  @Column(nullable = false)
  private boolean isActive;

  @Column(nullable = false)
  private String description;

  @Column(name = "display_order", nullable = false)
  private Integer displayOrder;
}
