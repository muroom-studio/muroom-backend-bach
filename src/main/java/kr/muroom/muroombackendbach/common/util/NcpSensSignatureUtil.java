package kr.muroom.muroombackendbach.common.util;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class NcpSensSignatureUtil {

  private static final String HMAC_SHA256 = "HmacSHA256";

  public static String makeSignature(
      String method,
      String uri,
      String timestamp,
      String accessKey,
      String secretKey
  ) {
    String space = " ";
    String newLine = "\n";

    String message = method + space + uri + newLine + timestamp + newLine + accessKey;

    try {
      SecretKeySpec signingKey =
          new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);

      Mac mac = Mac.getInstance(HMAC_SHA256);
      mac.init(signingKey);

      byte[] rawHmac = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));

      return Base64.getEncoder().encodeToString(rawHmac);

    } catch (NoSuchAlgorithmException | InvalidKeyException e) {
      throw new IllegalStateException("Failed to create HMAC-SHA256 signature", e);
    }
  }
}
