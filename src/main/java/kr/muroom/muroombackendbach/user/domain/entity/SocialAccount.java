package kr.muroom.muroombackendbach.user.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import kr.muroom.muroombackendbach.common.domain.AuditableEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@Table(
    name = "social_accounts",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_provider_provider_user",
            columnNames = {"provider", "provider_user_id"}
        )
    }
)
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class SocialAccount extends AuditableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "social_account_id_seq_gen")
  @SequenceGenerator(name = "social_account_id_seq_gen", sequenceName = "social_account_id_seq",
      allocationSize = 1)
  @Column(name = "social_account_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "musician_id")
  private Musician musician;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private OAuthProvider provider;

  private String accessToken;
  private String refreshToken;

  @Column(name = "provider_user_id", nullable = false)
  private String providerUserId;
}
