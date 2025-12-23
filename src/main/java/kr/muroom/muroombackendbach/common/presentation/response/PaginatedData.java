package kr.muroom.muroombackendbach.common.presentation.response;

import java.util.List;
import org.springframework.data.domain.Page;

/**
 * 페이징된 데이터를 나타내는 레코드 클래스입니다.
 *
 * <p>컨텐츠 목록 {@code content}와 페이징 정보 {@link Pagination}를 포함합니다.
 *
 * @param <T> 컨텐츠의 타입
 */
public record PaginatedData<T>(
    List<T> content,
    Pagination pagination
) {

  /**
   * 주어진 Page 객체에서 PaginatedData 인스턴스를 생성합니다.
   *
   * @param page 페이징된 데이터가 포함된 Page 객체
   * @param <T>  컨텐츠의 타입
   * @return PaginatedData 인스턴스
   */
  public static <T> PaginatedData<T> from(Page<T> page) {
    return new PaginatedData<>(page.getContent(), Pagination.from(page));
  }
}
