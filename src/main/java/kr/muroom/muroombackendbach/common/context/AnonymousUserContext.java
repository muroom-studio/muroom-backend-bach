package kr.muroom.muroombackendbach.common.context;

/**
 * 익명 사용자 컨텍스트를 관리하는 클래스입니다.
 *
 * <p>ThreadLocal을 사용하여 각 스레드별로 익명 사용자 ID를 저장하고 조회할 수 있습니다.
 */
public class AnonymousUserContext {

  private static final ThreadLocal<String> anonymousUserIdHolder = new ThreadLocal<>();

  /**
   * 현재 스레드에 익명 사용자 ID를 설정합니다.
   *
   * @param anonymousUserId 익명 사용자 ID
   */
  public static void setAnonymousUserId(String anonymousUserId) {
    anonymousUserIdHolder.set(anonymousUserId);
  }

  /**
   * 현재 스레드에서 익명 사용자 ID를 조회합니다.
   *
   * @return 익명 사용자 ID
   */
  public static String getAnonymousUserId() {
    return anonymousUserIdHolder.get();
  }

  /**
   * 현재 스레드에서 익명 사용자 ID를 제거합니다.
   */
  public static void clear() {
    anonymousUserIdHolder.remove();
  }
}
