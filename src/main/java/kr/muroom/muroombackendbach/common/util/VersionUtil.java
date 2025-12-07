package kr.muroom.muroombackendbach.common.util;

public final class VersionUtil {

  public static final int MAX_MINOR = 300;

  private VersionUtil() {
  }

  public static String nextVersion(String currentVersion) {
    if (currentVersion == null || !currentVersion.matches("\\d+\\.\\d+")) {
      throw new IllegalArgumentException(
          "Invalid version format: " + currentVersion + " (expected format: X.Y)"
      );
    }

    String[] parts = currentVersion.split("\\.");

    int major = Integer.parseInt(parts[0]);
    int minor = Integer.parseInt(parts[1]);

    if (minor < MAX_MINOR) {
      minor++;
    } else {
      major++;
      minor = 0;
    }

    return major + "." + minor;
  }
}
