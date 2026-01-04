package kr.muroom.muroombackendbach.owner.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import kr.muroom.muroombackendbach.common.domain.AuditableEntity;
import kr.muroom.muroombackendbach.common.util.tsid.Tsid;
import kr.muroom.muroombackendbach.musician.domain.entity.UserStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@Table(name = "owners")
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class Owner extends AuditableEntity {

  @Id
  @Tsid
  @Column(name = "owner_id")
  private Long id;

  @Column(length = 255, unique = true)
  private String email;

  @Column(length = 255)
  private String password;

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

  @Column
  private Integer experienceYears;

  private OffsetDateTime deletedAt;

  public boolean isActive() {
    return this.status == UserStatus.ACTIVE && this.deletedAt == null;
  }

  public void changeNickname(String newNickname) {
    this.nickname = newNickname;
  }
}
