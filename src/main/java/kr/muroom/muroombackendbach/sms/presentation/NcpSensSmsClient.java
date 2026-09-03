package kr.muroom.muroombackendbach.sms.presentation;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import kr.muroom.muroombackendbach.common.util.NcpSensSignatureUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class NcpSensSmsClient implements SmsSender {

  private final RestTemplate restTemplate;

  private final String accessKey;
  private final String secretKey;
  private final String serviceId;
  private final String from;

  public NcpSensSmsClient(
      RestTemplateBuilder restTemplateBuilder,
      @Value("${ncp.sens.access-key}") String accessKey,
      @Value("${ncp.sens.secret-key}") String secretKey,
      @Value("${ncp.sens.service-id}") String serviceId,
      @Value("${ncp.sens.from}") String from
  ) {
    this.restTemplate = restTemplateBuilder.build();
    this.accessKey = accessKey;
    this.secretKey = secretKey;
    this.serviceId = serviceId;
    this.from = from;
  }

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
