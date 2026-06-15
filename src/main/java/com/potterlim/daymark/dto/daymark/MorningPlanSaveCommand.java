package com.potterlim.daymark.dto.daymark;

import java.time.LocalDate;
import com.potterlim.daymark.entity.UserAccountId;
import com.potterlim.daymark.support.DaymarkEntryDate;
import com.potterlim.daymark.support.DaymarkSectionText;

public final class MorningPlanSaveCommand {

    private final DaymarkEntryDate mEntryDate;
    private final UserAccountId mUserAccountId;
    private final DaymarkSectionText mGoals;
    private final DaymarkSectionText mFocus;
    private final DaymarkSectionText mChallenges;

    private MorningPlanSaveCommand(
        DaymarkEntryDate entryDate,
        UserAccountId userAccountId,
        DaymarkSectionText goals,
        DaymarkSectionText focus,
        DaymarkSectionText challenges
    ) {
        if (entryDate == null) {
            throw new IllegalArgumentException("entryDate must not be null.");
        }

        if (userAccountId == null) {
            throw new IllegalArgumentException("userAccountId must not be null.");
        }

        if (goals == null) {
            throw new IllegalArgumentException("goals must not be null.");
        }

        if (focus == null) {
            throw new IllegalArgumentException("focus must not be null.");
        }

        if (challenges == null) {
            throw new IllegalArgumentException("challenges must not be null.");
        }

        mEntryDate = entryDate;
        mUserAccountId = userAccountId;
        mGoals = goals;
        mFocus = focus;
        mChallenges = challenges;
    }

    public static MorningPlanSaveCommand createFromRawInput(
        LocalDate date,
        UserAccountId userAccountId,
        String goalsOrNull,
        String focusOrNull,
        String challengesOrNull
    ) {
        return new MorningPlanSaveCommand(
            DaymarkEntryDate.of(date),
            userAccountId,
            DaymarkSectionText.create(goalsOrNull),
            DaymarkSectionText.create(focusOrNull),
            DaymarkSectionText.create(challengesOrNull)
        );
    }

    public DaymarkEntryDate getEntryDate() {
        return mEntryDate;
    }

    public UserAccountId getUserAccountId() {
        return mUserAccountId;
    }

    public DaymarkSectionText getGoals() {
        return mGoals;
    }

    public DaymarkSectionText getFocus() {
        return mFocus;
    }

    public DaymarkSectionText getChallenges() {
        return mChallenges;
    }
}
