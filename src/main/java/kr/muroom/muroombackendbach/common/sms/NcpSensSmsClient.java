package kr.muroom.muroombackendbach.common.sms;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class NcpSensSmsClient implements SmsSender {

  private final RestTemplate restTemplate = new RestTemplate();

  @Value("${ncp.sens.access-key}")
  private String accessKey;

  @Value("${ncp.sens.secret-key}")
  private String secretKey;

  @Value("${ncp.sens.service-id}")
  private String serviceId;

  @Value("${ncp.sens.from}")
  private String from;

  @Override
  public void sendSms(String phone, String content) {
    String method = "POST";
    String uri = "/sms/v2/services/" + serviceId + "/messages";
    String timestamp = String.valueOf(Instant.now().toEpochMilli());

    String signature = NcpSensSignatureUtil.makeSignature(
        method,
        uri,
        timestamp,
        accessKey,
        secretKey
    );

    String url = "https://sens.apigw.ntruss.com" + uri;

    Map<String, Object> body = new HashMap<>();
    body.put("type", "SMS");
    body.put("contentType", "COMM");
    body.put("countryCode", "82");
    body.put("from", from);
    body.put("content", content);

    Map<String, String> msg = new HashMap<>();
    msg.put("to", phone);
    msg.put("content", content);

    body.put("messages", Collections.singletonList(msg));

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("x-ncp-apigw-timestamp", timestamp);
    headers.set("x-ncp-iam-access-key", accessKey);
    headers.set("x-ncp-apigw-signature-v2", signature);

    HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(body, headers);

    ResponseEntity<String> response =
        restTemplate.exchange(url, HttpMethod.POST, httpEntity, String.class);

    if (!response.getStatusCode().is2xxSuccessful()) {
      throw new RuntimeException("Failed to send SMS: " + response.getBody());
    }
  }
}
