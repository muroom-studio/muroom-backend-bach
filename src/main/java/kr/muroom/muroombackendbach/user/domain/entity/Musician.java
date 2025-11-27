package kr.muroom.muroombackendbach.user.domain.entity;

import jakarta.persistence.*;
import kr.muroom.muroombackendbach.common.domain.AuditableEntity;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "musicians")
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Musician extends AuditableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "musician_id_seq_generator")
  @SequenceGenerator(name = "musician_id_seq_generator", sequenceName = "musician_id_seq",
      allocationSize = 1)
  @Column(name = "musician_id")
  private Long id;

  @Column(length = 50)
  private String name;

  private LocalDate birthdate;

  @Column(length = 16)
  private String phoneNumber;

  @Column(length = 10, unique = true)
  private String nickname;

  @Enumerated(EnumType.STRING)
  @Column(length = 50)
  private UserStatus status;

  @OneToOne(fetch = FetchType.LAZY)
  private Instrument instrument;

  private OffsetDateTime deletedAt;

  public static Musician of(
      String name,
      LocalDate birthdate,
      String phoneNumber,
      String nickname,
      Instrument instrument
  ) {
    return Musician.builder()
        .name(name)
        .birthdate(birthdate)
        .phoneNumber(phoneNumber)
        .nickname(nickname)
        .instrument(instrument)
        .status(UserStatus.ACTIVE)
        .build();
  }

  public void softDelete() {
    this.deletedAt = OffsetDateTime.now();
    this.status = UserStatus.INACTIVE; // enum 값에 맞게 수정
  }
}
