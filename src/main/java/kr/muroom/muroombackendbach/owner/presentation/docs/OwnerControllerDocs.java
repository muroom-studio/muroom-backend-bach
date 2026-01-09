package kr.muroom.muroombackendbach.owner.presentation.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.owner.presentation.dto.request.OwnerSignupRequest;
import kr.muroom.muroombackendbach.owner.presentation.dto.response.OwnerSignupResponse;
import org.springframework.web.bind.annotation.RequestBody;

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
}
