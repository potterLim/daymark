package com.potterlim.daymark.dto.auth;

import com.potterlim.daymark.identity.EmailAddress;
import com.potterlim.daymark.identity.GoogleSubject;
import com.potterlim.daymark.identity.RawPassword;
import com.potterlim.daymark.identity.WorkspaceId;

public final class RegisterGoogleUserAccountCommand {

    private final WorkspaceId mWorkspaceId;
    private final EmailAddress mEmailAddress;
    private final GoogleSubject mGoogleSubject;
    private final RawPassword mRawPassword;

    private RegisterGoogleUserAccountCommand(
        WorkspaceId workspaceId,
        EmailAddress emailAddress,
        GoogleSubject googleSubject,
        RawPassword rawPassword
    ) {
        if (workspaceId == null) {
            throw new IllegalArgumentException("workspaceId must not be null.");
        }

        if (emailAddress == null) {
            throw new IllegalArgumentException("emailAddress must not be null.");
        }

        if (googleSubject == null) {
            throw new IllegalArgumentException("googleSubject must not be null.");
        }

        if (rawPassword == null) {
            throw new IllegalArgumentException("rawPassword must not be null.");
        }

        mWorkspaceId = workspaceId;
        mEmailAddress = emailAddress;
        mGoogleSubject = googleSubject;
        mRawPassword = rawPassword;
    }

    public static RegisterGoogleUserAccountCommand createFromRawInput(
        String workspaceId,
        String emailAddress,
        String googleSubject,
        String rawPassword
    ) {
        return new RegisterGoogleUserAccountCommand(
            WorkspaceId.create(workspaceId),
            EmailAddress.create(emailAddress),
            GoogleSubject.create(googleSubject),
            RawPassword.create(rawPassword)
        );
    }

    public WorkspaceId getWorkspaceId() {
        return mWorkspaceId;
    }

    public EmailAddress getEmailAddress() {
        return mEmailAddress;
    }

    public GoogleSubject getGoogleSubject() {
        return mGoogleSubject;
    }

    public RawPassword getRawPassword() {
        return mRawPassword;
    }
}
