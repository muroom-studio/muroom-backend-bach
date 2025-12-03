package kr.muroom.muroombackendbach.map.infrastructure.client;

import kr.muroom.muroombackendbach.map.presentation.dto.JusoCoordResponse;
import kr.muroom.muroombackendbach.map.presentation.dto.JusoSearchResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "juso-api-client", url = "https://business.juso.go.kr/addrlink")
public interface JusoApiClient {

  // 1단계: 주소검색 API
  @GetMapping("/addrLinkApi.do")
  JusoSearchResponse searchAddress(
      @RequestParam("confmKey") String confmKey,
      @RequestParam("keyword") String keyword,
      @RequestParam("resultType") String resultType,
      @RequestParam(value = "currentPage", defaultValue = "1") int currentPage,
      @RequestParam(value = "countPerPage", defaultValue = "10") int countPerPage
  );

  // 2단계: 주소좌표변환 API (추가된 메소드)
  @GetMapping("/addrCoordApi.do")
  JusoCoordResponse getCoordinates(
      @RequestParam("confmKey") String confmKey,
      @RequestParam("admCd") String admCd,
      @RequestParam("rnMgtSn") String rnMgtSn,
      @RequestParam("udrtYn") String udrtYn,
      @RequestParam("buldMnnm") String buldMnnm,
      @RequestParam("buldSlno") String buldSlno,
      @RequestParam("resultType") String resultType
  );
}
