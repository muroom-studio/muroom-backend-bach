package kr.muroom.muroombackendbach.report.domain.repository;

import feign.Param;
import kr.muroom.muroombackendbach.report.domain.entity.Report;
import kr.muroom.muroombackendbach.report.domain.enums.ReportDomainType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReportRepository extends JpaRepository<Report, Long> {

  boolean existsByReporterIdAndTargetTypeAndTargetId(Long id, ReportDomainType type, Long domainId);

  Page<Report> findByReporterId(Long reporterId, Pageable pageable);

  @Query("""
      select r
      from Report r
      where r.reporter.id = :musicianId
        and (
          :keyword = ''
          or lower(coalesce(r.description, '')) like lower(concat('%', :keyword, '%'))
        )
      order by r.createdAt desc
      """)
  Page<Report> searchByDescription(
      @Param("musicianId") Long musicianId,
      @Param("keyword") String keyword,
      Pageable pageable
  );
}
