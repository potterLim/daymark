package com.potterlim.daymark.dto.auth;

import com.potterlim.daymark.identity.EmailAddress;
import com.potterlim.daymark.identity.RawPassword;
import com.potterlim.daymark.identity.WorkspaceId;

public final class RegisterUserAccountCommand {

    private final WorkspaceId mWorkspaceId;
    private final EmailAddress mEmailAddress;
    private final RawPassword mRawPassword;

    private RegisterUserAccountCommand(WorkspaceId workspaceId, EmailAddress emailAddress, RawPassword rawPassword) {
        if (workspaceId == null) {
            throw new IllegalArgumentException("workspaceId must not be null.");
        }

        if (emailAddress == null) {
            throw new IllegalArgumentException("emailAddress must not be null.");
        }

        if (rawPassword == null) {
            throw new IllegalArgumentException("rawPassword must not be null.");
        }

        mWorkspaceId = workspaceId;
        mEmailAddress = emailAddress;
        mRawPassword = rawPassword;
    }

    public static RegisterUserAccountCommand createFromRawInput(
        String workspaceId,
        String emailAddress,
        String rawPassword
    ) {
        return new RegisterUserAccountCommand(
            WorkspaceId.create(workspaceId),
            EmailAddress.create(emailAddress),
            RawPassword.create(rawPassword)
        );
    }

    public WorkspaceId getWorkspaceId() {
        return mWorkspaceId;
    }

    public EmailAddress getEmailAddress() {
        return mEmailAddress;
    }

    public RawPassword getRawPassword() {
        return mRawPassword;
    }
}
