package kr.muroom.muroombackendbach.common.sms.presentation;

public interface SmsSender {

  void sendSms(String phone, String content);

}
