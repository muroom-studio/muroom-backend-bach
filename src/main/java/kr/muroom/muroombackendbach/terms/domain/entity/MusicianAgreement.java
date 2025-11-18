package kr.muroom.muroombackendbach.terms.domain.entity;

import jakarta.persistence.*;
import kr.muroom.muroombackendbach.user.domain.entity.Musician;
import org.springframework.data.annotation.CreatedDate;

import java.time.OffsetDateTime;

@Entity
@Table(name = "musician_agreements")
public class MusicianAgreement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long musicianAgreementId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terms_id")
    private Terms terms;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "musician_id")
    private Musician musician;

    @CreatedDate
    private OffsetDateTime agreedAt;
}
