package com.potterlim.daymark.dto.auth;

import com.potterlim.daymark.entity.UserAccountId;
import com.potterlim.daymark.identity.RawPassword;

public final class PasswordChangeCommand {

    private final UserAccountId mUserAccountId;
    private final RawPassword mCurrentRawPassword;
    private final RawPassword mNewRawPassword;

    private PasswordChangeCommand(
        UserAccountId userAccountId,
        RawPassword currentRawPassword,
        RawPassword newRawPassword
    ) {
        if (userAccountId == null) {
            throw new IllegalArgumentException("userAccountId must not be null.");
        }

        if (currentRawPassword == null) {
            throw new IllegalArgumentException("currentRawPassword must not be null.");
        }

        if (newRawPassword == null) {
            throw new IllegalArgumentException("newRawPassword must not be null.");
        }

        mUserAccountId = userAccountId;
        mCurrentRawPassword = currentRawPassword;
        mNewRawPassword = newRawPassword;
    }

    public static PasswordChangeCommand createFromRawInput(
        UserAccountId userAccountId,
        String currentRawPassword,
        String newRawPassword
    ) {
        return new PasswordChangeCommand(
            userAccountId,
            RawPassword.create(currentRawPassword),
            RawPassword.create(newRawPassword)
        );
    }

    public UserAccountId getUserAccountId() {
        return mUserAccountId;
    }

    public RawPassword getCurrentRawPassword() {
        return mCurrentRawPassword;
    }

    public RawPassword getNewRawPassword() {
        return mNewRawPassword;
    }
}
