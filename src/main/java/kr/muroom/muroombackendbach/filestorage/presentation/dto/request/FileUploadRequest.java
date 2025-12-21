package kr.muroom.muroombackendbach.filestorage.presentation.dto.request;

/**
 * 파일 업로드 요청 DTO 인터페이스입니다.
 */
public interface FileUploadRequest {

  /**
   * 파일 이름을 반환합니다.
   *
   * @return 파일 이름
   */
  String getFileName();

  /**
   * 콘텐츠 타입을 반환합니다.
   *
   * <p>예: "image/png", "image/jpeg"
   *
   * @return 콘텐츠 타입
   */
  String getContentType();

  /**
   * 도메인 디렉토리를 반환합니다.
   *
   * <p>예: "studios/blueprint", "users/profiles"
   *
   * @return 도메인 디렉토리
   */
  String getDomainDirectory();
}