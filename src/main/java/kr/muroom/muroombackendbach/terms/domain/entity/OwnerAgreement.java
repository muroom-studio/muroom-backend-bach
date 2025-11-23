package kr.muroom.muroombackendbach.terms.domain.entity;

import jakarta.persistence.*;
import kr.muroom.muroombackendbach.user.domain.entity.Owner;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "owner_agreements")
public class OwnerAgreement {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "owner_agreement_id_seq_generator")
    @SequenceGenerator(name = "owner_agreement_id_seq_generator", sequenceName = "owner_agreement_id_seq",allocationSize = 1)
    @Column(name = "owner_agreement_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terms_id")
    private Term term;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owners_id")
    private Owner owner;

    @CreatedDate
    private LocalDateTime agreedAt;

    public static OwnerAgreement of(Owner owner, Term term) {
        return OwnerAgreement.builder()
                .owner(owner)
                .term(term)
                .build();
    }
}
