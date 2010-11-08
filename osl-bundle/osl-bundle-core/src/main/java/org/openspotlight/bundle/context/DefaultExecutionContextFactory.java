package org.openspotlight.bundle.context;

import org.openspotlight.bundle.annotation.ArtifactLoaderRegistry;
import org.openspotlight.common.Disposable;
import org.openspotlight.federation.finder.OriginArtifactLoader;
import org.openspotlight.federation.finder.PersistentArtifactManagerProvider;
import org.openspotlight.federation.loader.MutableConfigurationManager;
import org.openspotlight.graph.GraphSessionFactory;
import org.openspotlight.guice.ThreadLocalProvider;
import org.openspotlight.persist.support.SimplePersistFactory;
import org.openspotlight.storage.StorageSession;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Created by IntelliJ IDEA. User: feu Date: Sep 30, 2010 Time: 11:42:35 AM To change this template use File | Settings | File
 * Templates.
 */
public class DefaultExecutionContextFactory extends ThreadLocalProvider<ExecutionContext> implements ExecutionContextFactory {
    private final MutableConfigurationManager                     configurationManager;

    private final GraphSessionFactory                             graphSessionFactory;

    private final Iterable<Class<? extends OriginArtifactLoader>> loaderRegistry;

    private final PersistentArtifactManagerProvider               persistentArtifactManagerProvider;

    private final Provider<StorageSession>                        sessionProvider;

    private final SimplePersistFactory                            simplePersistFactory;

    @Inject
    public DefaultExecutionContextFactory(
                                          final Provider<StorageSession> sessionProvider,
                                          final GraphSessionFactory graphSessionFactory,
                                          final SimplePersistFactory simplePersistFactory,
                                          final PersistentArtifactManagerProvider persistentArtifactManagerProvider,
                                          final MutableConfigurationManager configurationManager,
                                          @ArtifactLoaderRegistry final Iterable<Class<? extends OriginArtifactLoader>> loaderRegistry) {
        this.sessionProvider = sessionProvider;
        this.graphSessionFactory = graphSessionFactory;
        this.simplePersistFactory = simplePersistFactory;
        this.persistentArtifactManagerProvider = persistentArtifactManagerProvider;
        this.configurationManager = configurationManager;
        this.loaderRegistry = loaderRegistry;
    }

    public static void closeResourcesIfNeeded(final Object o) {
        if (o instanceof Disposable) {
            ((Disposable) o).closeResources();
        }
    }

    @Override
    protected ExecutionContext createInstance() {
        return new DefaultExecutionContext(sessionProvider,
                    graphSessionFactory, simplePersistFactory,
                    persistentArtifactManagerProvider, configurationManager,
                    loaderRegistry);
    }

    @Override
    public void closeResources() {
        closeResourcesIfNeeded(sessionProvider);
        closeResourcesIfNeeded(graphSessionFactory);
        closeResourcesIfNeeded(simplePersistFactory);
        closeResourcesIfNeeded(persistentArtifactManagerProvider);
        closeResourcesIfNeeded(configurationManager);

    }

}
