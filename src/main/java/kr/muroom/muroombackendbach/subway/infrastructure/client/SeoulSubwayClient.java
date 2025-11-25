package kr.muroom.muroombackendbach.subway.infrastructure.client;

import kr.muroom.muroombackendbach.subway.presentation.dto.SeoulSubwayStationApiDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "seoul-subway-client", url = "http://openapi.seoul.go.kr:8088")
public interface SeoulSubwayClient {

  @GetMapping("/{apiKey}/json/subwayStationMaster/{startIndex}/{endIndex}/")
  SeoulSubwayStationApiDto.Response getSubwayStations(@PathVariable("apiKey") String apiKey,
      @PathVariable("startIndex") int startIndex, @PathVariable("endIndex") int endIndex);
}