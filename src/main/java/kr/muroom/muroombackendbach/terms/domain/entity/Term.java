package kr.muroom.muroombackendbach.terms.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import kr.muroom.muroombackendbach.common.domain.AuditableEntity;
import kr.muroom.muroombackendbach.common.domain.CreatedDateEntity;
import kr.muroom.muroombackendbach.common.util.tsid.Tsid;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "terms")
@SQLRestriction("is_active = true")
@SQLDelete(sql = "UPDATE terms SET is_active = false WHERE term_id = ?")
public class Term extends AuditableEntity {

  @Id
  @Tsid
  @Column(name = "term_id")
  private Long id;

  @Column(length = 50)
  @Enumerated(EnumType.STRING)
  private TermsType code;

  @Column(length = 50)
  @Enumerated(EnumType.STRING)
  private TargetRole targetRole;

  @Column(length = 50)
  private String version;

  @Column(nullable = false)
  private boolean isMandatory;

  @Builder.Default
  @Column(nullable = false)
  private boolean isActive = true;

  private OffsetDateTime effectiveAt;

  public void updateTerm(TermsType code, TargetRole targetRole, OffsetDateTime effectiveAt) {
    this.code = code;
    this.targetRole = targetRole;
    this.effectiveAt = effectiveAt;
  }
}
