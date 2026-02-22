package kr.muroom.muroombackendbach.common.util;

public final class SubjectParser {

  public static Subject parse(String subjectId) {
    if (subjectId == null) {
      return null;
    }

    int idx = subjectId.indexOf(':');
    if (idx <= 0 || idx == subjectId.length() - 1) {
      return null;
    }

    String prefix = subjectId.substring(0, idx);
    String id = subjectId.substring(idx + 1);

    return new Subject(prefix, id);
  }

  public record Subject(String prefix, String id) {

  }
}
