package kr.muroom.muroombackendbach.faq.domain.entity;

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
import kr.muroom.muroombackendbach.admin.faq.presentation.dto.UpdateFaqRequest;
import kr.muroom.muroombackendbach.common.domain.SoftDeletableEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(
    sql = """
        UPDATE faqs
        SET deleted_at = CURRENT_TIMESTAMP
        WHERE faq_id = ?
        """
)
@Table(name = "faqs")
public class Faq extends SoftDeletableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "faq_id_seq_gen")
  @SequenceGenerator(
      name = "faq_id_seq_gen",
      sequenceName = "faq_id_seq",
      allocationSize = 1
  )
  @Column(name = "faq_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "faq_category_id", nullable = false)
  private FaqCategory category;

  @Column(name = "question", nullable = false, columnDefinition = "TEXT")
  private String question;

  @Column(name = "answer", nullable = false, columnDefinition = "TEXT")
  private String answer;

  public void updateFaq(String question, String answer) {
    this.question = question;
    this.answer = answer;
  }
}