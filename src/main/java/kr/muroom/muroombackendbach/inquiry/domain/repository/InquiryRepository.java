package kr.muroom.muroombackendbach.inquiry.domain.repository;

import feign.Param;
import kr.muroom.muroombackendbach.inquiry.domain.entity.Inquiry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

  Page<Inquiry> findAllByMusicianId(Long musicianId, Pageable pageable);

  @Query("""
        select i
        from Inquiry i
        where i.deletedAt is null
          and i.musician.id = :musicianId
          and (
            lower(i.title) like lower(concat('%', :keyword, '%'))
            or lower(i.content) like lower(concat('%', :keyword, '%'))
          )
        order by i.id desc
      """)
  Page<Inquiry> searchByKeyword(@Param("musicianId") Long musicianId,
      @Param("keyword") String keyword,
      Pageable pageable);
}
