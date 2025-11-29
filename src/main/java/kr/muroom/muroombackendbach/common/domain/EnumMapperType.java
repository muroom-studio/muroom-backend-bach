package kr.muroom.muroombackendbach.common.domain;

/**
 * EnumMapperType 인터페이스는 열거형(enum) 타입이 구현해야 하는 메서드를 정의합니다.
 *
 * <p>각 열거형은 고유한 코드와 설명을 제공해야 합니다.
 */
public interface EnumMapperType {

  /**
   * 열거형의 고유 코드를 반환합니다.
   *
   * @return 열거형 코드
   */
  String getCode();

  /**
   * 열거형의 설명을 반환합니다.
   *
   * @return 열거형 설명
   */
  String getDescription();

}
