package kr.muroom.muroombackendbach.common.exception;

import lombok.Getter;

@Getter
public class ExternalApiException extends RuntimeException {
  
  private final String serviceName;

  public ExternalApiException(String message, String serviceName) {
    super(message);
    this.serviceName = serviceName;
  }

  // 원본 예외(Stack Trace)를 유지하고 싶을 때
  public ExternalApiException(String message, String serviceName, Throwable cause) {
    super(message, cause);
    this.serviceName = serviceName;
  }
}