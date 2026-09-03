package kr.muroom.muroombackendbach.common.util;

public final class VersionUtil {

  // patch가 이 값에 도달하면 minor 올리고 patch를 0으로 초기화
  public static final int MAX_PATCH = 300;

  private VersionUtil() {
  }

  public static String nextVersion(String currentVersion) {
    if (currentVersion == null || !currentVersion.matches("\\d+\\.\\d+\\.\\d+")) {
      throw new IllegalArgumentException(
          "Invalid version format: " + currentVersion + " (expected format: X.Y.Z)"
      );
    }

    String[] parts = currentVersion.split("\\.");

    int major = Integer.parseInt(parts[0]);
    int minor = Integer.parseInt(parts[1]);
    int patch = Integer.parseInt(parts[2]);

    if (patch < MAX_PATCH) {
      patch++;
    } else {
      // patch == MAX_PATCH 이면 minor++ 하고 patch 초기화
      patch = 0;
      minor++;
    }

    return major + "." + minor + "." + patch;
  }
}
