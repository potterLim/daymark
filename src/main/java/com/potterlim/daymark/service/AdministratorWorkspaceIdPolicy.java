package com.potterlim.daymark.service;

import java.util.List;
import com.potterlim.daymark.config.DaymarkApplicationProperties;
import com.potterlim.daymark.identity.WorkspaceId;
import org.springframework.stereotype.Component;

@Component
public class AdministratorWorkspaceIdPolicy {

    private final DaymarkApplicationProperties mDaymarkApplicationProperties;

    public AdministratorWorkspaceIdPolicy(DaymarkApplicationProperties daymarkApplicationProperties) {
        mDaymarkApplicationProperties = daymarkApplicationProperties;
    }

    public boolean isAdministratorWorkspaceId(WorkspaceId workspaceIdOrNull) {
        if (workspaceIdOrNull == null) {
            return false;
        }

        for (WorkspaceId administratorWorkspaceId : listAdministratorWorkspaceIds()) {
            if (administratorWorkspaceId.getValue().equalsIgnoreCase(workspaceIdOrNull.getValue())) {
                return true;
            }
        }

        return false;
    }

    public List<WorkspaceId> listAdministratorWorkspaceIds() {
        return mDaymarkApplicationProperties.getOperations().getAdministratorWorkspaceIds()
            .stream()
            .filter(administratorWorkspaceId -> administratorWorkspaceId != null && !administratorWorkspaceId.isBlank())
            .map(String::trim)
            .distinct()
            .map(WorkspaceId::create)
            .toList();
    }
}
