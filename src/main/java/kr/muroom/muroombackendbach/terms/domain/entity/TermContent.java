package kr.muroom.muroombackendbach.terms.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "term_contents")
@EntityListeners(AuditingEntityListener.class)
public class TermContent {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "term_content_id_seq_generator")
    @SequenceGenerator(name = "term_content_id_seq_generator", sequenceName = "term_content_id_seq",allocationSize = 1)
    @Column(name = "term_content_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "term_id")
    private Term term;

    @Column(columnDefinition = "TEXT")
    private String content;
}
