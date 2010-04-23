package org.openspotlight.persist.lazy.test;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Before;
import org.junit.Test;
import org.openspotlight.persist.support.SimplePersistCapable;
import org.openspotlight.persist.support.SimplePersistImpl;
import org.openspotlight.storage.STPartition;
import org.openspotlight.storage.STStorageSession;
import org.openspotlight.storage.domain.node.STNodeEntry;
import org.openspotlight.storage.redis.guice.JRedisFactory;
import org.openspotlight.storage.redis.guice.JRedisServerDetail;
import org.openspotlight.storage.redis.guice.JRedisStorageModule;

import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.openspotlight.storage.STRepositoryPath.repositoryPath;

public class SimplePersistLazyBehaviorTest {
    private enum JRedisServerConfigExample implements JRedisServerDetail {
        DEFAULT("localhost", 6379, 0);

        private JRedisServerConfigExample(String serverName, int serverPort, int db) {
            this.serverName = serverName;
            this.serverPort = serverPort;
            this.db = db;
        }

        private final String serverName;

        private final int db;

        public int getDb() {
            return db;
        }

        public String getPassword() {
            return null;
        }

        private final int serverPort;

        public String getServerName() {
            return serverName;
        }

        public int getServerPort() {
            return serverPort;
        }
    }

    private enum ExamplePartition implements STPartition {

        DEFAULT("default");

        private final String partitionName;

        public String getPartitionName() {
            return partitionName;
        }

        ExamplePartition(String partitionName) {
            this.partitionName = partitionName;
        }
    }

    final Map<STPartition, JRedisServerDetail> mappedServerConfig;

    {
        mappedServerConfig = ImmutableMap.<STPartition, JRedisServerDetail>builder()
                .put(ExamplePartition.DEFAULT, JRedisServerConfigExample.DEFAULT).build();
    }

    final Injector autoFlushInjector = Guice.createInjector(new JRedisStorageModule(STStorageSession.STFlushMode.AUTO,
            mappedServerConfig, repositoryPath("repository")));

    STStorageSession session;
    SimplePersistCapable<STNodeEntry, STStorageSession> simplePersist;

    @Before
    public void cleanPreviousData() throws Exception {
        JRedisFactory autoFlushFactory = autoFlushInjector.getInstance(JRedisFactory.class);
        autoFlushFactory.getFrom(ExamplePartition.DEFAULT).flushall();
        this.session = autoFlushInjector.getInstance(STStorageSession.class);
        this.simplePersist = new SimplePersistImpl(session, ExamplePartition.DEFAULT);
    }


    @Test
    public void shouldLoadSavedValue() throws Exception {


        ClassWithLazyProperty bean = new ClassWithLazyProperty();
        bean.setTest("test");

        bean.getBigPojoProperty().setTransient(
                new SerializablePojoProperty());
        bean.getBigPojoProperty().get(simplePersist).setAnotherProperty("test");
        assertThat(bean.getBigPojoProperty().getMetadata().getTransient(),
                is(notNullValue()));
        final STNodeEntry node = simplePersist.convertBeanToNode( bean);
        bean = simplePersist.convertNodeToBean(node);
        assertThat(bean.getBigPojoProperty().get(simplePersist),
                is(notNullValue()));

    }

    @Test
    public void shouldLooseWeakValue() throws Exception {
        final ClassWithLazyProperty bean = new ClassWithLazyProperty();
        bean.setTest("test");
        bean.getBigPojoProperty().getMetadata().setCached(
                new SerializablePojoProperty());
        bean.getBigPojoProperty().get( null).setAnotherProperty("test");
        assertThat(bean.getBigPojoProperty().get(null), is(notNullValue()));
        System.gc();
        assertThat(bean.getBigPojoProperty().getMetadata().getTransient(),
                is(nullValue()));

    }

    @Test
    public void shouldSaveTransientValue() throws Exception {
        ClassWithLazyProperty bean = new ClassWithLazyProperty();
        bean.setTest("test");

        bean.getBigPojoProperty().setTransient(
                new SerializablePojoProperty());
        bean.getBigPojoProperty().get(simplePersist).setAnotherProperty("test");
        assertThat(bean.getBigPojoProperty().getMetadata().getTransient(),
                is(notNullValue()));
        final STNodeEntry node = simplePersist.convertBeanToNode(bean);

        bean = simplePersist.convertNodeToBean(node);
        assertThat(bean.getBigPojoProperty().getMetadata().getCached( simplePersist), is(notNullValue()));
        assertThat(bean.getBigPojoProperty().getMetadata().getTransient(),
                is(nullValue()));
    }

}
