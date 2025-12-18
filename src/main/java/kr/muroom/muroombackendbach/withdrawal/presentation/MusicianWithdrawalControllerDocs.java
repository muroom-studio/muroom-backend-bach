package kr.muroom.muroombackendbach.withdrawal.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.muroom.muroombackendbach.common.exception.BusinessException;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.withdrawal.presentation.dto.RegisterWithdrawalReasonRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "뮤지션 탈퇴 API", description = "로그인된 뮤지션 탈퇴 관련 API")
public interface MusicianWithdrawalControllerDocs {

  @Operation(
      summary = "뮤지션 탈퇴 요청",
      description = """
          로그인한 뮤지션이 탈퇴를 요청합니다.
          
          - 탈퇴 사유는 필수이며, 사전에 정의된 탈퇴 사유만 사용할 수 있습니다.
          - 탈퇴 시 계정은 즉시 탈퇴 처리됩니다.
          """
  )
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "뮤지션 탈퇴 성공"
      ),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "400",
          description = "잘못된 요청",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = BusinessException.class),
              examples = {
                  @ExampleObject(
                      name = "존재하지 않는 탈퇴 사유",
                      value = """
                          {
                            "code": "WR-400-01",
                            "message": "존재하지 않는 탈퇴 사유입니다."
                          }
                          """,
                      description = "요청한 withdrawalReasonId가 존재하지 않는 경우"
                  ),
                  @ExampleObject(
                      name = "존재하지 않는 뮤지션",
                      value = """
                          {
                            "code": "MC-400-02",
                            "message": "존재하지 않는 뮤지션입니다."
                          }
                          """,
                      description = "인증 정보는 있으나 실제 뮤지션 정보가 존재하지 않는 경우"
                  )
              }
          )
      )
  })
  @PostMapping
  ApiResponse<Void> register(
      @Parameter(hidden = true)
      @AuthenticationPrincipal Long musicianId,
      @RequestBody RegisterWithdrawalReasonRequest request
  );
}
