package kr.muroom.muroombackendbach.user.domain.entity;

import jakarta.persistence.*;
import kr.muroom.muroombackendbach.common.domain.AuditableEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "owners")
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class Owner extends AuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "owner_id_seq_generator")
    @SequenceGenerator(name = "owner_id_seq_generator", sequenceName = "owner_id_seq",allocationSize = 1)
    @Column(name = "owner_id")
    private Long id;

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

    public static Owner of(
            String name,
            LocalDate birthdate,
            String phoneNumber,
            String nickname
    ) {
        return Owner.builder()
                .name(name)
                .birthdate(birthdate)
                .phoneNumber(phoneNumber)
                .nickname(nickname)
                .status(UserStatus.ACTIVE)
                .build();
    }

    public void softDelete() {
        this.deletedAt = OffsetDateTime.now();
        this.status = UserStatus.INACTIVE;
    }
}
