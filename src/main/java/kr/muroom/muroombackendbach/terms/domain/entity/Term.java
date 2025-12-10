package kr.muroom.muroombackendbach.terms.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import kr.muroom.muroombackendbach.common.domain.CreatedDateEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "terms")
@EntityListeners(AuditingEntityListener.class)
public class Term extends CreatedDateEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "term_id_seq_gen")
  @SequenceGenerator(name = "term_id_seq_gen", sequenceName = "term_id_seq",
      allocationSize = 1)
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

  @Column
  private boolean isActive;

  private OffsetDateTime effectiveAt;

  public void updateTerm(TermsType code, TargetRole targetRole, OffsetDateTime effectiveAt) {
    this.code = code;
    this.targetRole = targetRole;
    this.effectiveAt = effectiveAt;
  }
}
