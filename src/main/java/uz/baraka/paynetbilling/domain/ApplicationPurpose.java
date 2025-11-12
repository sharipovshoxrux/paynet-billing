package uz.baraka.paynetbilling.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum ApplicationPurpose {
    ISSUE, RE_ISSUE, ISSUE_AND_PRINT;

    @JsonCreator
    public static ApplicationPurpose from(String v) {
        if (v == null) throw new IllegalArgumentException("purpose is required");
        return ApplicationPurpose.valueOf(v.trim().replace('-', '_').toUpperCase());
    }
}
