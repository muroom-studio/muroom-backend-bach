package kr.muroom.muroombackendbach.studio.application;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import kr.muroom.muroombackendbach.studio.domain.entity.Option;
import kr.muroom.muroombackendbach.studio.domain.enums.FloorType;
import kr.muroom.muroombackendbach.studio.domain.enums.OptionCategory;
import kr.muroom.muroombackendbach.studio.domain.enums.ParkingFeeType;
import kr.muroom.muroombackendbach.studio.domain.enums.RestroomType;
import kr.muroom.muroombackendbach.studio.domain.repository.OptionRepository;
import kr.muroom.muroombackendbach.studio.presentation.dto.response.StudioOptionResponse;
import kr.muroom.muroombackendbach.studio.presentation.dto.response.StudioOptionResponse.GetSingle;
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
        .map(GetSingle::from)
        .toList();

    List<GetSingle> restroomOptions = Arrays.stream(RestroomType.values())
        .map(GetSingle::from)
        .toList();

    List<GetSingle> parkingFeeOptions = Arrays.stream(ParkingFeeType.values())
        .map(GetSingle::from)
        .toList();

    Map<OptionCategory, List<Option>> optionsByCategory = optionRepository.findAll()
        .stream()
        .collect(Collectors.groupingBy(Option::getCategory));

    List<GetSingle> studioCommonOptions = optionsByCategory
        .getOrDefault(OptionCategory.COMMON, List.of()).stream()
        .map(GetSingle::from)
        .toList();

    List<GetSingle> studioIndividualOptions = optionsByCategory
        .getOrDefault(OptionCategory.INDIVIDUAL, List.of()).stream()
        .map(GetSingle::from)
        .toList();

    List<Instrument> instrumentEntityList = instrumentRepository.findAll();
    List<GetSingle> unavailableInstrumentOptions = instrumentEntityList.stream()
        .map(GetSingle::from)
        .toList();

    return StudioOptionResponse.GetAll.builder()
        .floorOptions(floorOptions)
        .restroomOptions(restroomOptions)
        .parkingFeeOptions(parkingFeeOptions)
        .studioCommonOptions(studioCommonOptions)
        .studioIndividualOptions(studioIndividualOptions)
        .unavailableInstrumentOptions(unavailableInstrumentOptions)
        .build();
  }
}
