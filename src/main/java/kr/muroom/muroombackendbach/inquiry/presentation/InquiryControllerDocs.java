package kr.muroom.muroombackendbach.inquiry.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.common.presentation.response.PaginatedData;
import kr.muroom.muroombackendbach.inquiry.presentation.dto.request.RegisterInquiryRequest;
import kr.muroom.muroombackendbach.inquiry.presentation.dto.request.SearchInquiryRequest;
import kr.muroom.muroombackendbach.inquiry.presentation.dto.response.InquiryAllResponse;
import kr.muroom.muroombackendbach.inquiry.presentation.dto.response.InquiryResponse;
import kr.muroom.muroombackendbach.inquiry.presentation.dto.response.SearchInquiryResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "1:1 문의 API", description = "뮤지션 1:1 문의 관련 API")
public interface InquiryControllerDocs {

  @Operation(
      summary = "내 1:1 문의 목록 조회",
      description = "로그인한 사용자의 1:1 문의 목록을 조회합니다."
  )
  @GetMapping("/my")
  ApiResponse<PaginatedData<InquiryAllResponse>> getMyInquiry(
      @Parameter(hidden = true)
      Long musicianId,
      @Parameter(hidden = true)
      Pageable pageable
  );

  @Operation(
      summary = "1:1 문의 등록",
      description = "로그인한 사용자가 1:1 문의를 등록합니다."
  )
  @PostMapping
  ApiResponse<Void> registerInquiry(
      @Parameter(hidden = true)
      Long musicianId,
      RegisterInquiryRequest request
  );

  @Operation(
      summary = "나의 1:1 문의 검색"
  )
  @GetMapping("/search")
  ApiResponse<PaginatedData<SearchInquiryResponse>> searchInquiry(
      Long musicianId,
      String keyword,
      @Parameter(hidden = true)
      Pageable pageable
  );

  @Operation(
      summary = "1:1 문의 상세 조회",
      description = "문의 ID로 1:1 문의 상세 내용을 조회합니다."
  )
  @GetMapping("/{inquiryId}")
  ApiResponse<InquiryResponse> getInquiry(
      @Parameter(hidden = true)
      Long musicianId,
      Long inquiryId
  );
}
