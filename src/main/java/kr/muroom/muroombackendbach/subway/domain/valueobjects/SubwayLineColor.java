package kr.muroom.muroombackendbach.subway.domain.valueobjects;

import java.util.Arrays;
import lombok.Getter;

@Getter
public enum SubwayLineColor {
  LINE_1("#0051A3", "1호선", "01호선", "경부선", "경인선", "경원선", "장항선"),
  LINE_2("#00A74C", "2호선", "02호선"),
  LINE_3("#EC6A00", "3호선", "03호선", "일산선"),
  LINE_4("#0797CB", "4호선", "04호선", "안산선", "과천선", "진접선"),
  LINE_5("#774898", "5호선", "05호선"),
  LINE_6("#733819", "6호선", "06호선"),
  LINE_7("#686E32", "7호선", "07호선", "7호선(인천)"),
  LINE_8("#D72171", "8호선", "08호선", "별내선"),
  LINE_9("#A49D89", "9호선", "09호선", "9호선(연장)"),
  GYEONGUI_JUNGANG("#60C3AD", "경의중앙선", "중앙선"),
  SUIN_BUNDANG("#E2A40E", "수인분당선", "수인선", "분당선"),
  AREX("#0090D2", "공항철도", "공항철도1호선"),
  INCHEON_1("#86B0E1", "인천1호선"),

  GYEONGCHUN("#0C8E72", "경춘선"),
  SHINBUNDANG("#D4003B", "신분당선", "신분당선(연장)", "신분당선(연장2)"),
  UI_SINSEOL("#B0CE18", "우이신설선"),
  SILLIM("#6789CA", "신림선"),
  GIMPO_GOLD("#A17800", "김포골드라인"),
  SEOHAE("#81A914", "서해선"),
  EVERLINE("#56AD2D", "에버라인선", "용인경전철"),
  UIJEONGBU("#FD8100", "의정부선", "의정부경전철"),
  GTX_A("#9A6292", "수도권 광역급행철도", "GTX-A"),
  INCHEON_2("#ED8B00", "인천2호선");

  private final String colorCode;
  private final String[] mappingNames;

  SubwayLineColor(String colorCode, String... mappingNames) {
    this.colorCode = colorCode;
    this.mappingNames = mappingNames;
  }

  public static String findColorByRouteName(String routeName) {
    return Arrays.stream(values())
        .filter(line -> Arrays.asList(line.mappingNames).contains(routeName))
        .findFirst()
        .map(SubwayLineColor::getColorCode)
        .orElse("#808080"); // 매핑 안 되면 회색
  }
}