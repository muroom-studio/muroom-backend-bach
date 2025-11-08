package kr.muroom.muroombackendbach.common.presentation;

import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class HomeController {

  @GetMapping("/ping")
  public ApiResponse<String> ping() {
    return ApiResponse.success("pong");
  }
}
