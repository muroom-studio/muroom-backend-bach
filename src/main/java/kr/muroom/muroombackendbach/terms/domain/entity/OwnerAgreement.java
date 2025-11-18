package kr.muroom.muroombackendbach.terms.domain.entity;

import jakarta.persistence.*;
import kr.muroom.muroombackendbach.user.domain.entity.Owner;
import org.springframework.data.annotation.CreatedDate;

import java.time.OffsetDateTime;

@Entity
@Table(name = "owner_agreements")
public class OwnerAgreement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ownerAgreementId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terms_id")
    private Terms terms;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "onwers_id")
    private Owner owner;

    @CreatedDate
    private OffsetDateTime agreedAt;
}
