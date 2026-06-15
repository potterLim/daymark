package com.potterlim.daymark.dto.daymark;

import java.util.List;

public final class DaymarkLibraryContentBlockDto {

    private final String mEyebrow;
    private final String mTitle;
    private final List<String> mLines;
    private final int mHiddenLineCount;
    private final EDaymarkLibraryContentTone mContentTone;

    private DaymarkLibraryContentBlockDto(Builder builder) {
        mEyebrow = builder.mEyebrow;
        mTitle = builder.mTitle;
        mLines = List.copyOf(builder.mLines);
        mHiddenLineCount = builder.mHiddenLineCount;
        mContentTone = builder.mContentTone;
    }

    public static Builder createBuilder(EDaymarkLibraryContentTone contentTone) {
        return new Builder(contentTone);
    }

    public String getEyebrow() {
        return mEyebrow;
    }

    public String getTitle() {
        return mTitle;
    }

    public List<String> getLines() {
        return mLines;
    }

    public int getHiddenLineCount() {
        return mHiddenLineCount;
    }

    public boolean hasHiddenLines() {
        return mHiddenLineCount > 0;
    }

    public String getToneCssClass() {
        return mContentTone.getCssClassName();
    }

    public static final class Builder {

        private final EDaymarkLibraryContentTone mContentTone;
        private String mEyebrow = "";
        private String mTitle = "";
        private List<String> mLines = List.of();
        private int mHiddenLineCount;

        private Builder(EDaymarkLibraryContentTone contentTone) {
            if (contentTone == null) {
                throw new IllegalArgumentException("contentTone must not be null.");
            }

            mContentTone = contentTone;
        }

        public Builder setEyebrow(String eyebrowOrNull) {
            mEyebrow = eyebrowOrNull == null ? "" : eyebrowOrNull;
            return this;
        }

        public Builder setTitle(String titleOrNull) {
            mTitle = titleOrNull == null ? "" : titleOrNull;
            return this;
        }

        public Builder setLines(List<String> lines) {
            if (lines == null) {
                throw new IllegalArgumentException("lines must not be null.");
            }

            mLines = List.copyOf(lines);
            return this;
        }

        public Builder setHiddenLineCount(int hiddenLineCount) {
            if (hiddenLineCount < 0) {
                throw new IllegalArgumentException("hiddenLineCount must not be negative.");
            }

            mHiddenLineCount = hiddenLineCount;
            return this;
        }

        public DaymarkLibraryContentBlockDto build() {
            return new DaymarkLibraryContentBlockDto(this);
        }
    }
}
