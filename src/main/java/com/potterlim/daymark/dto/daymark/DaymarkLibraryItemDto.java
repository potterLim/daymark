package com.potterlim.daymark.dto.daymark;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

public final class DaymarkLibraryItemDto {

    private final LocalDate mDate;
    private final boolean mHasMorningEntry;
    private final boolean mHasEveningEntry;
    private final int mAchievedGoalCount;
    private final int mTotalGoalCount;
    private final int mCompletionPercent;
    private final String mExcerpt;
    private final String mMarkdownText;
    private final List<DaymarkLibraryGoalPreviewDto> mGoalPreviewItems;
    private final int mHiddenGoalCount;
    private final List<DaymarkLibraryContentBlockDto> mContentBlocks;

    private DaymarkLibraryItemDto(Builder builder) {
        mDate = builder.mDate;
        mHasMorningEntry = builder.mHasMorningEntry;
        mHasEveningEntry = builder.mHasEveningEntry;
        mAchievedGoalCount = builder.mAchievedGoalCount;
        mTotalGoalCount = builder.mTotalGoalCount;
        mCompletionPercent = builder.mCompletionPercent;
        mExcerpt = builder.mExcerpt;
        mMarkdownText = builder.mMarkdownText;
        mGoalPreviewItems = List.copyOf(builder.mGoalPreviewItems);
        mHiddenGoalCount = builder.mHiddenGoalCount;
        mContentBlocks = List.copyOf(builder.mContentBlocks);
    }

    public static Builder createBuilder(LocalDate date) {
        return new Builder(date);
    }

    public LocalDate getDate() {
        return mDate;
    }

    public boolean hasMorningEntry() {
        return mHasMorningEntry;
    }

    public boolean hasEveningEntry() {
        return mHasEveningEntry;
    }

    public int getAchievedGoalCount() {
        return mAchievedGoalCount;
    }

    public int getTotalGoalCount() {
        return mTotalGoalCount;
    }

    public int getCompletionPercent() {
        return mCompletionPercent;
    }

    public int getTrendHeightPercent() {
        if (mTotalGoalCount == 0) {
            return 10;
        }

        return Math.max(12, mCompletionPercent);
    }

    public String getExcerpt() {
        return mExcerpt;
    }

    public String getMarkdownText() {
        return mMarkdownText;
    }

    public List<DaymarkLibraryGoalPreviewDto> getGoalPreviewItems() {
        return mGoalPreviewItems;
    }

    public int getHiddenGoalCount() {
        return mHiddenGoalCount;
    }

    public List<DaymarkLibraryContentBlockDto> getContentBlocks() {
        return mContentBlocks;
    }

    public boolean hasGoalPreviewItems() {
        return !mGoalPreviewItems.isEmpty();
    }

    public boolean hasHiddenGoals() {
        return mHiddenGoalCount > 0;
    }

    public boolean hasContentBlocks() {
        return !mContentBlocks.isEmpty();
    }

    public boolean hasStructuredPreview() {
        return hasGoalPreviewItems() || hasContentBlocks();
    }

    public String getDayLabel() {
        DayOfWeek dayOfWeek = mDate.getDayOfWeek();
        return switch (dayOfWeek) {
            case MONDAY -> "월";
            case TUESDAY -> "화";
            case WEDNESDAY -> "수";
            case THURSDAY -> "목";
            case FRIDAY -> "금";
            case SATURDAY -> "토";
            case SUNDAY -> "일";
            default -> throw new IllegalStateException("Unexpected dayOfWeek: " + dayOfWeek);
        };
    }

    public String getFlowLabel() {
        if (mHasMorningEntry && mHasEveningEntry) {
            return "계획과 회고";
        }

        if (mHasMorningEntry) {
            return "아침 계획";
        }

        return "저녁 회고";
    }

    public static final class Builder {

        private final LocalDate mDate;
        private boolean mHasMorningEntry;
        private boolean mHasEveningEntry;
        private int mAchievedGoalCount;
        private int mTotalGoalCount;
        private int mCompletionPercent;
        private String mExcerpt = "";
        private String mMarkdownText = "";
        private List<DaymarkLibraryGoalPreviewDto> mGoalPreviewItems = List.of();
        private int mHiddenGoalCount;
        private List<DaymarkLibraryContentBlockDto> mContentBlocks = List.of();

        private Builder(LocalDate date) {
            if (date == null) {
                throw new IllegalArgumentException("date must not be null.");
            }

            mDate = date;
        }

        public Builder markMorningEntryPresent() {
            mHasMorningEntry = true;
            return this;
        }

        public Builder markEveningEntryPresent() {
            mHasEveningEntry = true;
            return this;
        }

        public Builder setAchievedGoalCount(int achievedGoalCount) {
            mAchievedGoalCount = requireNonNegative(achievedGoalCount, "achievedGoalCount");
            return this;
        }

        public Builder setTotalGoalCount(int totalGoalCount) {
            mTotalGoalCount = requireNonNegative(totalGoalCount, "totalGoalCount");
            return this;
        }

        public Builder setCompletionPercent(int completionPercent) {
            mCompletionPercent = requirePercent(completionPercent, "completionPercent");
            return this;
        }

        public Builder setExcerpt(String excerptOrNull) {
            mExcerpt = excerptOrNull == null ? "" : excerptOrNull;
            return this;
        }

        public Builder setMarkdownText(String markdownTextOrNull) {
            mMarkdownText = markdownTextOrNull == null ? "" : markdownTextOrNull;
            return this;
        }

        public Builder setGoalPreviewItems(List<DaymarkLibraryGoalPreviewDto> goalPreviewItems) {
            if (goalPreviewItems == null) {
                throw new IllegalArgumentException("goalPreviewItems must not be null.");
            }

            mGoalPreviewItems = List.copyOf(goalPreviewItems);
            return this;
        }

        public Builder setHiddenGoalCount(int hiddenGoalCount) {
            mHiddenGoalCount = requireNonNegative(hiddenGoalCount, "hiddenGoalCount");
            return this;
        }

        public Builder setContentBlocks(List<DaymarkLibraryContentBlockDto> contentBlocks) {
            if (contentBlocks == null) {
                throw new IllegalArgumentException("contentBlocks must not be null.");
            }

            mContentBlocks = List.copyOf(contentBlocks);
            return this;
        }

        public DaymarkLibraryItemDto build() {
            if (mAchievedGoalCount > mTotalGoalCount) {
                throw new IllegalStateException("achievedGoalCount must not be greater than totalGoalCount.");
            }

            if (mTotalGoalCount == 0 && mCompletionPercent != 0) {
                throw new IllegalStateException("completionPercent must be zero when totalGoalCount is zero.");
            }

            return new DaymarkLibraryItemDto(this);
        }

        private static int requireNonNegative(int value, String fieldName) {
            if (value < 0) {
                throw new IllegalArgumentException(fieldName + " must not be negative.");
            }

            return value;
        }

        private static int requirePercent(int value, String fieldName) {
            if (value < 0 || value > 100) {
                throw new IllegalArgumentException(fieldName + " must be between 0 and 100.");
            }

            return value;
        }
    }
}
