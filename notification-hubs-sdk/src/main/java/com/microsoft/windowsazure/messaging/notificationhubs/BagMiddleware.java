package com.microsoft.windowsazure.messaging.notificationhubs;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Does no routing of Middleware based on conditions, simply applies all enrichments it is aware of
 * then calls the next Middleware.
 */
class BagMiddleware implements InstallationMiddleware {

    private final Set<InstallationVisitor> mEnrichers;

    public BagMiddleware() {
        this.mEnrichers = new HashSet<InstallationVisitor>();
    }

    public BagMiddleware(Collection<? extends InstallationVisitor> enrichers) {
        this.mEnrichers = new HashSet<InstallationVisitor>(enrichers);
    }

    /**
     * Updates the set of {@link InstallationVisitor} implementers that will be called when this
     * {@link InstallationMiddleware} is invoked.
     * @param enricher The {@link InstallationVisitor} that should be applied in the future.
     * @return True if this enricher was not previously part of the set to be applied.
     */
    public boolean addEnricher(InstallationVisitor enricher){
        return this.mEnrichers.add(enricher);
    }

    /**
     * Creates a link in the Installation chain that applies many enrichments at once.
     * @param next The {@link InstallationVisitor} that should be invoked when this enricher has
     *             completed its work.
     * @return A {@link InstallationVisitor} that applies many updates to an {@link Installation}.
     */
    @Override
    public InstallationVisitor getInstallationEnricher(InstallationVisitor next) {
        return subject -> {
            for (InstallationVisitor e: BagMiddleware.this.mEnrichers) {
                e.visitInstallation(subject);
            }
            next.visitInstallation(subject);
        };
    }
}
