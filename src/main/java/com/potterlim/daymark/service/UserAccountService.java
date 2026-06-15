package com.potterlim.daymark.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;
import com.potterlim.daymark.dto.auth.GoogleIdentityConnectionCommand;
import com.potterlim.daymark.dto.auth.PasswordChangeCommand;
import com.potterlim.daymark.dto.auth.RegisterGoogleUserAccountCommand;
import com.potterlim.daymark.dto.auth.RegisterUserAccountCommand;
import com.potterlim.daymark.entity.UserAccount;
import com.potterlim.daymark.entity.UserAccountId;
import com.potterlim.daymark.identity.EmailAddress;
import com.potterlim.daymark.identity.GoogleSubject;
import com.potterlim.daymark.identity.LoginIdentifier;
import com.potterlim.daymark.identity.PasswordHash;
import com.potterlim.daymark.identity.RawPassword;
import com.potterlim.daymark.identity.WorkspaceId;
import com.potterlim.daymark.repository.IUserAccountRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAccountService implements IUserAccountService {

    private final IUserAccountRepository mUserAccountRepository;
    private final PasswordEncoder mPasswordEncoder;
    private final AdministratorWorkspaceIdPolicy mAdministratorWorkspaceIdPolicy;
    private final Clock mClock;

    public UserAccountService(
        IUserAccountRepository userAccountRepository,
        PasswordEncoder passwordEncoder,
        AdministratorWorkspaceIdPolicy administratorWorkspaceIdPolicy,
        Clock clock
    ) {
        mUserAccountRepository = userAccountRepository;
        mPasswordEncoder = passwordEncoder;
        mAdministratorWorkspaceIdPolicy = administratorWorkspaceIdPolicy;
        mClock = clock;
    }

    @Override
    @Transactional
    public UserAccount registerUserAccount(RegisterUserAccountCommand registerUserAccountCommand) {
        validateRegisterUserAccountCommand(registerUserAccountCommand);

        WorkspaceId workspaceId = registerUserAccountCommand.getWorkspaceId();
        EmailAddress emailAddress = registerUserAccountCommand.getEmailAddress();
        RawPassword rawPassword = registerUserAccountCommand.getRawPassword();

        if (mUserAccountRepository.findByUserName(workspaceId.getValue()).isPresent()) {
            throw new DuplicateUserNameException(workspaceId);
        }

        if (mUserAccountRepository.findByEmailAddress(emailAddress.getValue()).isPresent()) {
            throw new DuplicateEmailException(emailAddress);
        }

        UserAccount userAccount = UserAccount.createRegularUser(
            workspaceId,
            emailAddress,
            PasswordHash.create(mPasswordEncoder.encode(rawPassword.getValue()))
        );
        grantAdministratorRoleIfConfigured(userAccount);

        try {
            return mUserAccountRepository.save(userAccount);
        } catch (DataIntegrityViolationException dataIntegrityViolationException) {
            throw resolveDuplicateRegistrationException(
                workspaceId,
                emailAddress,
                dataIntegrityViolationException
            );
        }
    }

    @Override
    @Transactional
    public UserAccount registerGoogleUserAccount(RegisterGoogleUserAccountCommand registerGoogleUserAccountCommand) {
        validateRegisterGoogleUserAccountCommand(registerGoogleUserAccountCommand);

        WorkspaceId workspaceId = registerGoogleUserAccountCommand.getWorkspaceId();
        EmailAddress emailAddress = registerGoogleUserAccountCommand.getEmailAddress();
        GoogleSubject googleSubject = registerGoogleUserAccountCommand.getGoogleSubject();
        RawPassword rawPassword = registerGoogleUserAccountCommand.getRawPassword();

        if (mUserAccountRepository.findByUserName(workspaceId.getValue()).isPresent()) {
            throw new DuplicateUserNameException(workspaceId);
        }

        if (mUserAccountRepository.findByEmailAddress(emailAddress.getValue()).isPresent()) {
            throw new DuplicateEmailException(emailAddress);
        }

        if (mUserAccountRepository.findByGoogleSubject(googleSubject.getValue()).isPresent()) {
            throw new DuplicateEmailException(emailAddress);
        }

        UserAccount userAccount = UserAccount.createGoogleVerifiedUser(
            workspaceId,
            emailAddress,
            PasswordHash.create(mPasswordEncoder.encode(rawPassword.getValue())),
            googleSubject,
            LocalDateTime.now(mClock)
        );
        grantAdministratorRoleIfConfigured(userAccount);

        try {
            return mUserAccountRepository.save(userAccount);
        } catch (DataIntegrityViolationException dataIntegrityViolationException) {
            throw resolveDuplicateRegistrationException(
                workspaceId,
                emailAddress,
                dataIntegrityViolationException
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserAccount> findUserAccountByLoginIdentifier(LoginIdentifier loginIdentifierOrNull) {
        if (loginIdentifierOrNull == null) {
            return Optional.empty();
        }

        return mUserAccountRepository.findByLoginIdentifier(loginIdentifierOrNull.getValue());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserAccount> findUserAccountByEmailAddress(EmailAddress emailAddressOrNull) {
        if (emailAddressOrNull == null) {
            return Optional.empty();
        }

        return mUserAccountRepository.findByEmailAddress(emailAddressOrNull.getValue());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserAccount> findUserAccountByGoogleSubject(GoogleSubject googleSubjectOrNull) {
        if (googleSubjectOrNull == null) {
            return Optional.empty();
        }

        return mUserAccountRepository.findByGoogleSubject(googleSubjectOrNull.getValue());
    }

    @Override
    @Transactional
    public Optional<UserAccount> connectGoogleIdentity(GoogleIdentityConnectionCommand googleIdentityConnectionCommand) {
        if (googleIdentityConnectionCommand == null) {
            return Optional.empty();
        }

        EmailAddress emailAddress = googleIdentityConnectionCommand.getEmailAddress();
        GoogleSubject googleSubject = googleIdentityConnectionCommand.getGoogleSubject();
        Optional<UserAccount> existingGoogleUserAccountOrEmpty =
            mUserAccountRepository.findByGoogleSubject(googleSubject.getValue());
        if (existingGoogleUserAccountOrEmpty.isPresent()) {
            return existingGoogleUserAccountOrEmpty;
        }

        return mUserAccountRepository.findByEmailAddress(emailAddress.getValue())
            .map(userAccount -> {
                userAccount.connectGoogleIdentity(googleSubject, LocalDateTime.now(mClock));
                return userAccount;
            });
    }

    @Override
    @Transactional
    public void changePassword(PasswordChangeCommand passwordChangeCommand) {
        validatePasswordChangeCommand(passwordChangeCommand);

        UserAccount userAccount = getRequiredUserAccount(passwordChangeCommand.getUserAccountId());
        if (!mPasswordEncoder.matches(passwordChangeCommand.getCurrentRawPassword().getValue(), userAccount.getPassword())) {
            throw new InvalidCurrentPasswordException();
        }

        applyNewPassword(userAccount, passwordChangeCommand.getNewRawPassword());
    }

    private static void validateRegisterUserAccountCommand(RegisterUserAccountCommand registerUserAccountCommand) {
        if (registerUserAccountCommand == null) {
            throw new IllegalArgumentException("registerUserAccountCommand must not be null.");
        }

        if (registerUserAccountCommand.getWorkspaceId() == null) {
            throw new IllegalArgumentException("workspaceId must not be null.");
        }

        if (registerUserAccountCommand.getEmailAddress() == null) {
            throw new IllegalArgumentException("emailAddress must not be null.");
        }

        validateRawPassword(registerUserAccountCommand.getRawPassword());
    }

    private static void validateRegisterGoogleUserAccountCommand(
        RegisterGoogleUserAccountCommand registerGoogleUserAccountCommand
    ) {
        if (registerGoogleUserAccountCommand == null) {
            throw new IllegalArgumentException("registerGoogleUserAccountCommand must not be null.");
        }

        if (registerGoogleUserAccountCommand.getWorkspaceId() == null) {
            throw new IllegalArgumentException("workspaceId must not be null.");
        }

        if (registerGoogleUserAccountCommand.getEmailAddress() == null) {
            throw new IllegalArgumentException("emailAddress must not be null.");
        }

        if (registerGoogleUserAccountCommand.getGoogleSubject() == null) {
            throw new IllegalArgumentException("googleSubject must not be null.");
        }

        validateRawPassword(registerGoogleUserAccountCommand.getRawPassword());
    }

    private static void validatePasswordChangeCommand(PasswordChangeCommand passwordChangeCommand) {
        if (passwordChangeCommand == null) {
            throw new IllegalArgumentException("passwordChangeCommand must not be null.");
        }

        validateRawPassword(passwordChangeCommand.getCurrentRawPassword());
        validateRawPassword(passwordChangeCommand.getNewRawPassword());
    }

    private static void validateRawPassword(RawPassword rawPassword) {
        if (rawPassword == null) {
            throw new IllegalArgumentException("rawPassword must not be null.");
        }
    }

    private UserAccount getRequiredUserAccount(UserAccountId userAccountId) {
        return mUserAccountRepository.findById(userAccountId.getValue())
            .orElseThrow(() -> new IllegalStateException("User account not found."));
    }

    private void applyNewPassword(UserAccount userAccount, RawPassword newRawPassword) {
        validateRawPassword(newRawPassword);
        userAccount.changePasswordHash(PasswordHash.create(mPasswordEncoder.encode(newRawPassword.getValue())));
    }

    private void grantAdministratorRoleIfConfigured(UserAccount userAccount) {
        if (mAdministratorWorkspaceIdPolicy.isAdministratorWorkspaceId(userAccount.getWorkspaceId())) {
            userAccount.grantAdministratorRole();
        }
    }

    private RuntimeException resolveDuplicateRegistrationException(
        WorkspaceId workspaceId,
        EmailAddress emailAddress,
        DataIntegrityViolationException dataIntegrityViolationException
    ) {
        if (mUserAccountRepository.findByUserName(workspaceId.getValue()).isPresent()) {
            return new DuplicateUserNameException(workspaceId);
        }

        if (mUserAccountRepository.findByEmailAddress(emailAddress.getValue()).isPresent()) {
            return new DuplicateEmailException(emailAddress);
        }

        return dataIntegrityViolationException;
    }
}
