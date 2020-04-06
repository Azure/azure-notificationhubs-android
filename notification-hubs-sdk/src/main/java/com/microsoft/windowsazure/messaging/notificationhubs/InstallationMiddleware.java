package com.microsoft.windowsazure.messaging.notificationhubs;

/**
 * Offers a mechanism for dynamically controlling which {@link InstallationEnricher} instances
 * should be invoked before an {@link Installation} is sent to a backend.
 */
public interface InstallationMiddleware {
    /**
     * Builds an {@link InstallationEnricher} decorator that controls which details will be added to
     * an {@link Installation}.
     * @param next The {@link InstallationEnricher} that should be invoked when this enricher has
     *             completed its work.
     * @return An {@link InstallationEnricher} that will add all desired details to an {@link Installation}.
     */
    InstallationEnricher getInstallationEnricher(InstallationEnricher next);
}
