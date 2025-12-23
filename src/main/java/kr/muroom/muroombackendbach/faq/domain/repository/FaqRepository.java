package kr.muroom.muroombackendbach.faq.domain.repository;

import feign.Param;
import kr.muroom.muroombackendbach.faq.domain.entity.Faq;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FaqRepository extends JpaRepository<Faq, Long> {

  @EntityGraph(attributePaths = {"category"})
  Page<Faq> findAllByDeletedAtIsNullAndCategory_IsActiveTrue(Pageable pageable);

  @EntityGraph(attributePaths = {"category"})
  @Query("""
      SELECT f
      FROM Faq f
      WHERE f.deletedAt IS NULL
        AND f.category.isActive = true
        AND (
          lower(f.question) LIKE lower(concat('%', :keyword, '%'))
          OR lower(f.answer) LIKE lower(concat('%', :keyword, '%'))
        )
      """)
  Page<Faq> searchForClient(String keyword, Pageable pageable);

  @EntityGraph(attributePaths = {"category"})
  @Query("""
      SELECT f
      FROM Faq f
      WHERE f.deletedAt IS NULL
        AND f.category.isActive = true
        AND f.category.id = :categoryId
      """)
  Page<Faq> findAllByCategory(Long categoryId, Pageable pageable);

  @EntityGraph(attributePaths = {"category"})
  @Query("""
      SELECT f
      FROM Faq f
      WHERE f.deletedAt IS NULL
        AND f.category.isActive = true
        AND f.category.id = :categoryId
        AND (
          lower(f.question) LIKE lower(concat('%', :keyword, '%'))
          OR lower(f.answer) LIKE lower(concat('%', :keyword, '%'))
        )
      """)
  Page<Faq> searchByKeywordAndCategory(String keyword, Long categoryId, Pageable pageable);
}
