package kr.muroom.muroombackendbach.withdrawal.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import kr.muroom.muroombackendbach.user.domain.entity.Musician;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "musician_withdrawals")
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class MusicianWithdrawal {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "musician_withdrawals_id_seq_gen")
  @SequenceGenerator(
      name = "musician_withdrawals_id_seq_gen",
      sequenceName = "musician_withdrawals_id_seq",
      allocationSize = 1
  )
  @Column(name = "musician_withdrawals_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "withdrawal_reason_id", nullable = false)
  private WithdrawalReason withdrawalReason;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "musician_id", nullable = false)
  private Musician musician;

  @Column(columnDefinition = "TEXT")
  private String opinion;

  @Column(nullable = false)
  private OffsetDateTime createdAt;
}