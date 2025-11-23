package kr.muroom.muroombackendbach.common.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.Getter;
import org.hibernate.annotations.SQLRestriction;

/**
 * 소프트 삭제 기능을 제공하는 추상 엔티티 클래스입니다.
 *
 * <p>이 클래스는 AuditableEntity 클래스를 상속하며, deletedAt 필드를 통해 엔티티가 삭제되었는지 여부를 관리합니다.
 * deletedAt 필드가 null이 아니면 해당 엔티티는 삭제된 것으로 간주됩니다.
 */
@Getter
@MappedSuperclass
@SQLRestriction("deleted_at IS NULL")
public abstract class SoftDeletableEntity extends AuditableEntity {

  @Column
  private LocalDateTime deletedAt;
}
