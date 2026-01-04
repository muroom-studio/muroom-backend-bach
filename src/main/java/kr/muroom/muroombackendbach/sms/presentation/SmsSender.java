package kr.muroom.muroombackendbach.sms.presentation;

public interface SmsSender {

  void sendSms(String phone, String content);

}
