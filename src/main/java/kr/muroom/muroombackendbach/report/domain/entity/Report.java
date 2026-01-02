package kr.muroom.muroombackendbach.report.domain.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import kr.muroom.muroombackendbach.common.domain.SoftDeletableEntity;
import kr.muroom.muroombackendbach.common.util.tsid.Tsid;
import kr.muroom.muroombackendbach.report.domain.enums.ReportDomainType;
import kr.muroom.muroombackendbach.report.domain.enums.ReportStatus;
import kr.muroom.muroombackendbach.musician.domain.entity.Musician;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "reports")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(
    sql = """
        UPDATE reports
        SET deleted_at = CURRENT_TIMESTAMP
        WHERE report_id = ?
        """
)
public class Report extends SoftDeletableEntity {

  @Id
  @Tsid
  @Column(name = "report_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "reporter_id", nullable = false)
  private Musician reporter;

  @Enumerated(EnumType.STRING)
  @Column(name = "target_type", length = 30, nullable = false)
  private ReportDomainType targetType;

  @Column(name = "target_id", nullable = false)
  private Long targetId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "report_reason_id", nullable = false)
  private ReportReason reportReason;

  @Column(length = 1000)
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(length = 30, nullable = false)
  private ReportStatus status;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "snapshot", nullable = false, columnDefinition = "jsonb")
  private JsonNode snapshot;

  private OffsetDateTime deletedAt;

  public void changeReportReason(ReportReason reportReason) {
    this.reportReason = reportReason;
  }

  public void changeDescription(String description) {
    this.description = description;
  }

  public void changeStatus(ReportStatus status) {
    this.status = status;
  }
}
