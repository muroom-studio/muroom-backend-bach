package kr.muroom.muroombackendbach.terms.domain.entity;

import jakarta.persistence.*;
import kr.muroom.muroombackendbach.user.domain.entity.Owner;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "owner_agreements")
public class OwnerAgreement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ownerAgreementId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terms_id")
    private Terms terms;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owners_id")
    private Owner owner;

    @CreatedDate
    private OffsetDateTime agreedAt;
}
