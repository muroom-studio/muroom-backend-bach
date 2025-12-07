package kr.muroom.muroombackendbach.common.sms;

import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class NcpSensSignatureUtil {

  public static String makeSignature(
      String method,
      String uri,
      String timestamp,
      String accessKey,
      String secretKey
  ) {
    try {
      String space = " ";
      String newLine = "\n";

      String message = method + space + uri + newLine + timestamp + newLine + accessKey;

      SecretKeySpec signingKey =
          new SecretKeySpec(secretKey.getBytes("UTF-8"), "HmacSHA256");
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(signingKey);

      byte[] rawHmac = mac.doFinal(message.getBytes("UTF-8"));

      return Base64.getEncoder().encodeToString(rawHmac);
    } catch (Exception e) {
      throw new RuntimeException("Failed to make NCP SENS signature", e);
    }
  }
}
