package kr.muroom.muroombackendbach.user.domain.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "owners")
@EntityListeners(AuditingEntityListener.class)
public class Owner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ownerId;

    @Column(length = 50)
    private String name;

    private LocalDate birthdate;

    @Column(length = 16)
    private String phoneNumber;

    @Column(length = 10)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private UserStatus status;

    @CreatedDate
    private OffsetDateTime createdAt;

    @LastModifiedDate
    private OffsetDateTime updatedAt;

    private OffsetDateTime deletedAt;
}
