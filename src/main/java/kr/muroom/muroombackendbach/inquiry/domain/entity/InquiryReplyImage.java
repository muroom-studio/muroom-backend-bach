package kr.muroom.muroombackendbach.inquiry.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import kr.muroom.muroombackendbach.common.domain.AuditableEntity;
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
@Table(name = "inquiry_reply_images")
public class InquiryReplyImage extends AuditableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inquiry_reply_image_id_seq_gen")
  @SequenceGenerator(
      name = "inquiry_reply_image_id_seq_gen",
      sequenceName = "inquiry_reply_image_id_seq",
      allocationSize = 1
  )
  @Column(name = "inquiry_reply_image_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "inquiry_reply_id", nullable = false)
  private InquiryReply inquiryReply;

  @Column(name = "image_key", nullable = false, columnDefinition = "TEXT")
  private String imageKey;
}