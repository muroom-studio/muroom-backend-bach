package kr.muroom.muroombackendbach.user.domain.entity;

import jakarta.persistence.*;
import kr.muroom.muroombackendbach.common.domain.AuditableEntity;
import lombok.*;
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
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "social_account_id_seq_generator")
    @SequenceGenerator(name = "social_account_id_seq_generator", sequenceName = "social_account_id_seq", allocationSize = 1)
    @Column(name = "social_account_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "musician_id")
    private Musician musician;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OAuthProvider provider;

    @Column(name = "provider_user_id", nullable = false, length = 255)
    private String providerUserId;
}
