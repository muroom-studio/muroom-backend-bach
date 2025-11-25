package kr.muroom.muroombackendbach.common.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.time.OffsetDateTime;
import lombok.Getter;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * 수정 일시를 자동으로 관리하는 추상 엔티티 클래스입니다.
 *
 * <p>이 클래스는 생성 일시를 관리하는 CreatedDateEntity 클래스를 상속하며, 엔티티가 수정될 때마다 updatedAt 필드를 자동으로 업데이트합니다.
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableEntity extends CreatedDateEntity {

  @LastModifiedDate
  @Column(nullable = false)
  private OffsetDateTime updatedAt;
}
