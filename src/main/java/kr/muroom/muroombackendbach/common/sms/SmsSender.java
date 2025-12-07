package kr.muroom.muroombackendbach.common.sms;

public interface SmsSender {

  void sendSms(String phone, String content);

}
