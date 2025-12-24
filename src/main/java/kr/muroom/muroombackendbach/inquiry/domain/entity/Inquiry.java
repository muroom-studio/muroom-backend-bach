package kr.muroom.muroombackendbach.inquiry.domain.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import kr.muroom.muroombackendbach.common.domain.SoftDeletableEntity;
import kr.muroom.muroombackendbach.common.util.tsid.Tsid;
import kr.muroom.muroombackendbach.user.domain.entity.Musician;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@SQLRestriction("deleted_at IS NULL")
@Table(name = "inquiries")
public class Inquiry extends SoftDeletableEntity {

  @Id
  @Tsid
  @Column(name = "inquiry_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "musician_id", nullable = false)
  private Musician musician;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "inquiry_category_id", nullable = false)
  private InquiryCategory category;

  @Column(nullable = false, length = 200)
  private String title;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String content;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private InquiryStatus status;

  @Column(columnDefinition = "TIMESTAMPTZ")
  private OffsetDateTime deletedAt;

  @OneToOne(mappedBy = "inquiry", fetch = FetchType.LAZY)
  private InquiryReply inquiryReply;

  @OneToMany(mappedBy = "inquiry", cascade = CascadeType.ALL, orphanRemoval = true, fetch =
      FetchType.LAZY)
  private List<InquiryImage> images = new ArrayList<>();
}
