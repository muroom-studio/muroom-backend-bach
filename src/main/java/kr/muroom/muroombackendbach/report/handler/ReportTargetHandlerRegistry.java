package kr.muroom.muroombackendbach.report.handler;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import kr.muroom.muroombackendbach.common.exception.BusinessException;
import kr.muroom.muroombackendbach.report.domain.enums.ReportDomainType;
import kr.muroom.muroombackendbach.report.exception.ReportErrorCode;
import org.springframework.stereotype.Component;

@Component
public class ReportTargetHandlerRegistry {

  private final Map<ReportDomainType, ReportTargetHandler> map;

  public ReportTargetHandlerRegistry(List<ReportTargetHandler> handlers) {
    this.map = handlers.stream().collect(Collectors.toMap(ReportTargetHandler::supports, h -> h));
  }

  public ReportTargetHandler get(ReportDomainType type) {
    ReportTargetHandler handler = map.get(type);
    if (handler == null) {
      throw new BusinessException(ReportErrorCode.REPORT_UNSUPPORTED_TARGET_TYPE);
    }
    return handler;
  }
}
