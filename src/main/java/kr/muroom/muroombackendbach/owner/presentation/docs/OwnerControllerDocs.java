package kr.muroom.muroombackendbach.owner.presentation.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.muroom.muroombackendbach.common.exception.BusinessException;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.owner.presentation.dto.request.OwnerSignupRequest;
import kr.muroom.muroombackendbach.owner.presentation.dto.response.OwnerSignupResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "owner - 사장님 API")
public interface OwnerControllerDocs {

  @Operation(
      summary = "사장님 회원가입",
      description = """
          휴대폰 인증을 완료한 사장님 회원을 등록합니다.
          
          ### 회원가입 처리 순서
          1. 휴대폰 인증 토큰(smsVerifyToken) 검증
          2. 전화번호 / 이메일 / 닉네임 중복 검증
          3. 약관 존재 여부 및 필수 약관 동의 검증
          4. 사장님 계정 생성 및 약관 동의 저장
          5. Access / Refresh Token 발급
          
          ### 주의사항
          - phoneNumber는 요청값이 아닌 **휴대폰 인증 토큰에 포함된 번호**를 기준으로 처리됩니다.
          - 필수 약관이 하나라도 누락되면 회원가입이 실패합니다.
          """
  )
  ApiResponse<OwnerSignupResponse> registerOwner(
      @Valid @RequestBody OwnerSignupRequest request
  );

  @Operation(
      summary = "닉네임 중복 확인",
      description = """
          닉네임 사용 가능 여부를 확인합니다.
          - 사용 가능한 경우: 200 OK
          - 이미 존재하는 닉네임: 409 CONFLICT
          """
  )
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "닉네임 중복 확인 성공"
      ),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "409",
          description = "이미 존재하는 닉네임",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = BusinessException.class),
              examples = @ExampleObject(
                  name = "닉네임 중복",
                  value = """
                      {
                        "code": "OW-409-01",
                        "message": "이미 존재하는 닉네임 입니다."
                      }
                      """
              )
          )
      ),
  })
  ApiResponse<Void> checkNickname(
      @Parameter(
          description = "검사할 닉네임",
          example = "muroom_artist"
      )
      @RequestParam String nickname
  );

  @Operation(
      summary = "전화번호 중복 확인",
      description = """
          전화번호 사용 가능 여부를 확인합니다.
          
          - 사용 가능한 경우: 200 OK
          - 이미 존재하는 전화번호: 409 CONFLICT
          - 잘못된 전화번호 형식: 400 BAD_REQUEST
          """
  )
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "전화번호 사용 가능"
      ),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "409",
          description = "이미 존재하는 전화번호",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = BusinessException.class),
              examples = @ExampleObject(
                  name = "전화번호 중복",
                  value = """
                      {
                        "code": "OW-409-03",
                        "message": "이미 존재하는 전화번호 입니다."
                      }
                      """
              )
          )
      ),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "400",
          description = "잘못된 전화번호 형식",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = BusinessException.class),
              examples = @ExampleObject(
                  name = "잘못된 핸드폰 번호",
                  value = """
                      {
                        "code": "SM-400-01",
                        "message": "잘못된 핸드폰 번호입니다."
                      }
                      """
              )
          )
      )
  })
  ApiResponse<Void> checkPhone(
      @Parameter(
          description = "검사할 전화번호 (하이픈 넣어야함)",
          example = "010-1234-5678"
      )
      @RequestParam String phone
  );
}
