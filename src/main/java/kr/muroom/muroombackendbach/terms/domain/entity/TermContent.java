package kr.muroom.muroombackendbach.terms.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "term_contents")
public class TermContent {
    @Id
    private Long termId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "term_id")
    private Terms terms;

    @Column(columnDefinition = "TEXT")
    private String content;
}
