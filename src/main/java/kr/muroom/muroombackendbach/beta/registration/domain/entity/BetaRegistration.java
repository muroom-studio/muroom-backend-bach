package kr.muroom.muroombackendbach.beta.registration.domain.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import kr.muroom.muroombackendbach.common.domain.CreatedDateEntity;
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
@Table(name = "beta_registrations")
public class BetaRegistration extends CreatedDateEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "beta_registration_seq_gen")
  @SequenceGenerator(name = "beta_registration_seq_gen", sequenceName =
      "beta_registrations_registration_id_seq", allocationSize = 1)
  @Column(name = "registration_id")
  private Long id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String phoneNumber;

  @Column(nullable = false)
  private String thirdPartyUrl;

  @Column(nullable = false)
  private Boolean agreedToPersonalInfoCollection;

  @Column(nullable = false)
  private Boolean agreedToContentCollection;

  @Column(nullable = false)
  private Boolean agreedToThirdPartyProvision;

  @Column(nullable = false)
  private Boolean agreedToMarketing;

  private String featureSuggestions;

  @OneToMany(mappedBy = "registration", fetch = FetchType.LAZY, cascade = CascadeType.ALL,
      orphanRemoval = true)
  @Builder.Default
  private List<BetaIntroductoryImage> introductoryImages = new ArrayList<>();

  public void addIntroductoryImage(BetaIntroductoryImage image) {
    image.linkRegistration(this);
    introductoryImages.add(image);
  }
}