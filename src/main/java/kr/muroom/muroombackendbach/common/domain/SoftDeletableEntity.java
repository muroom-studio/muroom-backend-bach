package kr.muroom.muroombackendbach.common.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.OffsetDateTime;
import lombok.Getter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * 소프트 삭제 기능을 제공하는 추상 엔티티 클래스입니다.
 *
 * <p>이 클래스는 삭제 일시를 나타내는 deletedAt 필드를 포함하며, 엔티티가 삭제될 때 실제로 데이터베이스에서 삭제되지 않고 deletedAt 필드가 현재 시간으로
 * 설정됩니다.
 *
 * <p>또한, 삭제된 엔티티는 기본 쿼리에서 제외됩니다.
 */
@Getter
@MappedSuperclass
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE {h-schema}{h-table} SET deleted_at = CURRENT_TIMESTAMP WHERE {h-pk} = ?")
public abstract class SoftDeletableEntity extends AuditableEntity {

  @Column
  private OffsetDateTime deletedAt;
}
