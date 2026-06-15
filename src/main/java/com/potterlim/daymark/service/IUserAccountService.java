package com.potterlim.daymark.service;

import java.util.Optional;
import com.potterlim.daymark.dto.auth.GoogleIdentityConnectionCommand;
import com.potterlim.daymark.dto.auth.PasswordChangeCommand;
import com.potterlim.daymark.dto.auth.RegisterGoogleUserAccountCommand;
import com.potterlim.daymark.dto.auth.RegisterUserAccountCommand;
import com.potterlim.daymark.entity.UserAccount;
import com.potterlim.daymark.identity.EmailAddress;
import com.potterlim.daymark.identity.GoogleSubject;
import com.potterlim.daymark.identity.LoginIdentifier;

public interface IUserAccountService {

    /**
     * Registers a new user account.
     *
     * <p>Preconditions: the command must contain a workspace id, email address, and raw password.
     * The workspace id and email address must not already exist.</p>
     *
     * @return The persisted user account.
     */
    UserAccount registerUserAccount(RegisterUserAccountCommand registerUserAccountCommand);

    /**
     * Registers a new user account after Google has confirmed the email identity.
     *
     * <p>Preconditions: the command must contain a workspace id, email address, Google subject, and
     * raw password. The workspace id, email address, and Google subject must not already exist.</p>
     *
     * @return The persisted Google-connected user account.
     */
    UserAccount registerGoogleUserAccount(RegisterGoogleUserAccountCommand registerGoogleUserAccountCommand);

    /**
     * Finds a user account by login identifier.
     *
     * <p>Preconditions: the login identifier may be null. In that case, the method returns an
     * empty result instead of querying the database.</p>
     *
     * @return The matching user account when it exists, otherwise an empty result.
     */
    Optional<UserAccount> findUserAccountByLoginIdentifier(LoginIdentifier loginIdentifierOrNull);

    /**
     * Finds a user account by email address.
     *
     * <p>Preconditions: the email address may be null. In that case, the method returns
     * an empty result instead of querying the database.</p>
     *
     * @return The matching user account when it exists, otherwise an empty result.
     */
    Optional<UserAccount> findUserAccountByEmailAddress(EmailAddress emailAddressOrNull);

    /**
     * Finds a user account by Google subject.
     *
     * <p>Preconditions: the Google subject may be null. In that case, the method returns an
     * empty result instead of querying the database.</p>
     *
     * @return The matching user account when it exists, otherwise an empty result.
     */
    Optional<UserAccount> findUserAccountByGoogleSubject(GoogleSubject googleSubjectOrNull);

    /**
     * Connects a verified Google identity to the account with the same email address.
     *
     * <p>Preconditions: the command must contain an email address and Google subject. If the email
     * address does not belong to an account, the method returns an empty result.</p>
     *
     * @return The connected account when one exists, otherwise an empty result.
     */
    Optional<UserAccount> connectGoogleIdentity(GoogleIdentityConnectionCommand googleIdentityConnectionCommand);

    /**
     * Changes the password for the authenticated user after verifying the current password.
     *
     * <p>Preconditions: the command must contain the user account id, current password, and new
     * password.</p>
     */
    void changePassword(PasswordChangeCommand passwordChangeCommand);

}
