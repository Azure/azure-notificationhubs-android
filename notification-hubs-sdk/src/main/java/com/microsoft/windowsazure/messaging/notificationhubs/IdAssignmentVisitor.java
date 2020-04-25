package com.microsoft.windowsazure.messaging.notificationhubs;

import java.util.UUID;

class IdAssignmentVisitor implements InstallationVisitor {

    private String mInstallationId;

    public IdAssignmentVisitor() {
        this(UUID.randomUUID().toString());
    }

    public IdAssignmentVisitor(String id) {
        mInstallationId = id;
    }

    /**
     * Modifies an {@link Installation} to add more information before being registered with a
     * backend.
     *
     * @param subject The {@link Installation} that should be modified to include more detail.
     */
    @Override
    public void visitInstallation(Installation subject) {
        if(subject.getInstallationId() == null) {
            subject.setInstallationId(mInstallationId);
        }
    }

    public String getInstallationId() {
        return mInstallationId;
    }

    public void setInstallationId(String mInstallationId) {
        this.mInstallationId = mInstallationId;
    }
}
