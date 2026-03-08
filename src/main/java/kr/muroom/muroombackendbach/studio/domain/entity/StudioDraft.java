package kr.muroom.muroombackendbach.studio.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import kr.muroom.muroombackendbach.common.domain.AuditableEntity;
import kr.muroom.muroombackendbach.common.util.tsid.Tsid;
import kr.muroom.muroombackendbach.studio.domain.valueobject.StudioDraftData;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "studio_drafts")
public class StudioDraft extends AuditableEntity {

  @Id
  @Tsid
  @Column(name = "studio_draft_id")
  private Long id;

  @Column(name = "owner_id", nullable = false)
  private Long ownerId;

  @Column(nullable = false)
  private Integer step; // 현재 진행 중인 단계 (1~8)

  @Column(name = "studio_name")
  private String studioName; // 목록에서 식별하기 위한 이름

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "jsonb", nullable = false)
  private StudioDraftData studioDraftData; // 각 단계에서 입력된 데이터를 JSON 형태로 저장

  @Column(name = "expires_at", nullable = false)
  private OffsetDateTime expiresAt; // 임시 저장된 데이터의 유효 기간: 3일

  public void update(Integer step, String studioName, StudioDraftData studioDraftData, OffsetDateTime expiresAt) {
    this.step = step;
    this.studioName = studioName;
    this.studioDraftData = studioDraftData;
    this.expiresAt = expiresAt;
  }

}
