package com.microsoft.windowsazure.messaging.notificationhubs;

/**
 * Wraps an {@link InstallationVisitor} to fulfill the most basic responsibilities of middleware.
 */
class DefaultMiddleware implements InstallationMiddleware {

    private InstallationVisitor mEnricher;

    public DefaultMiddleware(InstallationVisitor enricher) {
        this.mEnricher = enricher;
    }

    /**
     * Applies an enrichment, then calls the next piece of middleware.
     * @param next The {@link InstallationVisitor} that should be invoked when this enricher has
     *             completed its work.
     * @return An {@link InstallationVisitor} that applies multiple enrichments.
     */
    @Override
    public InstallationVisitor getInstallationEnricher(InstallationVisitor next) {
        return subject -> {
            if (this.mEnricher != null) {
                DefaultMiddleware.this.mEnricher.visitInstallation(subject);
            }
            if (next != null) {
                next.visitInstallation(subject);
            }
        };
    }
}
