package kr.muroom.muroombackendbach.terms.domain.entity;

import jakarta.persistence.*;
import kr.muroom.muroombackendbach.user.domain.entity.Musician;
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
@Table(name = "musician_agreements")
public class MusicianAgreement {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "musician_agreement_id_seq_generator")
    @SequenceGenerator(name = "musician_agreement_id_seq_generator", sequenceName = "musician_agreement_id_seq",allocationSize = 1)
    @Column(name = "musician_agreement_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terms_id")
    private Term term;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "musician_id")
    private Musician musician;

    @CreatedDate
    private LocalDateTime agreedAt;

    public static MusicianAgreement of(Musician musician, Term term) {
        return MusicianAgreement.builder()
                .musician(musician)
                .term(term)
                .build();
    }
}
