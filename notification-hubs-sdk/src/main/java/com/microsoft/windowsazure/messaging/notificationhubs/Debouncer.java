package com.microsoft.windowsazure.messaging.notificationhubs;

import android.content.Context;

import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Decorates an arbitrary InstallationManager, responsible for
 * batching several operation requested in quick succession into single most recent operation
 * filtering out same requests.
 */
public class Debouncer extends DebounceInstallationManager {
    private Installation pendingInstallation;
    private Context pendingContext;
    private Installation recentInstallation;
    private int delayInMillisec;
    private Timer timer;

    public Debouncer(InstallationManager installationManager) {
        super(installationManager);
        delayInMillisec = 2000;
    }

    private boolean InstallationIsSameAsRecent(Installation installation) {
        if (recentInstallation == null) {
            return false;
        }

        HashSet<String> tags = (HashSet<String>) installation.getTags();
        HashSet<String> tagsRecent = (HashSet<String>) installation.getTags();

        return installation.getInstallationId().equals(recentInstallation.getInstallationId())
                && installation.getPushChannel().equals(recentInstallation.getPushChannel())
                && ((tags == null && tagsRecent == null)
                || (tags.containsAll(tagsRecent) && tagsRecent.containsAll(tags)));
    }

    @Override
    public void saveInstallation(Context context, Installation installation) {
        if (InstallationIsSameAsRecent(installation)) {
            return;
        }

        if (pendingInstallation == null) {
            if (timer == null) {
                timer = new Timer();
            }

            timer.schedule(new TimerTask() {
                public void run() {
                    installationManager.saveInstallation(pendingContext, pendingInstallation);
                    recentInstallation = pendingInstallation;
                    pendingInstallation = null;
                    pendingContext = null;
                    timer.cancel();
                    timer = null;
                }
            }, delayInMillisec);
        }

        this.pendingInstallation = installation;
        this.pendingContext = context;
    }
}