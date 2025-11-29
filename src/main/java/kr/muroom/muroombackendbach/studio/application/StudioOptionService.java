package kr.muroom.muroombackendbach.studio.application;

import java.util.Arrays;
import java.util.List;
import kr.muroom.muroombackendbach.studio.domain.entity.Option;
import kr.muroom.muroombackendbach.studio.domain.enums.FloorType;
import kr.muroom.muroombackendbach.studio.domain.enums.OptionCategory;
import kr.muroom.muroombackendbach.studio.domain.enums.RestroomType;
import kr.muroom.muroombackendbach.studio.domain.repository.OptionRepository;
import kr.muroom.muroombackendbach.studio.presentation.dto.StudioOptionResponse;
import kr.muroom.muroombackendbach.studio.presentation.dto.StudioOptionResponse.GetSingle;
import kr.muroom.muroombackendbach.user.domain.entity.Instrument;
import kr.muroom.muroombackendbach.user.domain.repository.InstrumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudioOptionService {

  private final OptionRepository optionRepository;
  private final InstrumentRepository instrumentRepository;

  public StudioOptionResponse.GetAll getAllFilterOptions() {
    List<GetSingle> floorOptions = Arrays.stream(FloorType.values())
        .map(floorType -> GetSingle.builder()
            .code(floorType.getCode())
            .description(floorType.getDescription())
            .build()
        ).toList();

    List<GetSingle> restroomOptions = Arrays.stream(RestroomType.values())
        .map(restroomType -> GetSingle.builder()
            .code(restroomType.getCode())
            .description(restroomType.getDescription())
            .build()
        ).toList();

    List<Option> studioCommonOptionEntityList = optionRepository.findAllByCategory(
        OptionCategory.COMMON);
    List<GetSingle> studioCommonOptions = studioCommonOptionEntityList.stream()
        .map(optionEntity -> GetSingle.builder()
            .code(optionEntity.getCode())
            .description(optionEntity.getDescription())
            .build()
        ).toList();

    List<Option> studioIndividualOptionEntityList = optionRepository.findAllByCategory(
        OptionCategory.INDIVIDUAL);
    List<GetSingle> studioIndividulOptions = studioIndividualOptionEntityList.stream()
        .map(optionEntity -> GetSingle.builder()
            .code(optionEntity.getCode())
            .description(optionEntity.getDescription())
            .build()
        ).toList();

    List<Instrument> instrumentEntityList = instrumentRepository.findAll();
    List<GetSingle> unavailableInstrumentOptions = instrumentEntityList.stream()
        .map(instrumentEntity -> GetSingle.builder()
            .code(instrumentEntity.getCode())
            .description(instrumentEntity.getDescription())
            .build()
        ).toList();

    return StudioOptionResponse.GetAll.builder()
        .floorOptions(floorOptions)
        .restroomOptions(restroomOptions)
        .studioCommonOptions(studioCommonOptions)
        .studioIndividualOptions(studioIndividulOptions)
        .unavailableInstrumentOptions(unavailableInstrumentOptions)
        .build();
  }
}
