package kr.muroom.muroombackendbach.withdrawal.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kr.muroom.muroombackendbach.common.domain.AuditableEntity;
import kr.muroom.muroombackendbach.common.util.tsid.Tsid;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "withdrawal_reasons")
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@SQLRestriction("is_active = true")
public class WithdrawalReason extends AuditableEntity {

  @Id
  @Tsid
  @Column(name = "withdrawal_reason_id")
  private Long id;

  @Column(length = 50, nullable = false, unique = true)
  private String code;

  @Column(nullable = false)
  private String description;

  @Column(nullable = false)
  private Boolean isActive;

  @Builder.Default
  @Column(name = "sequence", nullable = false)
  private Integer sequence = 0;
}