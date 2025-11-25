package kr.muroom.muroombackendbach.terms.domain.entity;

import lombok.Getter;

@Getter
public enum TermsType {
    TERMS_OF_USE("이용약관", true),
    PRIVACY_COLLECTION("개인정보 수집", true),
    PRIVACY_PROCESSING("개인정보 처리", true),
    MARKETING_RECEIVE("마케팅 수신", false);

    private final String description;
    private final boolean required;

    TermsType(String description, boolean required) {
        this.description = description;
        this.required = required;
    }
}