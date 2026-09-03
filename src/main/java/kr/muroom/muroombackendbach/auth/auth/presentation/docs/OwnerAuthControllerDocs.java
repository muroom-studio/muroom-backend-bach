package kr.muroom.muroombackendbach.auth.auth.presentation.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import kr.muroom.muroombackendbach.auth.auth.presentation.dto.request.OwnerLoginRequest;
import kr.muroom.muroombackendbach.auth.auth.presentation.dto.response.OwnerLoginResponse;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "owner-auth - 사장님 인증 API")
public interface OwnerAuthControllerDocs {

  @Operation(
      summary = "사장님 비밀번호 로그인",
      description = """
          이메일과 비밀번호를 사용하여 사장님 계정으로 로그인합니다.
          
          ### 로그인 처리 방식
          1. 이메일 + 비밀번호 기반 인증 수행
          2. 인증 실패 시 로그인 실패 처리
          3. 인증 성공 시 Access / Refresh Token 발급
          4. Refresh Token은 서버 저장소에 저장됩니다.
          
          ### 주의사항
          - 이메일 또는 비밀번호가 올바르지 않으면 로그인에 실패합니다.
          - Refresh Token은 재발급 및 로그아웃 처리를 위해 서버에서 관리됩니다.
          """
  )
  ApiResponse<OwnerLoginResponse> login(
      @Valid @RequestBody OwnerLoginRequest request, HttpServletRequest httpRequest,
      HttpServletResponse httpResponse
  );
}
