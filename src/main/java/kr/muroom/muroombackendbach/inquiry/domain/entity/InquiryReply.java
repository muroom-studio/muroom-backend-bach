package kr.muroom.muroombackendbach.inquiry.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.util.List;
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
@Table(name = "inquiry_replies")
public class InquiryReply extends AuditableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "inquiry_reply_id_seq_gen")
  @SequenceGenerator(name = "inquiry_reply_id_seq_gen", sequenceName = "inquiry_reply_id_seq",
      allocationSize = 1)
  @Column(name = "inquiry_reply_id")
  private Long id;

  @OneToMany(fetch = FetchType.LAZY)
  @JoinColumn(name = "inquiry_reply_id")
  private List<InquiryReplyImage> inquiryReplyImage;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "inquiry_id", nullable = false)
  private Inquiry inquiry;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String content;

}