package kr.muroom.muroombackendbach.common.presentation;

import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 홈 컨트롤러로, 기본적인 API 상태 확인용 엔드포인트를 제공합니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class HomeController {

  /**
   * 서버 상태 확인 및 핑 테스트용 엔드포인트로, "pong" 응답을 반환합니다.
   *
   * @return ApiResponse 객체에 담긴 "pong" 문자열
   */
  @GetMapping("/ping")
  public ApiResponse<String> ping() {
    return ApiResponse.success("pong");
  }
}
