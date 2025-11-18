package kr.muroom.muroombackendbach.terms.domain.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;

@Entity
@Table(name = "terms")
@EntityListeners(AuditingEntityListener.class)
public class Terms {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long termId;

    @Column(length = 50)
    private String type;

    @Column(length = 50)
    private String targetRole;

    @Column(length = 50)
    private String version;

    @Column(nullable = false)
    private boolean isMandatory;

    private OffsetDateTime effectiveAt;

    @CreatedDate
    private  OffsetDateTime createdAt;
}
