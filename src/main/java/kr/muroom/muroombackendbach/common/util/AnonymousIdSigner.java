package kr.muroom.muroombackendbach.common.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public final class AnonymousIdSigner {

    private static final String ALG = "HmacSHA256";
    private final byte[] secret;

    public AnonymousIdSigner(String secret) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
    }

    public String sign(String anonId) {
        try {
            Mac mac = Mac.getInstance(ALG);
            mac.init(new SecretKeySpec(secret, ALG));
            byte[] raw = mac.doFinal(anonId.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to sign anonymous id", e);
        }
    }

    public boolean verify(String anonId, String sig) {
        if (anonId == null || sig == null) return false;
        String expected = sign(anonId);
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                sig.getBytes(StandardCharsets.UTF_8)
        );
    }
}