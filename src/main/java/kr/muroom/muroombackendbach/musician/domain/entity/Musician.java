package kr.muroom.muroombackendbach.musician.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import kr.muroom.muroombackendbach.common.domain.AuditableEntity;
import kr.muroom.muroombackendbach.common.util.tsid.Tsid;
import kr.muroom.muroombackendbach.instrument.domain.entity.Instrument;
import kr.muroom.muroombackendbach.user.domain.entity.UserStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "musicians")
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(
    sql = """
        UPDATE musicians
        SET deleted_at = CURRENT_TIMESTAMP,
            status = 'INACTIVE'
        WHERE musician_id = ?
        """
)
public class Musician extends AuditableEntity {

  @Id
  @Tsid
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
