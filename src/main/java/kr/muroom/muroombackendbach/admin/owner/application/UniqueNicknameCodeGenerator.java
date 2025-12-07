package kr.muroom.muroombackendbach.admin.owner.application;

import org.springframework.stereotype.Component;

@Component
public class UniqueNicknameCodeGenerator {

  // 1000 * 1000 = 1,000,000 (6자리 커버 가능)
  private static final long DOMAIN = 1000;

  // 라운드 수 (4 이상 권장, 숫자를 충분히 섞기 위함)
  private static final int ROUNDS = 4;

  // 비밀 키 (이 배열의 값을 바꾸면 생성되는 숫자 패턴이 완전히 달라집니다)
  // 서비스 런칭 후에는 절대 변경하면 안 됩니다!
  private static final int[] KEYS = {123, 456, 789, 101};

  /**
   * DB의 Sequence ID (0 ~ 999,999)를 입력받아 중복 없는 랜덤한 6자리 숫자 문자열을 반환합니다.
   */
  public String generate(long seqId) {
    if (seqId < 0 || seqId >= 1_000_000) {
      throw new IllegalArgumentException("ID must be between 0 and 999,999");
    }

    // 1. 숫자를 좌우로 쪼갭니다 (예: 123456 -> L:123, R:456)
    long left = seqId / DOMAIN;
    long right = seqId % DOMAIN;

    // 2. Feistel 네트워크 (4라운드 수행)
    for (int i = 0; i < ROUNDS; i++) {
      long newRight = (left + roundFunction(right, KEYS[i])) % DOMAIN;
      left = right;
      right = newRight;
    }

    // 3. 다시 합쳐서 6자리 문자열로 반환 (빈 자리는 0으로 채움)
    return String.format("사장님%06d", left * DOMAIN + right);
  }

  /**
   * 라운드 함수 (단순하지만 불규칙한 결과를 내는 함수) 입력값과 키를 섞어서 예측 불가능한 값을 만듭니다.
   */
  private long roundFunction(long val, int key) {
    // 소수를 곱하고 키를 더해 나머지 연산 (단순 해시)
    long prime = 137;
    return (val * prime + key) % DOMAIN;
  }
}