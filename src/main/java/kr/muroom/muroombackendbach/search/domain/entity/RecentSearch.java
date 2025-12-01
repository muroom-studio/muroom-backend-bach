package kr.muroom.muroombackendbach.search.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import kr.muroom.muroombackendbach.user.domain.entity.Musician;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "recent_searches")
public class RecentSearch {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "recent_search_id_seq_gen")
  @SequenceGenerator(name = "recent_search_id_seq_gen", sequenceName = "recent_search_id_seq",
      allocationSize = 1)
  @Column(name = "recent_search_id")
  private Long id;

  @Column(name = "keyword", nullable = false)
  private String keyword;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "recently_searched_at", nullable = false)
  private OffsetDateTime recentlySearchedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "musician_id", nullable = false)
  private Musician musician;

  public void updateRecentlySearchedAt() {
    this.recentlySearchedAt = OffsetDateTime.now();
  }
}
