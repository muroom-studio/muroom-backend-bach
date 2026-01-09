package kr.muroom.muroombackendbach.sms.presentation;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Locale;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.sms.application.SmsVerificationService;
import kr.muroom.muroombackendbach.sms.presentation.docs.MusicianSmsControllerDocs;
import kr.muroom.muroombackendbach.sms.presentation.dto.SmsAuthResponse;
import kr.muroom.muroombackendbach.sms.presentation.dto.request.MusicianSmsSendRequest;
import kr.muroom.muroombackendbach.sms.presentation.dto.request.MusicianSmsVerifyRequest;
import kr.muroom.muroombackendbach.sms.presentation.dto.response.SmsVerifyResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/sms")
@RestController
@RequiredArgsConstructor
@Slf4j
public class SmsController implements MusicianSmsControllerDocs {

  private final SmsVerificationService smsVerificationService;

  @PostMapping("/send-verification")
  public ApiResponse<SmsAuthResponse> authSend(
      @Validated @RequestBody MusicianSmsSendRequest request,
      HttpServletRequest httpRequest) {
    String ip = firstValidIp(
        httpRequest.getHeader("X-Forwarded-For"),
        httpRequest.getHeader("X-Real-IP"),
        httpRequest.getHeader("Forwarded"),
        httpRequest.getHeader("CF-Connecting-IP"),
        httpRequest.getHeader("CloudFront-Viewer-Address")
    );
    log.debug("ip: {}", ip);
    return ApiResponse.success(smsVerificationService.sendVerificationCode(request.phone(), ip));
  }

  @PostMapping("/verify")
  public ApiResponse<SmsVerifyResponse> verify(
      @Validated @RequestBody MusicianSmsVerifyRequest request) {
    return ApiResponse.success(smsVerificationService.verifyCode(request.phone(), request.code()));
  }

  private String firstValidIp(String... candidates) {
    for (String c : candidates) {
      String ip = extractIp(c);
      if (isValidIp(ip)) {
        return ip;
      }
    }
    return null;
  }

  private String extractIp(String raw) {
    if (raw == null) {
      return null;
    }
    String v = raw.trim();
    if (v.isEmpty() || "unknown".equalsIgnoreCase(v)) {
      return null;
    }

    // X-Forwarded-For: "client, proxy1, proxy2" → 첫 번째가 원본일 가능성 가장 큼
    if (v.contains(",")) {
      v = v.split(",")[0].trim();
    }

    // Forwarded: for=1.2.3.4;proto=https 형태 → for= 값 파싱(간단 버전)
    // for="1.2.3.4:1234" 또는 for=1.2.3.4 등 다양
    if (v.toLowerCase(Locale.ROOT).contains("for=")) {
      int idx = v.toLowerCase().indexOf("for=");
      v = v.substring(idx + 4);
      int end = v.indexOf(';');
      if (end >= 0) {
        v = v.substring(0, end);
      }
      v = v.replace("\"", "").trim();
      // 포트 제거
      int colon = v.indexOf(':');
      if (colon > 0 && v.contains(".")) {
        v = v.substring(0, colon);
      }
      // IPv6는 [::] 형태일 수 있음
      v = v.replace("[", "").replace("]", "");
    }

    // CloudFront-Viewer-Address: "1.2.3.4:12345" 형태가 올 수 있음
    if (v.contains(":") && v.contains(".")) {
      // IPv4:port
      v = v.substring(0, v.indexOf(':')).trim();
    }

    return v;
  }

  private boolean isValidIp(String ip) {
    if (ip == null) {
      return false;
    }
    String v = ip.trim();
    if (v.isEmpty() || "unknown".equalsIgnoreCase(v)) {
      return false;
    }

    // 너무 검증하면 정상 케이스도 누락될 수 있어 “약하게”만 체크
    // (실무에서는 InetAddress로 검증해도 되지만 여기선 예외/성능/IPv6 파싱 이슈 줄이려고 최소화)
    return v.matches("^(\\d{1,3}\\.){3}\\d{1,3}$") || v.contains(":"); // IPv4 또는 IPv6 느낌
  }
}
