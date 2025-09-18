package uz.baraka.paynetbilling.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum ApplicationSource {
    BANK, INSON, APP;

    @JsonCreator
    public static ApplicationSource from(String v) {
        if (v == null) throw new IllegalArgumentException("source is required");
        return ApplicationSource.valueOf(v.trim().replace('-', '_').toUpperCase());
    }
}
