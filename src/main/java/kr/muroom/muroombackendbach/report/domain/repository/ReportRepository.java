package kr.muroom.muroombackendbach.report.domain.repository;

import kr.muroom.muroombackendbach.report.domain.entity.Report;
import kr.muroom.muroombackendbach.report.domain.enums.ReportDomainType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {

  boolean existsByReporterIdAndTargetTypeAndTargetId(Long id, ReportDomainType type, Long domainId);

  Page<Report> findByReporterId(Long reporterId, Pageable pageable);
}
