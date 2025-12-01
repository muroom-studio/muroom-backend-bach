package kr.muroom.muroombackendbach.terms.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "terms")
@EntityListeners(AuditingEntityListener.class)
public class Term {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "term_id_seq_generator")
  @SequenceGenerator(name = "term_id_seq_generator", sequenceName = "term_id_seq",
      allocationSize = 1)
  @Column(name = "term_id")
  private Long id;

  @Column(length = 50)
  @Enumerated(EnumType.STRING)
  private TermsType code;

  @Column(length = 50)
  private String targetRole;

  @Column(length = 50)
  private String version;

  @Column(nullable = false)
  private boolean isMandatory;

  private OffsetDateTime effectiveAt;

  @CreatedDate
  private OffsetDateTime createdAt;
}
