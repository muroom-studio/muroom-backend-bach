package kr.muroom.muroombackendbach.withdrawal.domain.entity;

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
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "withdrawal_reasons")
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class WithdrawalReason {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "withdrawal_reason_id_seq_gen")
  @SequenceGenerator(
      name = "withdrawal_reason_id_seq_gen",
      sequenceName = "withdrawal_reason_id_seq",
      allocationSize = 1
  )
  @Column(name = "withdrawal_reason_id")
  private Long id;

  @Column(length = 50, nullable = false, unique = true)
  private String code;

  @Column(nullable = false)
  private String description;

  @Column(nullable = false)
  private Boolean isActive;

  @Column(nullable = false)
  private OffsetDateTime createdAt;

  @Column(nullable = false)
  private OffsetDateTime updatedAt;
}