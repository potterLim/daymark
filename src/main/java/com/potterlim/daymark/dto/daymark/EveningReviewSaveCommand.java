package com.potterlim.daymark.dto.daymark;

import java.time.LocalDate;
import java.util.List;
import com.potterlim.daymark.entity.UserAccountId;
import com.potterlim.daymark.support.DaymarkEntryDate;
import com.potterlim.daymark.support.DaymarkGoalCheckItem;
import com.potterlim.daymark.support.DaymarkSectionText;

public final class EveningReviewSaveCommand {

    private final DaymarkEntryDate mEntryDate;
    private final UserAccountId mUserAccountId;
    private final List<DaymarkGoalCheckItem> mGoals;
    private final DaymarkSectionText mAchievements;
    private final DaymarkSectionText mImprovements;
    private final DaymarkSectionText mGratitude;
    private final DaymarkSectionText mNotes;

    private EveningReviewSaveCommand(
        DaymarkEntryDate entryDate,
        UserAccountId userAccountId,
        List<DaymarkGoalCheckItem> goalsOrNull,
        DaymarkSectionText achievements,
        DaymarkSectionText improvements,
        DaymarkSectionText gratitude,
        DaymarkSectionText notes
    ) {
        if (entryDate == null) {
            throw new IllegalArgumentException("entryDate must not be null.");
        }

        if (userAccountId == null) {
            throw new IllegalArgumentException("userAccountId must not be null.");
        }

        if (achievements == null) {
            throw new IllegalArgumentException("achievements must not be null.");
        }

        if (improvements == null) {
            throw new IllegalArgumentException("improvements must not be null.");
        }

        if (gratitude == null) {
            throw new IllegalArgumentException("gratitude must not be null.");
        }

        if (notes == null) {
            throw new IllegalArgumentException("notes must not be null.");
        }

        mEntryDate = entryDate;
        mUserAccountId = userAccountId;
        mGoals = copyGoalItems(goalsOrNull);
        mAchievements = achievements;
        mImprovements = improvements;
        mGratitude = gratitude;
        mNotes = notes;
    }

    public static EveningReviewSaveCommand createFromRawInput(
        LocalDate date,
        UserAccountId userAccountId,
        List<DaymarkGoalCheckItem> goalsOrNull,
        String achievementsOrNull,
        String improvementsOrNull,
        String gratitudeOrNull,
        String notesOrNull
    ) {
        return new EveningReviewSaveCommand(
            DaymarkEntryDate.of(date),
            userAccountId,
            goalsOrNull,
            DaymarkSectionText.create(achievementsOrNull),
            DaymarkSectionText.create(improvementsOrNull),
            DaymarkSectionText.create(gratitudeOrNull),
            DaymarkSectionText.create(notesOrNull)
        );
    }

    public DaymarkEntryDate getEntryDate() {
        return mEntryDate;
    }

    public UserAccountId getUserAccountId() {
        return mUserAccountId;
    }

    public List<DaymarkGoalCheckItem> getGoals() {
        return mGoals;
    }

    public DaymarkSectionText getAchievements() {
        return mAchievements;
    }

    public DaymarkSectionText getImprovements() {
        return mImprovements;
    }

    public DaymarkSectionText getGratitude() {
        return mGratitude;
    }

    public DaymarkSectionText getNotes() {
        return mNotes;
    }

    private static List<DaymarkGoalCheckItem> copyGoalItems(List<DaymarkGoalCheckItem> goalItemsOrNull) {
        if (goalItemsOrNull == null || goalItemsOrNull.isEmpty()) {
            return List.of();
        }

        return List.copyOf(goalItemsOrNull);
    }
}
