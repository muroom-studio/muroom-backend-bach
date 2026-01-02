package kr.muroom.muroombackendbach.musician.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "my_studios")
public class MyStudio extends SoftDeletableEntity {

  @Id
  @Tsid
  @Column(name = "my_studio_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "musician_id")
  private Musician musician;

  @Column
  private String name;

  @Column
  private String detailAddress;

  @Column
  private String roadAddress;

  public void changeMyStudio(String name, String roadAddress, String detailAddress) {
    if (name != null) {
      this.name = name;
    }

    if (roadAddress != null) {
      this.roadAddress = roadAddress;
    }

    if (detailAddress != null) {
      this.detailAddress = detailAddress;
    }
  }

}
