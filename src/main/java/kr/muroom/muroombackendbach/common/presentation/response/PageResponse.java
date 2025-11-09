package kr.muroom.muroombackendbach.common.presentation.response;

import java.util.List;
import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
public class PageResponse<T> {

  private final List<T> content; // 현재 페이지의 데이터 목록
  private final int pageNumber; // 현재 페이지 번호 (0부터 시작)
  private final int pageSize; // 페이지당 데이터 개수
  private final int totalPages; // 전체 페이지 수
  private final long totalElements; // 전체 데이터 개수
  private final boolean isFirst; // 첫 번째 페이지 여부
  private final boolean isLast; // 마지막 페이지 여부

  public PageResponse(Page<T> page) {
    this.content = page.getContent();
    this.pageNumber = page.getNumber();
    this.pageSize = page.getSize();
    this.totalPages = page.getTotalPages();
    this.totalElements = page.getTotalElements();
    this.isFirst = page.isFirst();
    this.isLast = page.isLast();
  }
}
