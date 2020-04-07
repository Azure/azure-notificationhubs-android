package com.microsoft.windowsazure.messaging.notificationhubs;

/**
 * Wraps an {@link InstallationEnricher} to fulfill the most basic responsibilities of middleware.
 */
class DefaultMiddleware implements InstallationMiddleware {

    private InstallationEnricher mEnricher;

    public DefaultMiddleware(InstallationEnricher enricher) {
        this.mEnricher = enricher;
    }

    /**
     * Applies an enrichment, then calls the next piece of middleware.
     * @param next The {@link InstallationEnricher} that should be invoked when this enricher has
     *             completed its work.
     * @return An {@link InstallationEnricher} that applies multiple enrichments.
     */
    @Override
    public InstallationEnricher getInstallationEnricher(InstallationEnricher next) {
        return subject -> {
            if (this.mEnricher != null) {
                DefaultMiddleware.this.mEnricher.enrichInstallation(subject);
            }
            if (next != null) {
                next.enrichInstallation(subject);
            }
        };
    }
}
