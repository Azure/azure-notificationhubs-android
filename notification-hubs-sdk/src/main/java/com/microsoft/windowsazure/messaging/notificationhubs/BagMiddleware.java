package com.microsoft.windowsazure.messaging.notificationhubs;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Does no routing of Middleware based on conditions, simply applies all enrichments it is aware of
 * then calls the next Middleware.
 */
class BagMiddleware implements InstallationMiddleware {

    private final Set<InstallationEnricher> mEnrichers;

    public BagMiddleware() {
        this.mEnrichers = new HashSet<InstallationEnricher>();
    }

    public BagMiddleware(Collection<? extends InstallationEnricher> enrichers) {
        this.mEnrichers = new HashSet<InstallationEnricher>(enrichers);
    }

    /**
     * Updates the set of {@link InstallationEnricher} implementers that will be called when this
     * {@link InstallationMiddleware} is invoked.
     * @param enricher The {@link InstallationEnricher} that should be applied in the future.
     * @return True if this enricher was not previously part of the set to be applied.
     */
    public boolean addEnricher(InstallationEnricher enricher){
        return this.mEnrichers.add(enricher);
    }

    /**
     * Creates a link in the Installation chain that applies many enrichments at once.
     * @param next The {@link InstallationEnricher} that should be invoked when this enricher has
     *             completed its work.
     * @return A {@link InstallationEnricher} that applies many updates to an {@link Installation}.
     */
    @Override
    public InstallationEnricher getInstallationEnricher(InstallationEnricher next) {
        return subject -> {
            for (InstallationEnricher e: BagMiddleware.this.mEnrichers) {
                e.enrichInstallation(subject);
            }
            next.enrichInstallation(subject);
        };
    }
}
