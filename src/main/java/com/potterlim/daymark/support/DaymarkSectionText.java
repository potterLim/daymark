package com.potterlim.daymark.support;

public final class DaymarkSectionText {

    private final String mValue;

    private DaymarkSectionText(String value) {
        mValue = value;
    }

    public static DaymarkSectionText empty() {
        return new DaymarkSectionText("");
    }

    public static DaymarkSectionText create(String valueOrNull) {
        if (valueOrNull == null) {
            return empty();
        }

        return new DaymarkSectionText(valueOrNull);
    }

    public DaymarkSectionText normalizeSectionBody() {
        return DaymarkSectionText.create(DaymarkTextLines.normalizeSectionBody(mValue));
    }

    public boolean isBlank() {
        return mValue.isBlank();
    }

    public String getValue() {
        return mValue;
    }
}
