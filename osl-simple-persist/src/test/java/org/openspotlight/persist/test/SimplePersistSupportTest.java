package org.openspotlight.persist.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.jcr.Node;
import javax.jcr.Session;

import org.hamcrest.core.Is;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openspotlight.common.SharedConstants;
import org.openspotlight.jcr.provider.DefaultJcrDescriptor;
import org.openspotlight.jcr.provider.JcrConnectionProvider;
import org.openspotlight.persist.support.SimplePersistSupport;

/**
 * The Class SimplePersistSupportTest.
 */
public class SimplePersistSupportTest {

    /** The provider. */
    private static JcrConnectionProvider provider;

    /**
     * Setup.
     * 
     * @throws Exception the exception
     */
    @BeforeClass
    public static void setup() throws Exception {
        provider = JcrConnectionProvider.createFromData(DefaultJcrDescriptor.TEMP_DESCRIPTOR);
    }

    /** The session. */
    private Session session = null;

    /**
     * Close session.
     */
    @After
    public void closeSession() {
        if (this.session != null) {
            this.session.logout();
            this.session = null;
        }
    }

    /**
     * Setup session.
     */
    @Before
    public void setupSession() {
        this.session = provider.openSession();
    }

    /**
     * Should convert bean to jcr node.
     * 
     * @throws Exception the exception
     */
    @Test
    public void shouldConvertBeanToJcrNode() throws Exception {
        final RootObj root = new RootObj();
        final LevelOneObj obj1 = new LevelOneObj();
        final LevelTwoObj obj2 = new LevelTwoObj();
        final LevelThreeObj obj3 = new LevelThreeObj();
        obj1.setRootObj(root);
        obj2.setLevelOneObj(obj1);
        final PropertyObj propertyObj = new PropertyObj();
        propertyObj.setName("name");
        propertyObj.setValue(2);
        obj2.setPropertyObj(propertyObj);
        obj3.setLevelTwoObj(obj2);
        obj2.setProperty("propVal");
        final Node node = SimplePersistSupport.convertBeanToJcr(SharedConstants.DEFAULT_JCR_ROOT_NAME, this.session, obj3);
        final String path = node.getPath();
        Assert.assertThat(
                          path,
                          Is.is("/osl/NODE_org_openspotlight_persist_test_RootObj/NODE_org_openspotlight_persist_test_LevelOneObj/NODE_org_openspotlight_persist_test_LevelTwoObj/NODE_org_openspotlight_persist_test_LevelThreeObj"));
        Assert.assertThat(node.getProperty("node.property.property.type").getString(), Is.is("java.lang.String"));
        Assert.assertThat(node.getProperty("node.typeName").getString(), Is.is("org.openspotlight.persist.test.LevelThreeObj"));
        Assert.assertThat(node.getProperty("node.hashValue").getString(), Is.is("189cb273-86af-3350-885c-243376daea19"));
        Assert.assertThat(node.getProperty("node.key.key.type").getString(), Is.is("java.lang.String"));

        final Node parentNode = node.getParent();
        Assert.assertThat(parentNode.getProperty("node.property.property.type").getString(), Is.is("java.lang.String"));
        Assert.assertThat(parentNode.getProperty("node.typeName").getString(),
                          Is.is("org.openspotlight.persist.test.LevelTwoObj"));
        Assert.assertThat(parentNode.getProperty("node.hashValue").getString(), Is.is("fc6406df-60ae-331a-8ebb-b6269be079bb"));
        Assert.assertThat(parentNode.getProperty("node.property.property.value").getString(), Is.is("propVal"));
        Assert.assertThat(parentNode.getProperty("node.key.key.type").getString(), Is.is("java.lang.String"));
        final Node nodeProperty = parentNode.getNode("NODE_PROPERTY_propertyObj");
        Assert.assertThat(nodeProperty.getProperty("node.key.value.type").getString(), Is.is("int"));
        Assert.assertThat(nodeProperty.getProperty("node.hashValue").getString(), Is.is("f9facf49-a10f-35f3-90d5-1f2babe7478f"));
        Assert.assertThat(nodeProperty.getProperty("node.property.name.type").getString(), Is.is("java.lang.String"));
        Assert.assertThat(nodeProperty.getProperty("node.key.value.value").getString(), Is.is("2"));
        Assert.assertThat(nodeProperty.getProperty("property.name").getString(), Is.is("propertyObj"));
        Assert.assertThat(nodeProperty.getProperty("node.property.name.value").getString(), Is.is("name"));
        Assert.assertThat(nodeProperty.getProperty("node.typeName").getString(),
                          Is.is("org.openspotlight.persist.test.PropertyObj"));
    }

    /**
     * Should convert jcr node to bean.
     * 
     * @throws Exception the exception
     */
    @Test
    public void shouldConvertJcrNodeToBean() throws Exception {
        final RootObj root = new RootObj();
        final LevelOneObj obj1 = new LevelOneObj();
        final LevelTwoObj obj2 = new LevelTwoObj();
        final LevelThreeObj obj3 = new LevelThreeObj();
        obj1.setRootObj(root);
        obj2.setLevelOneObj(obj1);
        obj3.setLevelTwoObj(obj2);
        obj3.setBooleanList(new ArrayList<Boolean>());
        obj3.getBooleanList().add(Boolean.TRUE);
        obj3.getBooleanList().add(Boolean.FALSE);
        obj3.getBooleanList().add(Boolean.TRUE);
        obj3.getBooleanList().add(Boolean.TRUE);
        obj3.setNumberMap(new HashMap<Double, Integer>());
        obj3.getNumberMap().put(1.0, 3);
        obj3.getNumberMap().put(2.0, 2);
        obj3.getNumberMap().put(3.0, 1);

        obj2.setProperty("propVal");
        final PropertyObj propertyObj = new PropertyObj();
        propertyObj.setName("name");
        propertyObj.setValue(2);
        obj2.setPropertyObj(propertyObj);

        final Node node = SimplePersistSupport.convertBeanToJcr(SharedConstants.DEFAULT_JCR_ROOT_NAME + "/lalala/lelele",
                                                                this.session, obj3);
        final LevelThreeObj convertedFromJcr = SimplePersistSupport.convertJcrToBean(this.session, node);
        Assert.assertThat(obj3.getKey(), Is.is(convertedFromJcr.getKey()));
        Assert.assertThat(obj3.getProperty(), Is.is(convertedFromJcr.getProperty()));
        Assert.assertThat(obj3.getLevelTwoObj().getKey(), Is.is(convertedFromJcr.getLevelTwoObj().getKey()));
        Assert.assertThat(obj3.getLevelTwoObj().getPropertyObj().getName(),
                          Is.is(convertedFromJcr.getLevelTwoObj().getPropertyObj().getName()));
        Assert.assertThat(obj3.getLevelTwoObj().getLevelOneObj().getProperty(),
                          Is.is(convertedFromJcr.getLevelTwoObj().getLevelOneObj().getProperty()));
        Assert.assertThat(obj3.getBooleanList(), Is.is(Arrays.asList(true, false, true, true)));
        Assert.assertThat(obj3.getNumberMap().get(1.0), Is.is(3));
        Assert.assertThat(obj3.getNumberMap().get(2.0), Is.is(2));
        Assert.assertThat(obj3.getNumberMap().get(3.0), Is.is(1));

    }

}
