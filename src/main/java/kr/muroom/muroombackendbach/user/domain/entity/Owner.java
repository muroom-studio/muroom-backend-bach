package kr.muroom.muroombackendbach.user.domain.entity;

import jakarta.persistence.*;
import kr.muroom.muroombackendbach.common.domain.AuditableEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Getter
@Table(name = "owners")
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class Owner extends AuditableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "owner_id_seq_generator")
  @SequenceGenerator(name = "owner_id_seq_generator", sequenceName = "owner_id_seq",
      allocationSize = 1)
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

  @Column(length = 20, unique = true)
  private String nickname;

  @Enumerated(EnumType.STRING)
  @Column(length = 50)
  private UserStatus status;

  private OffsetDateTime deletedAt;

  public void softDelete() {
    this.deletedAt = OffsetDateTime.now();
    this.status = UserStatus.INACTIVE;
  }

  public boolean isActive() {
    return this.status == UserStatus.ACTIVE && this.deletedAt == null;
  }
}
