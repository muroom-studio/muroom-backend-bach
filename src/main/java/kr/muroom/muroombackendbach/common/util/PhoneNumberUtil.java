package kr.muroom.muroombackendbach.common.util;

import static kr.muroom.muroombackendbach.common.sms.SmsErrorCode.INVALID_PHONE_NUMBER;

import kr.muroom.muroombackendbach.common.exception.BusinessException;

public final class PhoneNumberUtil {

  // 인스턴스 생성 방지
  private PhoneNumberUtil() {
    throw new UnsupportedOperationException("Utility class");
  }

  // 하이픈 제거
  public static String removeHyphens(String phone) {
    if (phone == null) {
      return null;
    }
    return phone.replaceAll("-", "");
  }

  // 휴대폰 번호 형식 검사
  public static boolean isValidPhoneNumber(String phone) {
    if (phone == null) {
      return false;
    }

    String digits = removeHyphens(phone);

    String regex = "^(01[016789])[0-9]{7,8}$";

    return digits.matches(regex);
  }

  // 정규화(하이픈 제거 + 유효성)
  public static String normalize(String phone) {
    if (!isValidPhoneNumber(phone)) {
      throw new BusinessException(INVALID_PHONE_NUMBER);
    }
    return removeHyphens(phone);
  }
}
