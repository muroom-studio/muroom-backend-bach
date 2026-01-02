package kr.muroom.muroombackendbach.report.domain.repository;

import java.util.List;
import kr.muroom.muroombackendbach.report.domain.entity.ReportReason;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportReasonRepository extends JpaRepository<ReportReason, Long> {

  List<ReportReason> findAllByOrderBySequenceAscIdAsc();
}
