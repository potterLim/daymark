package com.potterlim.daymark.service;

import com.potterlim.daymark.identity.EmailAddress;

public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(EmailAddress emailAddress) {
        super("User account already exists with email address: " + emailAddress.getValue());
    }
}
