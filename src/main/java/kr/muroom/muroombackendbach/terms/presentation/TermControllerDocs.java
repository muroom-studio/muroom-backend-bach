package kr.muroom.muroombackendbach.terms.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import kr.muroom.muroombackendbach.common.exception.BusinessException;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.terms.domain.entity.TermsType;
import kr.muroom.muroombackendbach.terms.presentation.dto.request.TermRegisterRequest;
import kr.muroom.muroombackendbach.terms.presentation.dto.request.TermUpdateRequest;
import kr.muroom.muroombackendbach.terms.presentation.dto.response.TermDetailResponse;
import kr.muroom.muroombackendbach.terms.presentation.dto.response.TermSimpleResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "terms - 약관 API")
public interface TermControllerDocs {

  @Operation(
      summary = "뮤지션 회원가입 약관 조회",
      description = "뮤지션 회원가입 시 필요한 약관들을 조회합니다."
  )
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "약관 목록 조회 성공"
      )
  })
  @GetMapping("/musician/signup")
  ApiResponse<List<TermDetailResponse>> getMusicianTerms();

  @Operation(
      summary = "약관 상세 조회",
      description = "약관 ID로 약관의 상세 내용을 조회합니다."
  )
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "약관 상세 조회 성공"
      ),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "400",
          description = "잘못된 요청 (존재하지 않는 약관)",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = BusinessException.class),
              examples = {
                  @ExampleObject(
                      name = "존재하지 않는 약관",
                      value = """
                          {
                            "code": "TR-400-01",
                            "message": "존재하지 않는 약관입니다."
                          }
                          """,
                      description = "termId에 해당하는 약관이 DB에 존재하지 않는 경우"
                  )
              }
          )
      )
  })
  @GetMapping("/{termId}")
  ApiResponse<TermSimpleResponse> getTermById(@PathVariable Long termId);

  @Operation(
      summary = "뮤지션 약관 등록",
      description = "뮤지션 약관을 등록합니다."
  )
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "약관 등록 성공"
      )
  })
  @PostMapping
  ApiResponse<Void> registerMusicianTerms(
      @Validated @RequestBody TermRegisterRequest request
  );

  @Operation(
      summary = "뮤지션 약관 수정",
      description = "약관 ID로 약관 내용을 수정합니다."
  )
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "약관 수정 성공"
      ),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "400",
          description = "잘못된 요청 (존재하지 않는 약관)",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = BusinessException.class),
              examples = {
                  @ExampleObject(
                      name = "존재하지 않는 약관",
                      value = """
                          {
                            "code": "TR-400-01",
                            "message": "존재하지 않는 약관입니다."
                          }
                          """,
                      description = "termId에 해당하는 약관이 DB에 존재하지 않는 경우"
                  )
              }
          )
      )
  })
  @PutMapping("/{termId}")
  ApiResponse<Void> updateMusicianTerms(
      @PathVariable Long termId,
      @RequestBody TermUpdateRequest request
  );

  @Operation(
      summary = "사장님 약관 조회",
      description = "사장님(Owner) 가입/이용에 필요한 약관을 types로 조회합니다."
  )
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "약관 목록 조회 성공"
      )
  })
  @GetMapping("/owner")
  ApiResponse<List<TermDetailResponse>> getOwnerTerms();
}
