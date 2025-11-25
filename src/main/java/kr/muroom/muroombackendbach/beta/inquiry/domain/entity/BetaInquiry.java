package kr.muroom.muroombackendbach.beta.inquiry.domain.entity;

import jakarta.persistence.*;
import kr.muroom.muroombackendbach.common.domain.CreatedDateEntity;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "beta_inquiries")
public class BetaInquiry extends CreatedDateEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "beta_inquiry_seq_generator")
  @SequenceGenerator(name = "beta_inquiry_seq_generator", sequenceName =
      "beta_inquiries_inquiry_id_seq", allocationSize = 1)
  @Column(name = "inquiry_id")
  private Long id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String phoneNumber;

  @Column(nullable = false)
  private String content;

  @Column(nullable = false)
  private Boolean agreedToPrivacy; // 개인정보 수집 및 이용 동의 여부
}
