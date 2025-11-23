package kr.muroom.muroombackendbach.beta.registration.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "beta_introductory_images")
public class BetaIntroductoryImage {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator =
      "beta_introductory_image_seq_generator")
  @SequenceGenerator(name = "beta_introductory_image_seq_generator", sequenceName =
      "beta_introductory_images_introductory_image_id_seq", allocationSize = 1)
  @Column(name = "introductory_image_id")
  private Long id;

  @Column(nullable = false)
  private String fileKey;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "registration_id", nullable = false)
  private BetaRegistration registration;

  public void linkRegistration(BetaRegistration registration) {
    this.registration = registration;
  }
}
