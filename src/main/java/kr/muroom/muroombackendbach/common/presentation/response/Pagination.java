package kr.muroom.muroombackendbach.common.presentation.response;

import org.springframework.data.domain.Page;

/**
 * 페이지네이션 정보를 나타내는 레코드 클래스입니다.
 *
 * <p>현재 페이지 번호, 페이지 크기, 총 페이지 수, 총 요소 수, 첫 번째 페이지 여부 및 마지막 페이지 여부를 포함합니다.
 */
public record Pagination(
    int pageNumber,
    int pageSize,
    int totalPages,
    long totalElements,
    boolean isFirst,
    boolean isLast
) {

  /**
   * Spring Data의 {@link Page} 객체로부터 {@link Pagination} 객체를 생성합니다.
   *
   * <p>페이지의 내용물 타입과 무관하므로 와일드카드(<?>)를 사용합니다.
   *
   * @param page 메타 정보를 추출할 Page 객체
   * @return 생성된 Pagination 객체
   */
  public static Pagination from(Page<?> page) {
    return new Pagination(
        page.getNumber(),
        page.getSize(),
        page.getTotalPages(),
        page.getTotalElements(),
        page.isFirst(),
        page.isLast()
    );
  }
}
