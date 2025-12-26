package kr.muroom.muroombackendbach.studioboasting.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "studio_boast_comments")
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE studio_boast_comments SET deleted_at = NOW() WHERE studio_boast_comment_id = ?")
public class StudioBoastComment extends SoftDeletableEntity {

  @Id
  @Tsid
  @Column(name = "studio_boast_comment_id")
  private Long id;

  @Column(nullable = false)
  @Builder.Default
  private Boolean isSecret = false;

  @Column(columnDefinition = "TEXT", nullable = false)
  private String content;

  @Column(nullable = false)
  private Long creatorUserId;

  @Column
  private Long taggedUserId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "studio_boast_id", nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
  private StudioBoast studioBoast;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
  private StudioBoastComment parent;

  public void updateContent(String content) {
    this.content = content;
  }
}
