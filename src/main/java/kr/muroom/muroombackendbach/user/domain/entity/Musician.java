package kr.muroom.muroombackendbach.user.domain.entity;

import jakarta.persistence.*;
import kr.muroom.muroombackendbach.common.domain.CreatedDateEntity;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Entity
@Table(name = "musicians")
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class Musician extends CreatedDateEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long musicianId;

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

    private LocalDateTime deletedAt;

    public static Musician of(
            String name,
            LocalDate birthdate,
            String phoneNumber,
            String nickname
    ) {
        return Musician.builder()
                .name(name)
                .birthdate(birthdate)
                .phoneNumber(phoneNumber)
                .nickname(nickname)
                .status(UserStatus.ACTIVE)
                .build();
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
        this.status = UserStatus.INACTIVE; // enum 값에 맞게 수정
    }
}
