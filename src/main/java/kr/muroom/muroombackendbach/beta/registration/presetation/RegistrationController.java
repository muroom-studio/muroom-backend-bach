package kr.muroom.muroombackendbach.beta.registration.presetation;

import java.util.List;
import kr.muroom.muroombackendbach.beta.registration.application.RegistrationService;
import kr.muroom.muroombackendbach.beta.registration.presetation.dto.RegistrationDto;
import kr.muroom.muroombackendbach.beta.registration.presetation.dto.RegistrationDto.GetResponse;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.filestorage.presentation.dto.response.GeneratePresignedUrlsPutResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/beta/registrations")
public class RegistrationController {

  private final RegistrationService registrationService;

  @PostMapping("/presigned-url")
  public ApiResponse<GeneratePresignedUrlsPutResponse> generatePresignedUrls(
      @Validated @RequestBody RegistrationDto.GeneratePresignedUrlsRequest request) {
    GeneratePresignedUrlsPutResponse response = registrationService.generatePresignedPutUrls(
        request);
    return ApiResponse.success(response);
  }

  @PostMapping
  public ApiResponse<Void> createRegistration(
      @Validated @RequestBody RegistrationDto.CreateRequest request) {
    registrationService.addNewRegistration(request);
    return ApiResponse.created();
  }

  @GetMapping
  public ApiResponse<List<RegistrationDto.GetResponse>> getRegistration() {
    List<GetResponse> response = registrationService.getAllRegistrations();
    return ApiResponse.success(response);
  }
}
