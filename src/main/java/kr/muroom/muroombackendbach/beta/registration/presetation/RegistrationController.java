package kr.muroom.muroombackendbach.beta.registration.presetation;

import kr.muroom.muroombackendbach.beta.registration.application.RegistrationService;
import kr.muroom.muroombackendbach.beta.registration.presetation.dto.RegistrationDto;
import kr.muroom.muroombackendbach.beta.registration.presetation.dto.RegistrationDto.GetResponse;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.common.presentation.response.PaginatedData;
import kr.muroom.muroombackendbach.filestorage.presentation.dto.response.GeneratePresignedUrlsPutResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 베타 서버에서 매물 등록 관련 API를 처리하는 컨트롤러입니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/beta/registrations")
public class RegistrationController {

  private final RegistrationService registrationService;

  /**
   * 매물 관련 이미지 업로드를 위한 사전 서명된 URL을 생성합니다.
   *
   * @param request 사전 서명된 URL 생성을 위한 요청 데이터
   * @return 생성된 사전 서명된 URL 정보
   */
  @PostMapping("/presigned-url")
  public ApiResponse<GeneratePresignedUrlsPutResponse> generatePresignedUrls(
      @Validated @RequestBody RegistrationDto.GeneratePresignedUrlsRequest request) {
    GeneratePresignedUrlsPutResponse response = registrationService.generatePresignedPutUrls(
        request);
    return ApiResponse.success(response);
  }

  /**
   * 새로운 매물 등록을 생성합니다.
   *
   * @param request 매물 등록 생성을 위한 요청 데이터
   * @return 생성 성공 응답
   */
  @PostMapping
  public ApiResponse<Void> createRegistration(
      @Validated @RequestBody RegistrationDto.CreateRequest request) {
    registrationService.addNewRegistration(request);
    return ApiResponse.created();
  }

  /**
   * 모든 매물 등록 정보를 페이징 처리하여 조회합니다.
   *
   * @param pageable 페이징 및 정렬 정보를 포함하는 Pageable 객체
   * @return 페이징된 매물 등록 정보 목록
   */
  @GetMapping
  public ApiResponse<PaginatedData<GetResponse>> getRegistration(
      @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
  ) {
    Page<GetResponse> registrations = registrationService.getAllRegistrations(pageable);
    PaginatedData<GetResponse> response = PaginatedData.from(registrations);
    return ApiResponse.success(response);
  }

  /**
   * 매물 등록의 통계 정보를 조회합니다.
   *
   * @return 매물 등록 통계 정보
   */
  @GetMapping("/counts")
  public ApiResponse<RegistrationDto.CountResponse> getRegistrationCounts() {
    RegistrationDto.CountResponse response = registrationService.getRegistrationCounts();
    return ApiResponse.success(response);
  }
}
