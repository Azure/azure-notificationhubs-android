package com.microsoft.windowsazure.messaging.notificationhubs;

/**
 * Wraps an {@link InstallationEnricher} to fulfill the most basic responsibilities of middleware.
 */
class DefaultMiddleware implements InstallationMiddleware {

    private InstallationEnricher enricher;

    public DefaultMiddleware(InstallationEnricher enricher) {
        this.enricher = enricher;
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
            if (this.enricher != null) {
                DefaultMiddleware.this.enricher.enrichInstallation(subject);
            }
            if (next != null) {
                next.enrichInstallation(subject);
            }
        };
    }
}
