package kr.muroom.muroombackendbach.terms.domain.entity;

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
import java.time.LocalDateTime;
import kr.muroom.muroombackendbach.user.domain.entity.Musician;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "musician_agreements")
public class MusicianAgreement {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "musician_agreement_id_seq_gen")
  @SequenceGenerator(name = "musician_agreement_id_seq_gen", sequenceName = "musician_agreement_id_seq", allocationSize = 1)
  @Column(name = "musician_agreement_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "term_id")
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
