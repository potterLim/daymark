package com.potterlim.daymark.dto.auth;

import com.potterlim.daymark.identity.EmailAddress;
import com.potterlim.daymark.identity.GoogleSubject;

public final class GoogleIdentityConnectionCommand {

    private final EmailAddress mEmailAddress;
    private final GoogleSubject mGoogleSubject;

    private GoogleIdentityConnectionCommand(EmailAddress emailAddress, GoogleSubject googleSubject) {
        if (emailAddress == null) {
            throw new IllegalArgumentException("emailAddress must not be null.");
        }

        if (googleSubject == null) {
            throw new IllegalArgumentException("googleSubject must not be null.");
        }

        mEmailAddress = emailAddress;
        mGoogleSubject = googleSubject;
    }

    public static GoogleIdentityConnectionCommand of(EmailAddress emailAddress, GoogleSubject googleSubject) {
        return new GoogleIdentityConnectionCommand(emailAddress, googleSubject);
    }

    public static GoogleIdentityConnectionCommand createFromRawInput(
        String emailAddress,
        String googleSubject
    ) {
        return new GoogleIdentityConnectionCommand(
            EmailAddress.create(emailAddress),
            GoogleSubject.create(googleSubject)
        );
    }

    public EmailAddress getEmailAddress() {
        return mEmailAddress;
    }

    public GoogleSubject getGoogleSubject() {
        return mGoogleSubject;
    }
}
