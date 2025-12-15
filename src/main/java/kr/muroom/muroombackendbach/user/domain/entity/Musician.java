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
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import kr.muroom.muroombackendbach.common.domain.AuditableEntity;
import kr.muroom.muroombackendbach.instrument.domain.entity.Instrument;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "musicians")
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Musician extends AuditableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "musician_id_seq_gen")
  @SequenceGenerator(name = "musician_id_seq_gen", sequenceName = "musician_id_seq",
      allocationSize = 1)
  @Column(name = "musician_id")
  private Long id;

  @Column(length = 50)
  private String name;

  @Column(length = 16)
  private String phoneNumber;

  @Column(length = 10, unique = true)
  private String nickname;

  @Enumerated(EnumType.STRING)
  @Column(length = 50)
  private UserStatus status;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "instrument_id", nullable = false)
  private Instrument instrument;

  private OffsetDateTime deletedAt;

  public void changeNickname(String nickname) {
    this.nickname = nickname;
  }

  public void changeInstrument(Instrument instrument) {
    this.instrument = instrument;
  }

  public void changePhone(String phone) {
    this.phoneNumber = phone;
  }
}
