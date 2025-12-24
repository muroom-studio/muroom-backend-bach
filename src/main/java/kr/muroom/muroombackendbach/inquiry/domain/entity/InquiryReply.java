package kr.muroom.muroombackendbach.inquiry.domain.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import kr.muroom.muroombackendbach.common.domain.AuditableEntity;
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
@Table(name = "inquiry_replies")
public class InquiryReply extends AuditableEntity {

  @Id
  @Tsid
  @Column(name = "inquiry_reply_id")
  private Long id;

  @OneToMany(mappedBy = "inquiryReply", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
  private List<InquiryReplyImage> inquiryReplyImages = new ArrayList<>();

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "inquiry_id", nullable = false)
  private Inquiry inquiry;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String content;

  public void changeContent(String content) {
    this.content = content;
  }

  public void replaceImages(List<String> imageKeys) {
    this.inquiryReplyImages.clear();

    if (imageKeys == null || imageKeys.isEmpty()) {
      return;
    }

    for (String key : imageKeys) {
      if (key == null || key.isBlank()) {
        continue;
      }

      InquiryReplyImage image = InquiryReplyImage.builder()
          .inquiryReply(this)
          .imageKey(key.trim())
          .build();

      this.inquiryReplyImages.add(image);
    }
  }

}