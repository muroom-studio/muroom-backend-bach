package kr.muroom.muroombackendbach.common.util;

import static kr.muroom.muroombackendbach.common.sms.exception.SmsErrorCode.INVALID_PHONE_NUMBER;

import kr.muroom.muroombackendbach.common.exception.BusinessException;

public final class PhoneNumberUtil {

  private static final String HYPHEN_PHONE_REGEX = "^(01[016789])-(\\d{3}|\\d{4})-\\d{4}$";

  private PhoneNumberUtil() {
    throw new UnsupportedOperationException("Utility class");
  }

  public static String removeHyphens(String phone) {
    isValidHyphenPhoneNumber(phone);
    return phone.replaceAll("-", "");
  }

  public static void isValidHyphenPhoneNumber(String phone) {
    if (phone == null || !phone.matches(HYPHEN_PHONE_REGEX)) {
      throw new BusinessException(INVALID_PHONE_NUMBER);
    }
  }
}
