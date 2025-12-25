package kr.muroom.muroombackendbach.studioboasting.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import kr.muroom.muroombackendbach.common.domain.SoftDeletableEntity;
import kr.muroom.muroombackendbach.common.util.tsid.Tsid;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "studio_boasts")
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE studio_boasts SET deleted_at = CURRENT_TIMESTAMP WHERE studio_boast_id = ?")
public class StudioBoast extends SoftDeletableEntity {

  @Id
  @Tsid
  @Column(name = "studio_boast_id")
  private Long id;

  @Column(length = 1024, nullable = false)
  private String thumbnailImageFileKey;

  @Column(length = 1024, nullable = false)
  private String content;

  @Column(nullable = false)
  private String studioName;

  @Column(nullable = false)
  private String roadNameAddress;

  @Column(nullable = false)
  private String lotNumberAddress;

  @Column(nullable = false)
  private String detailedAddress;

  @Column(nullable = false)
  private boolean agreedToEventTerms;

  @Column(nullable = false)
  private String instagramAccount;

  @Column(nullable = false)
  private Long creatorUserId;

  @Column(nullable = false)
  @Builder.Default
  private Long likeCount = 0L;

  @Column
  private Long studioId;

  public void adjustLikeCount(long delta) {
    this.likeCount += delta;
    if (this.likeCount < 0) {
      this.likeCount = 0L;
    }
  }

  public void update(
      String content,
      String studioName,
      String roadNameAddress,
      String lotNumberAddress,
      String detailedAddress,
      boolean agreedToEventTerms,
      String instagramAccount,
      Long studioId,
      String thumbnailImageFileKey
  ) {
    this.content = content;
    this.studioName = studioName;
    this.roadNameAddress = roadNameAddress;
    this.lotNumberAddress = lotNumberAddress;
    this.detailedAddress = detailedAddress;
    this.agreedToEventTerms = agreedToEventTerms;
    this.instagramAccount = instagramAccount;
    this.studioId = studioId;
    this.thumbnailImageFileKey = thumbnailImageFileKey;
  }

//  public boolean getAgreedToEventTerms() {
//    return this.agreedToEventTerms;
//  }
}
