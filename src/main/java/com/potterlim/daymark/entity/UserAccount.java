package com.potterlim.daymark.entity;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import com.potterlim.daymark.identity.EmailAddress;
import com.potterlim.daymark.identity.GoogleSubject;
import com.potterlim.daymark.identity.PasswordHash;
import com.potterlim.daymark.identity.WorkspaceId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Table(name = "user_account")
public class UserAccount implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long mId;

    @Column(name = "user_name", nullable = false, unique = true, length = 100)
    private String mUserName;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String mEmailAddress;

    @Column(name = "password_hash", nullable = false)
    private String mPasswordHash;

    @Column(name = "email_verified_at")
    private LocalDateTime mEmailVerifiedAt;

    @Column(name = "google_subject", unique = true, length = 255)
    private String mGoogleSubject;

    @Column(name = "google_connected_at")
    private LocalDateTime mGoogleConnectedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false, length = 30)
    private EUserRole mUserRole;

    @Column(name = "enabled", nullable = false)
    private boolean mIsEnabled;

    @Column(name = "locked", nullable = false)
    private boolean mIsLocked;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime mCreatedAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime mUpdatedAt;

    protected UserAccount() {
    }

    private UserAccount(WorkspaceId workspaceId, EmailAddress emailAddress, PasswordHash passwordHash, EUserRole userRole) {
        mUserName = workspaceId.getValue();
        mEmailAddress = emailAddress.getValue();
        mPasswordHash = passwordHash.getValue();
        mUserRole = userRole;
        mIsEnabled = true;
        mIsLocked = false;
    }

    public static UserAccount createRegularUser(
        WorkspaceId workspaceId,
        EmailAddress emailAddress,
        PasswordHash passwordHash
    ) {
        return new UserAccount(workspaceId, emailAddress, passwordHash, EUserRole.USER);
    }

    public static UserAccount createGoogleVerifiedUser(
        WorkspaceId workspaceId,
        EmailAddress emailAddress,
        PasswordHash passwordHash,
        GoogleSubject googleSubject,
        LocalDateTime connectedAt
    ) {
        UserAccount userAccount = new UserAccount(workspaceId, emailAddress, passwordHash, EUserRole.USER);
        userAccount.connectGoogleIdentity(googleSubject, connectedAt);
        return userAccount;
    }

    public UserAccountId getUserAccountId() {
        return UserAccountId.from(mId);
    }

    public String getEmailAddress() {
        return mEmailAddress;
    }

    public WorkspaceId getWorkspaceId() {
        return WorkspaceId.create(mUserName);
    }

    public LocalDateTime getCreatedAt() {
        return mCreatedAt;
    }

    public void changePasswordHash(PasswordHash passwordHash) {
        if (passwordHash == null) {
            throw new IllegalArgumentException("passwordHash must not be null.");
        }

        mPasswordHash = passwordHash.getValue();
    }

    public boolean hasVerifiedEmailAddress() {
        return mEmailVerifiedAt != null;
    }

    public boolean hasConnectedGoogleAccount() {
        return mGoogleSubject != null && !mGoogleSubject.isBlank();
    }

    public boolean isAdministrator() {
        return mUserRole == EUserRole.ADMIN;
    }

    public void grantAdministratorRole() {
        mUserRole = EUserRole.ADMIN;
    }

    public void markEmailAddressVerified(LocalDateTime verifiedAt) {
        if (verifiedAt == null) {
            throw new IllegalArgumentException("verifiedAt must not be null.");
        }

        mEmailVerifiedAt = verifiedAt;
    }

    public void connectGoogleIdentity(GoogleSubject googleSubject, LocalDateTime connectedAt) {
        if (googleSubject == null) {
            throw new IllegalArgumentException("googleSubject must not be null.");
        }

        if (connectedAt == null) {
            throw new IllegalArgumentException("connectedAt must not be null.");
        }

        mGoogleSubject = googleSubject.getValue();
        mGoogleConnectedAt = connectedAt;
        markEmailAddressVerified(connectedAt);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(mUserRole.getAuthorityName()));
    }

    @Override
    public String getPassword() {
        return mPasswordHash;
    }

    @Override
    public String getUsername() {
        return mUserName;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !mIsLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return mIsEnabled;
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
}
