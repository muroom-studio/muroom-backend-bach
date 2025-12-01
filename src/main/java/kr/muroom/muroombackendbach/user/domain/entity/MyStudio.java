package kr.muroom.muroombackendbach.user.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import kr.muroom.muroombackendbach.common.domain.SoftDeletableEntity;
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
@Table(name = "my_studio")
public class MyStudio extends SoftDeletableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "my_studio_id_seq_gen")
  @SequenceGenerator(name = "my_studio_id_seq_gen", sequenceName = "my_studio_id_seq",
      allocationSize = 1)
  @Column(name = "my_studio_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  private Musician musician;

  @Column
  private String name;

  @Column
  private String detailAddress;

  @Column
  private String postalCode;

  @Column
  private String roadAddress;

}
