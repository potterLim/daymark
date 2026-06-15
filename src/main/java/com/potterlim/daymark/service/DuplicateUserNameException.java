package com.potterlim.daymark.service;

import com.potterlim.daymark.identity.WorkspaceId;

public class DuplicateUserNameException extends RuntimeException {

    public DuplicateUserNameException(WorkspaceId workspaceId) {
        super("Duplicate workspace id: " + workspaceId.getValue());
    }
}
