package com.potterlim.daymark.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import com.potterlim.daymark.support.DaymarkEntryDate;
import com.potterlim.daymark.support.DaymarkGoalMarkdown;
import com.potterlim.daymark.support.DaymarkSectionText;
import com.potterlim.daymark.support.EDaymarkSectionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "daymark_entry",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_daymark_entry_user_account_id_entry_date",
        columnNames = {"user_account_id", "entry_date"}
    )
)
public class DaymarkEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long mId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_account_id", nullable = false)
    private UserAccount mUserAccount;

    @Column(name = "entry_date", nullable = false)
    private LocalDate mEntryDate;

    @Column(name = "has_morning_entry", nullable = false)
    private boolean mHasMorningEntry;

    @Column(name = "has_evening_entry", nullable = false)
    private boolean mHasEveningEntry;

    @Column(name = "goals_text", nullable = false, columnDefinition = "TEXT")
    private String mGoalsText;

    @Column(name = "focus_text", nullable = false, columnDefinition = "TEXT")
    private String mFocusText;

    @Column(name = "challenges_text", nullable = false, columnDefinition = "TEXT")
    private String mChallengesText;

    @Column(name = "evening_goals_text", nullable = false, columnDefinition = "TEXT")
    private String mEveningGoalsText;

    @Column(name = "achievements_text", nullable = false, columnDefinition = "TEXT")
    private String mAchievementsText;

    @Column(name = "improvements_text", nullable = false, columnDefinition = "TEXT")
    private String mImprovementsText;

    @Column(name = "gratitude_text", nullable = false, columnDefinition = "TEXT")
    private String mGratitudeText;

    @Column(name = "notes_text", nullable = false, columnDefinition = "TEXT")
    private String mNotesText;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime mCreatedAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime mUpdatedAt;

    protected DaymarkEntry() {
    }

    private DaymarkEntry(UserAccount userAccount, DaymarkEntryDate entryDate) {
        mUserAccount = userAccount;
        mEntryDate = entryDate.getValue();
        mHasMorningEntry = false;
        mHasEveningEntry = false;
        mGoalsText = "";
        mFocusText = "";
        mChallengesText = "";
        mEveningGoalsText = "";
        mAchievementsText = "";
        mImprovementsText = "";
        mGratitudeText = "";
        mNotesText = "";
    }

    public static DaymarkEntry create(UserAccount userAccount, DaymarkEntryDate entryDate) {
        if (userAccount == null) {
            throw new IllegalArgumentException("userAccount must not be null.");
        }

        if (entryDate == null) {
            throw new IllegalArgumentException("entryDate must not be null.");
        }

        return new DaymarkEntry(userAccount, entryDate);
    }

    public LocalDate getEntryDate() {
        return mEntryDate;
    }

    public UserAccountId getUserAccountId() {
        return mUserAccount.getUserAccountId();
    }

    public UserAccount getUserAccount() {
        return mUserAccount;
    }

    public boolean hasMorningEntry() {
        return hasMorningContent();
    }

    public boolean hasEveningEntry() {
        return hasEveningContent();
    }

    public boolean hasAnyEntryContent() {
        return hasMorningEntry() || hasEveningEntry();
    }

    public String readSection(EDaymarkSectionType daymarkSectionType) {
        if (daymarkSectionType == null) {
            throw new IllegalArgumentException("daymarkSectionType must not be null.");
        }

        return switch (daymarkSectionType) {
            case GOALS -> mGoalsText;
            case FOCUS -> mFocusText;
            case CHALLENGES -> mChallengesText;
            case EVENING_GOALS -> mEveningGoalsText;
            case ACHIEVEMENTS -> mAchievementsText;
            case IMPROVEMENTS -> mImprovementsText;
            case GRATITUDE -> mGratitudeText;
            case NOTES -> mNotesText;
            default -> throw new IllegalStateException("Unexpected daymarkSectionType: " + daymarkSectionType);
        };
    }

    public void writeSection(EDaymarkSectionType daymarkSectionType, DaymarkSectionText normalizedBody) {
        if (daymarkSectionType == null) {
            throw new IllegalArgumentException("daymarkSectionType must not be null.");
        }

        if (normalizedBody == null) {
            throw new IllegalArgumentException("normalizedBody must not be null.");
        }

        String safeBody = normalizedBody.getValue();
        switch (daymarkSectionType) {
            case GOALS -> {
                mGoalsText = safeBody;
            }
            case FOCUS -> {
                mFocusText = safeBody;
            }
            case CHALLENGES -> {
                mChallengesText = safeBody;
            }
            case EVENING_GOALS -> {
                mEveningGoalsText = safeBody;
            }
            case ACHIEVEMENTS -> {
                mAchievementsText = safeBody;
            }
            case IMPROVEMENTS -> {
                mImprovementsText = safeBody;
            }
            case GRATITUDE -> {
                mGratitudeText = safeBody;
            }
            case NOTES -> {
                mNotesText = safeBody;
            }
            default -> {
                throw new IllegalStateException("Unexpected daymarkSectionType: " + daymarkSectionType);
            }
        }

        refreshEntryPresenceFlags();
    }

    public List<String> readCheckedGoalTexts() {
        return DaymarkGoalMarkdown.readCheckedGoalTexts(mEveningGoalsText);
    }

    @PrePersist
    public void handleBeforePersist() {
        LocalDateTime now = LocalDateTime.now();
        mCreatedAt = now;
        mUpdatedAt = now;
    }

    @PreUpdate
    public void handleBeforeUpdate() {
        mUpdatedAt = LocalDateTime.now();
    }

    private void refreshEntryPresenceFlags() {
        mHasMorningEntry = hasMorningContent();
        mHasEveningEntry = hasEveningContent();
    }

    private boolean hasMorningContent() {
        return hasText(mGoalsText) || hasText(mFocusText) || hasText(mChallengesText);
    }

    private boolean hasEveningContent() {
        return hasText(mEveningGoalsText)
            || hasText(mAchievementsText)
            || hasText(mImprovementsText)
            || hasText(mGratitudeText)
            || hasText(mNotesText);
    }

    private static boolean hasText(String textOrNull) {
        return textOrNull != null && !textOrNull.isBlank();
    }
}
