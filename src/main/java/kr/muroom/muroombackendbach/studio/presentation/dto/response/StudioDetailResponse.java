package kr.muroom.muroombackendbach.studio.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import kr.muroom.muroombackendbach.studio.domain.entity.Studio;
import kr.muroom.muroombackendbach.user.domain.entity.Owner;
import kr.muroom.muroombackendbach.user.presentation.dto.OwnerDto;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.locationtech.jts.geom.Point;

@Builder
@Schema(description = "작업실 상세 내용")
public class StudioDetailResponse {

  public StudioDto studio;
  public OwnerDto owner;

  @Builder
  @Getter
  @ToString
  @JsonInclude(value = JsonInclude.Include.NON_NULL)
  public static class StudioDto {

    private Long id;
    private String name;
    private String address;
    private Point location;
    private Long viewCount = 0L;
    private String introduction;
    private String thumbnailImageKey;
    private String blueprintImageKey;

    public static StudioDto fromEntity(Studio studio) {
      return StudioDto.builder()
          .id(studio.getId())
          .name(studio.getName())
          .address(studio.getAddress())
          .location(studio.getLocation())
          .viewCount(studio.getViewCount())
          .introduction(studio.getIntroduction())
          .build();
    }

  }
}
