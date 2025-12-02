package kr.muroom.muroombackendbach.studio.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.time.LocalDate;
import java.util.List;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.common.presentation.response.PaginatedData;
import kr.muroom.muroombackendbach.search.application.SearchHistoryService;
import kr.muroom.muroombackendbach.studio.application.StudioService;
import kr.muroom.muroombackendbach.studio.application.StudioViewService;
import kr.muroom.muroombackendbach.studio.domain.enums.FloorType;
import kr.muroom.muroombackendbach.studio.presentation.dto.request.MapSearchRequest;
import kr.muroom.muroombackendbach.studio.presentation.dto.response.StudioDetailResponse;
import kr.muroom.muroombackendbach.studio.presentation.dto.response.StudioInfo;
import kr.muroom.muroombackendbach.studio.presentation.dto.response.StudioListResponse;
import kr.muroom.muroombackendbach.studio.presentation.dto.response.StudioMapResponse;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/studios")
public class StudioController {

  private final StudioService studioService;
  private final SearchHistoryService searchHistoryService;
  private final StudioViewService studioViewService;

  @GetMapping("/map-search")
  public ApiResponse<List<StudioMapResponse>> searchStudiosInMapBounds(
      @Validated @ParameterObject MapSearchRequest request
  ) {
    List<StudioMapResponse> response = studioService.searchStudiosInMapBounds(request);
    return ApiResponse.success(response);
  }

  @Operation(summary = "지도 기반 스튜디오 목록 페이지네이션 조회",
      description = "지도 범위 내의 스튜디오 목록을 페이지네이션하여 조회합니다. 기본 정렬은 최신순입니다.",
      parameters = {
          @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", example = "0"),
          @Parameter(name = "size", description = "페이지 당 항목 수", example = "10"),
          @Parameter(name = "sort", description = "정렬 기준 (예: 'price,asc', 'latest,desc'). 생략 시 "
              + "기본값은 'latest,desc' (최신순) 입니다.",
              example = "price,desc")
      }
  )
  // @SecurityRequirement(name = "Authorization")
  @GetMapping("/map-list")
  public ApiResponse<PaginatedData<StudioListResponse>> searchStudiosForMapList(
      @Validated @ParameterObject MapSearchRequest request,
      @AuthenticationPrincipal Long musicianId,
      @Parameter(hidden = true)
      @PageableDefault(sort = "latest", direction = Direction.DESC) Pageable pageable
  ) {
    if (request.keyword() != null && !request.keyword().isBlank()) {
      searchHistoryService.addSearchKeyword(musicianId, request.keyword());
    }

    Page<StudioListResponse> response = studioService.searchStudiosForMapList(request, pageable);
    return ApiResponse.success(PaginatedData.from(response));
  }

  @Operation(summary = "스튜디오 상세 조회", description = "스튜디오의 상세 정보를 조회합니다.")
  @GetMapping("/{studioId}")
  public ApiResponse<StudioDetailResponse> getStudio(@PathVariable Long studioId, @AuthenticationPrincipal Long musicianId) {
    studioViewService.incrementViewCount(musicianId, studioId);

    StudioDetailResponse response = studioService.getStudio(studioId);

    return ApiResponse.success(createMockResponse(studioId));
  }

  /**
   * 프론트엔드 개발을 위한 StudioDetailResponse 목업 객체를 생성합니다.
   *
   * @param studioId 요청된 스튜디오 ID
   * @return StudioDetailResponse 목업 객체
   */
  private StudioDetailResponse createMockResponse(Long studioId) {
    // 1. 하위 DTO들 생성
    var studioBaseInfo = StudioDetailResponse.StudioBaseInfoDto.builder()
        .studioId(studioId)
        .studioName("뮤룸 스튜디오 (목업 데이터)")
        .address("서울특별시 강남구 테헤란로 427 위워크타워 5층")
        .studioMinPrice(150000)
        .studioMaxPrice(430000)
        .nearbySubwayStationInfo(StudioInfo.StudioSubwayStationInfo.builder()
            .stationName("강남역")
            .lines(List.of(
                new StudioInfo.StudioSubwayLineInfo("2호선", "#1DB446"),
                new StudioInfo.StudioSubwayLineInfo("신분당선", "#FF3300")
            ))
            .build())
        .walkingTimeMinutesToSubwayStation(8)
        .studioMainImageUrls(List.of(
            "https://example.com/studio_main1.jpg",
            "https://example.com/studio_main2.jpg",
            "https://example.com/studio_main3.jpg"
        ))
        .build();

    var studioBuildingInfo = StudioDetailResponse.StudioBuildingInfoDto.builder()
        .floorType(FloorType.GROUND)
        .floorNumber(5)
        .isParkingAvailable("true")
        .parkingFeeType("FREE")
        .parkingFeeInfo("매월 3만원")
        .parkingSpots(6)
        .parkingLocationAddress("서울특별시 강남구 테헤란로 427 위워크타워 지하 1층")
        .parkingLocationInfo("빌딩 지하 주차장 이용")
        .isLodgingAvailable(true)
        .hasFireInsurance(true)
        .depositAmount(50000)
        .studioBuildingImageUrls(List.of(
            "https://example.com/building1.jpg",
            "https://example.com/building2.jpg"
        ))
        .build();

    var studioNotice = StudioDetailResponse.StudioNoticeDto.builder()
        .ownerNickname("뮤루뮤루")
        .experienceYears(5)
        .isIdentityVerified(false)
        .introduction("안녕하세요! 뮤룸 스튜디오입니다.\n저희 스튜디오는 쾌적한 환경과 최신 장비를 갖추고 있어 여러분의 창작 활동을 지원합니다.\n많은 관심 부탁드립니다!")
        .build();

    var studioRooms = StudioDetailResponse.StudioRoomsDto.builder()
        .forbiddenInstruments(List.of("드럼", "금관"))
        .roomImageUrls(List.of(
            "https://example.com/room1.jpg",
            "https://example.com/room2.jpg"
        ))
        .rooms(List.of(
            StudioDetailResponse.RoomInfoDto.builder()
                .roomId(132L)
                .roomName("Room A")
                .isAvailable(true)
                .availableAt(LocalDate.of(2024, 11, 15))
                .widthMm(5000)
                .heightMm(4000)
                .roomBasePrice(250000)
                .build(),
            StudioDetailResponse.RoomInfoDto.builder()
                .roomId(133L)
                .roomName("Room B")
                .isAvailable(false)
                .availableAt(LocalDate.of(2024, 12, 1))
                .widthMm(6000)
                .heightMm(4500)
                .roomBasePrice(300000)
                .build()
        ))
        .build();

    var studioOptions = StudioDetailResponse.OptionsDto.builder()
        .commonOptions(List.of(
            new StudioDetailResponse.OptionDto("WATER_PURIFIER", "정수기", "https://example.com/icons/water_purifier.png"),
            new StudioDetailResponse.OptionDto("AIR_CONDITIONER", "에어컨", "https://example.com/icons/air_conditioner.png")
        ))
        .individualOptions(List.of(
            new StudioDetailResponse.OptionDto("INDIVIDUAL_AC", "개별 에어컨", "https://example.com/icons/air_conditioner.png"),
            new StudioDetailResponse.OptionDto("WINDOW", "창문", "https://example.com/icons/window.png")
        ))
        .build();

    // 2. 최종 DTO 조립
    return StudioDetailResponse.builder()
        .studioBaseInfo(studioBaseInfo)
        .studioBuildingInfo(studioBuildingInfo)
        .studioNotice(studioNotice)
        .studioRooms(studioRooms)
        .studioOptions(studioOptions)
        .build();
  }
}
