package kr.muroom.muroombackendbach.withdrawal.domain.entity;

import kr.muroom.muroombackendbach.common.domain.EnumMapperType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WithdrawalReasonCode implements EnumMapperType {

  LACK_OF_LISTING_INFO("원하는 매물 정보가 부족함"),
  LOW_LISTING_TRUST("매물 정보의 신뢰가 부족함"),
  SERVICE_INCONVENIENCE("서비스 이용이 불편함"),
  SEARCH_OR_CONTRACT_COMPLETED("작업실 탐색/계약을 완료함"),
  PURPOSE_NO_LONGER_EXISTS("서비스 이용 목적이 사라짐"),
  USING_OTHER_SERVICE("다른 유사 서비스 이용"),
  REQUEST_PERSONAL_DATA_DELETION("개인 정보 삭제를 원함"),
  PAID_SERVICE_COMPLAINT("유료 서비스/광고에 대한 불만");

  private final String description;

  @Override
  public String getCode() {
    return this.name();
  }
}
